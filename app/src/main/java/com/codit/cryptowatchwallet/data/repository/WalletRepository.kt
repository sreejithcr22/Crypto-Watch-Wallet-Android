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
import kotlinx.coroutines.flow.flowOf
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
    // The outer try/catch also covers a synchronous throw while Room builds
    // the Flow (`.catch` only handles failures during collection).
    fun observeWallets(): Flow<List<Wallet>> = try {
        walletDao.observeWallets().catch { emit(emptyList()) }
    } catch (_: Throwable) {
        flowOf(emptyList())
    }

    fun observeWallet(name: String): Flow<Wallet?> = try {
        walletDao.observeWalletByName(name).catch { emit(null) }
    } catch (_: Throwable) {
        flowOf(null)
    }

    suspend fun getWalletByName(name: String): Wallet? = try {
        walletDao.getWalletByName(name)
    } catch (_: Throwable) {
        null
    }

    suspend fun deleteWallet(wallet: Wallet): Boolean {
        return try {
            walletDao.deleteWallet(wallet) > 0
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun refreshWalletsAndWorth(refreshBalances: Boolean = true): Boolean {
        return try {
            val marketOk = try {
                marketRepository.refreshMarket()
            } catch (_: Throwable) {
                false
            }
            if (refreshBalances) {
                try {
                    refreshBalancesFromNetwork()
                } catch (_: Throwable) {
                }
            }
            try {
                updateAllWalletsWorth(settingsRepository.getDefaultCurrency())
            } catch (_: Throwable) {
            }
            marketOk
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun updateAllWalletsWorth(currencyCode: String) {
        try {
            val wallets = try {
                walletDao.getAllWallets()
            } catch (_: Throwable) {
                return
            }
            if (wallets.isEmpty()) return
            wallets.forEach { wallet ->
                val rate = try {
                    marketDao.getCoinPricesFor(wallet.coinCode)?.prices?.get(currencyCode)
                } catch (_: Throwable) {
                    null
                }
                val balance = wallet.balance?.coinBalance
                val worth = if (rate != null && balance != null) {
                    Coin.calculateCoinWorth(balance, rate, currencyCode)
                } else {
                    Coin.PRICE_NOT_AVAILABLE
                }
                wallet.coinWorth = worth
            }
            try {
                walletDao.updateWallets(wallets)
            } catch (e: Throwable) {
                Log.d("wallet", "updateAllWalletsWorth failed: $e")
            }
        } catch (e: Throwable) {
            Log.d("wallet", "updateAllWalletsWorth failed: $e")
        }
    }

    suspend fun addWallet(name: String, coinCode: String, address: String): AddWalletResult {
        // Duplicate checks
        try {
            if (walletDao.checkIfNameDuplicate(name) != null) {
                return AddWalletResult.Error(ERROR_DUPLICATE_NAME)
            }
            walletDao.checkIfAddressDuplicate(address)?.let {
                return AddWalletResult.Error("Address already exists with wallet: ${it.displayName}")
            }
        } catch (_: Throwable) {
            return AddWalletResult.Error(ERROR_UNKNOWN)
        }

        val balance = fetchBalance(coinCode, address)
            ?: return AddWalletResult.Error(mapError())

        return try {
            val rate = try {
                marketDao.getCoinPricesFor(coinCode)?.prices?.get(Currency.USD)
            } catch (_: Throwable) {
                null
            }
            val worth = if (rate != null) {
                Coin.calculateCoinWorth(balance.coinBalance, rate, Currency.USD)
            } else {
                Coin.PRICE_NOT_AVAILABLE
            }
            walletDao.addNewWallet(Wallet(name, coinCode, address, balance, worth))
            // Kick off a market refresh + revaluation so the new wallet shows correct fiat value.
            try {
                marketRepository.refreshMarket()
            } catch (_: Throwable) {
            }
            try {
                updateAllWalletsWorth(settingsRepository.getDefaultCurrency())
            } catch (_: Throwable) {
            }
            AddWalletResult.Success
        } catch (e: Throwable) {
            AddWalletResult.Error(ERROR_UNKNOWN)
        }
    }

    private fun mapError(): String {
        return try {
            if (!connectivity.isConnected()) ERROR_NO_INTERNET else ERROR_UNKNOWN
        } catch (_: Throwable) {
            ERROR_UNKNOWN
        }
    }

    suspend fun fetchBalance(coinCode: String, address: String): Balance? {
        return try {
            when {
                coinCode == Coin.BTC || coinCode == Coin.ETH ||
                    coinCode == Coin.LTC || coinCode == Coin.DASH ||
                    coinCode.equals(Coin.DOGE, ignoreCase = true) -> {
                    val url = BASE_URL_BLOCKCYPHER + coinCode.lowercase() + "/main/addrs/" + address + "/balance"
                    val response = try {
                        walletApi.getCypherAddressBalance(url)
                    } catch (_: Throwable) {
                        return null
                    }
                    val body = try {
                        response.body()
                    } catch (_: Throwable) {
                        null
                    }
                    if (response.isSuccessful && body != null) {
                        try {
                            body.getWalletBalance(coinCode)
                        } catch (_: Throwable) {
                            null
                        }
                    } else {
                        Log.d("wallet", "fetchBalance failed: code=${try { response.code() } catch (_: Throwable) { -1 }}")
                        null
                    }
                }
                coinCode == Coin.BCH -> {
                    val response = try {
                        walletApi.getBCHAddressBalance("https://blockdozer.com/insight-api/addr/$address")
                    } catch (_: Throwable) {
                        return null
                    }
                    val body = try {
                        response.body()
                    } catch (_: Throwable) {
                        null
                    }
                    if (response.isSuccessful && body != null) {
                        try {
                            body.getWalletBalance(coinCode)
                        } catch (_: Throwable) {
                            null
                        }
                    } else null
                }
                coinCode == Coin.XRP -> {
                    val response = try {
                        walletApi.getRippleBalance("https://data.ripple.com/v2/accounts/$address/balances")
                    } catch (_: Throwable) {
                        return null
                    }
                    val body = try {
                        response.body()
                    } catch (_: Throwable) {
                        null
                    }
                    if (response.isSuccessful && body != null) {
                        try {
                            body.getWalletBalance(coinCode)
                        } catch (_: Throwable) {
                            null
                        }
                    } else null
                }
                else -> null
            }
        } catch (e: Throwable) {
            Log.d("wallet", "fetchBalance exception: ${e.message}")
            null
        }
    }

    private suspend fun refreshBalancesFromNetwork() {
        val wallets = try {
            walletDao.getAllWallets()
        } catch (_: Throwable) {
            return
        }
        if (wallets.isEmpty()) return
        val updated = mutableListOf<Wallet>()
        val notifications = mutableMapOf<String, String>()
        val gson = try {
            Gson()
        } catch (_: Throwable) {
            return
        }
        for (wallet in wallets) {
            val oldBalance = wallet.balance ?: continue
            val newBalance = try {
                fetchBalance(wallet.coinCode, wallet.walletAddress)
            } catch (_: Throwable) {
                null
            } ?: continue
            val tx = try {
                checkForNewTx(oldBalance, newBalance)
            } catch (_: Throwable) {
                continue
            }
            if (tx.tnxCount > 0) {
                wallet.balance = newBalance
                updated.add(wallet)
                try {
                    notifications[wallet.displayName] = gson.toJson(tx)
                } catch (_: Throwable) {
                }
            }
            // Be nice to rate-limited free APIs.
            try {
                delay(3000)
            } catch (_: Throwable) {
            }
        }
        if (updated.isNotEmpty()) {
            try {
                walletDao.updateWallets(updated)
            } catch (_: Throwable) {
                return
            }
            try {
                settingsRepository.updateNotificationQ(Gson().toJson(notifications))
            } catch (_: Throwable) {
            }
            try {
                drainNotifications()
            } catch (_: Throwable) {
            }
        }
    }

    suspend fun drainNotifications() {
        val json = try {
            settingsRepository.getNotificationQ() ?: return
        } catch (_: Throwable) {
            return
        }
        try {
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            val queue: HashMap<String, String> = try {
                Gson().fromJson(json, type) ?: return
            } catch (_: Throwable) {
                try {
                    settingsRepository.updateNotificationQ(null)
                } catch (_: Throwable) {
                }
                return
            }
            if (queue.isEmpty()) return
            for ((name, txJson) in queue) {
                try {
                    val tx = try {
                        Gson().fromJson(txJson, Transaction::class.java)
                    } catch (_: Throwable) {
                        null
                    }
                    val wallet = try {
                        walletDao.getWalletByName(name)
                    } catch (_: Throwable) {
                        null
                    }
                    if (wallet != null && tx != null) {
                        try {
                            notificationHelper.showWalletNotification(
                                wallet,
                                tx,
                                settingsRepository.generateUniqueId()
                            )
                        } catch (_: Throwable) {
                        }
                    }
                } catch (_: Throwable) {
                }
            }
            try {
                settingsRepository.updateNotificationQ(null)
            } catch (_: Throwable) {
            }
        } catch (e: Throwable) {
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
        } catch (_: Throwable) {
            Transaction(0, null)
        }
    }
}
