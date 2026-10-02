package com.codit.cryptowatchwallet.data.repository

import android.util.Log
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.local.WalletDao
import com.codit.cryptowatchwallet.data.remote.WalletApi
import com.codit.cryptowatchwallet.model.Balance
import com.codit.cryptowatchwallet.model.Transaction
import com.codit.cryptowatchwallet.model.Wallet
import com.codit.cryptowatchwallet.util.Coin
import com.codit.cryptowatchwallet.util.Connectivity
import com.codit.cryptowatchwallet.util.Currency
import com.codit.cryptowatchwallet.util.NotificationHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AddWalletResult {
    data object Success : AddWalletResult
    data class Error(val message: String) : AddWalletResult
}

@Singleton
class WalletRepository @Inject constructor(
    private val walletDao: WalletDao,
    private val marketDao: MarketDao,
    private val marketRepository: MarketRepository,
    private val walletApi: WalletApi,
    private val settingsRepository: SettingsRepository,
    private val notificationHelper: NotificationHelper,
    private val connectivity: Connectivity
) {
    companion object {
        const val ERROR_BAD_REQUEST = "Invalid address"
        const val ERROR_NO_INTERNET = "No internet connection"
        const val ERROR_TIMEOUT = "Connection timed out, please try again"
        const val ERROR_UNKNOWN = "Sorry, something went wrong"
        const val SUCCESS_ADDED = "Wallet added successfully"
        const val ERROR_DUPLICATE_NAME = "Wallet name already exists"
        private const val BASE_URL_BLOCKCYPHER = "https://api.blockcypher.com/v1/"
    }

    // A database error must never take the UI down: emit an empty list instead.
    fun observeWallets(): Flow<List<Wallet>> = walletDao.observeWallets().catch { emit(emptyList()) }

    fun observeWallet(name: String): Flow<Wallet?> = walletDao.observeWalletByName(name).catch { emit(null) }

    suspend fun getWalletByName(name: String): Wallet? = walletDao.getWalletByName(name)

    suspend fun deleteWallet(wallet: Wallet): Boolean {
        return try {
            walletDao.deleteWallet(wallet) > 0
        } catch (_: Exception) {
            false
        }
    }

    suspend fun refreshWalletsAndWorth(refreshBalances: Boolean = true): Boolean {
        val marketOk = marketRepository.refreshMarket()
        if (refreshBalances) {
            refreshBalancesFromNetwork()
        }
        updateAllWalletsWorth(settingsRepository.getDefaultCurrency())
        return marketOk
    }

    suspend fun updateAllWalletsWorth(currencyCode: String) {
        try {
            val wallets = walletDao.getAllWallets()
            if (wallets.isEmpty()) return
            wallets.forEach { wallet ->
                val rate = marketDao.getCoinPricesFor(wallet.coinCode)?.prices?.get(currencyCode)
                val worth = if (rate != null && wallet.balance != null) {
                    Coin.calculateCoinWorth(wallet.balance!!.coinBalance, rate, currencyCode)
                } else {
                    Coin.PRICE_NOT_AVAILABLE
                }
                wallet.coinWorth = worth
            }
            walletDao.updateWallets(wallets)
        } catch (e: Exception) {
            Log.d("wallet", "updateAllWalletsWorth failed: $e")
        }
    }

    suspend fun addWallet(name: String, coinCode: String, address: String): AddWalletResult {
        // Duplicate checks
        if (walletDao.checkIfNameDuplicate(name) != null) {
            return AddWalletResult.Error(ERROR_DUPLICATE_NAME)
        }
        walletDao.checkIfAddressDuplicate(address)?.let {
            return AddWalletResult.Error("Address already exists with wallet: ${it.displayName}")
        }

        val balance = fetchBalance(coinCode, address)
            ?: return AddWalletResult.Error(mapError())

        return try {
            val rate = marketDao.getCoinPricesFor(coinCode)?.prices?.get(Currency.USD)
            val worth = if (rate != null) {
                Coin.calculateCoinWorth(balance.coinBalance, rate, Currency.USD)
            } else {
                Coin.PRICE_NOT_AVAILABLE
            }
            walletDao.addNewWallet(Wallet(name, coinCode, address, balance, worth))
            // Kick off a market refresh + revaluation so the new wallet shows correct fiat value.
            marketRepository.refreshMarket()
            updateAllWalletsWorth(settingsRepository.getDefaultCurrency())
            AddWalletResult.Success
        } catch (e: Exception) {
            AddWalletResult.Error(ERROR_UNKNOWN)
        }
    }

    private fun mapError(): String {
        return if (!connectivity.isConnected()) ERROR_NO_INTERNET else ERROR_UNKNOWN
    }

    suspend fun fetchBalance(coinCode: String, address: String): Balance? {
        return try {
            when {
                coinCode == Coin.BTC || coinCode == Coin.ETH ||
                    coinCode == Coin.LTC || coinCode == Coin.DASH ||
                    coinCode.equals(Coin.DOGE, ignoreCase = true) -> {
                    val url = BASE_URL_BLOCKCYPHER + coinCode.lowercase() + "/main/addrs/" + address + "/balance"
                    val response = walletApi.getCypherAddressBalance(url)
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.getWalletBalance(coinCode)
                    } else {
                        Log.d("wallet", "fetchBalance failed: code=${response.code()}")
                        null
                    }
                }
                coinCode == Coin.BCH -> {
                    val response = walletApi.getBCHAddressBalance("https://blockdozer.com/insight-api/addr/$address")
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.getWalletBalance(coinCode)
                    } else null
                }
                coinCode == Coin.XRP -> {
                    val response = walletApi.getRippleBalance("https://data.ripple.com/v2/accounts/$address/balances")
                    if (response.isSuccessful && response.body() != null) {
                        response.body()!!.getWalletBalance(coinCode)
                    } else null
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.d("wallet", "fetchBalance exception: ${e.message}")
            null
        }
    }

    private suspend fun refreshBalancesFromNetwork() {
        val wallets = try {
            walletDao.getAllWallets()
        } catch (_: Exception) {
            return
        }
        if (wallets.isEmpty()) return
        val updated = mutableListOf<Wallet>()
        val notifications = mutableMapOf<String, String>()
        val gson = Gson()
        for (wallet in wallets) {
            val newBalance = fetchBalance(wallet.coinCode, wallet.walletAddress)
            if (newBalance != null && wallet.balance != null) {
                val tx = checkForNewTx(wallet.balance!!, newBalance)
                if (tx.tnxCount > 0) {
                    wallet.balance = newBalance
                    updated.add(wallet)
                    notifications[wallet.displayName] = gson.toJson(tx)
                }
            }
            // Be nice to rate-limited free APIs.
            delay(3000)
        }
        if (updated.isNotEmpty()) {
            walletDao.updateWallets(updated)
            settingsRepository.updateNotificationQ(Gson().toJson(notifications))
            drainNotifications()
        }
    }

    suspend fun drainNotifications() {
        val json = settingsRepository.getNotificationQ() ?: return
        try {
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            val queue: HashMap<String, String> = Gson().fromJson(json, type) ?: return
            if (queue.isEmpty()) return
            for ((name, txJson) in queue) {
                val tx = Gson().fromJson(txJson, Transaction::class.java)
                val wallet = walletDao.getWalletByName(name)
                if (wallet != null && tx != null) {
                    notificationHelper.showWalletNotification(
                        wallet,
                        tx,
                        settingsRepository.generateUniqueId()
                    )
                }
            }
            settingsRepository.updateNotificationQ(null)
        } catch (e: Exception) {
            Log.d("wallet", "drainNotifications failed: $e")
        }
    }

    private fun checkForNewTx(oldBalance: Balance, newBalance: Balance): Transaction {
        return try {
            val oldCoin = BigDecimal(oldBalance.coinBalance)
            val newCoin = BigDecimal(newBalance.coinBalance)
            val countDiff = newBalance.transactionCount - oldBalance.transactionCount
            if (countDiff > 0) {
                val diff = newCoin.subtract(oldCoin).toPlainString()
                Transaction(countDiff, diff)
            } else {
                Transaction(0, null)
            }
        } catch (_: Exception) {
            Transaction(0, null)
        }
    }
}
