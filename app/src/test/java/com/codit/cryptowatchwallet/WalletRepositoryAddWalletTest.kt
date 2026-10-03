package com.codit.cryptowatchwallet

import android.content.Context
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.local.WalletDao
import com.codit.cryptowatchwallet.data.remote.CoinGeckoApi
import com.codit.cryptowatchwallet.data.remote.MarketApi
import com.codit.cryptowatchwallet.data.remote.WalletApi
import com.codit.cryptowatchwallet.data.repository.AddWalletResult
import com.codit.cryptowatchwallet.data.repository.MarketRepository
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.model.BCHAddressBalance
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.model.CypherAddressBalance
import com.codit.cryptowatchwallet.model.RippleBalance
import com.codit.cryptowatchwallet.model.Wallet
import com.codit.cryptowatchwallet.util.Connectivity
import com.codit.cryptowatchwallet.util.NotificationHelper
import com.google.gson.JsonElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Response

/**
 * Regression test for the endless "Adding…" spinner: [WalletRepository.addWallet]
 * persisted the wallet and then awaited an unbounded post-insert market refresh,
 * so a hung/queued refresh (mutex contention, 60s network timeouts) delayed the
 * Success result forever — while Back already showed the wallet in the list.
 *
 * The refresh after insert is now time-bounded; this test hangs the network
 * deterministically (120s virtual delay) and asserts Success still returns
 * promptly with the wallet persisted.
 */
class WalletRepositoryAddWalletTest {

    /** Simulates a stuck network: CoinGecko never answers in a useful bound. */    private class HungCoinGeckoApi : CoinGeckoApi {
        override suspend fun getPrices(
            symbols: String,
            vsCurrencies: String,
            precision: String
        ): Map<String, Map<String, Double>> {
            delay(120_000)
            return mapOf("BTC" to mapOf("usd" to 50_000.0))
        }

        override suspend fun getExchangeRates(): Response<JsonElement> {
            delay(120_000)
            throw RuntimeException("hung network")
        }
    }

    private class FailingMarketApi : MarketApi {
        override suspend fun getAllCoinPrices(
            coinsList: String,
            currencyList: String,
            apiKey: String?
        ): Response<JsonElement> = throw RuntimeException("hung network")
    }

    private class FakeWalletApi(
        private val balanceBody: CypherAddressBalance
    ) : WalletApi {
        override suspend fun getCypherAddressBalance(url: String): Response<CypherAddressBalance> =
            Response.success(balanceBody)

        override suspend fun getBCHAddressBalance(url: String): Response<BCHAddressBalance> =
            throw RuntimeException("unused")

        override suspend fun getRippleBalance(url: String): Response<RippleBalance> =
            throw RuntimeException("unused")
    }

    private class InMemoryMarketDao : MarketDao {
        val stored = linkedMapOf<String, CoinPrices>()
        override fun observeCoinPrices(): Flow<List<CoinPrices>> = flowOf(stored.values.toList())
        override suspend fun getAllCoinPricesList(): List<CoinPrices> = stored.values.toList()
        override suspend fun getCoinPricesFor(coinCode: String): CoinPrices? = stored[coinCode]
        override suspend fun addCoinPrices(prices: List<CoinPrices>) {
            prices.forEach { stored[it.coinCode] = it }
        }
    }

    private class InMemoryWalletDao : WalletDao {
        val wallets = mutableListOf<Wallet>()
        override fun observeWallets(): Flow<List<Wallet>> = flowOf(wallets.toList())
        override suspend fun getAllWallets(): List<Wallet> = wallets.toList()
        override suspend fun addNewWallet(wallet: Wallet): Long {
            wallets.add(wallet)
            return wallets.size.toLong()
        }

        override suspend fun checkIfNameDuplicate(displayName: String): Wallet? =
            wallets.firstOrNull { it.displayName.equals(displayName, ignoreCase = true) }

        override suspend fun checkIfAddressDuplicate(walletAddress: String): Wallet? =
            wallets.firstOrNull { it.walletAddress == walletAddress }

        override suspend fun updateWallets(wallets: List<Wallet>): Int = wallets.size
        override suspend fun updateWallet(wallet: Wallet): Int = 1
        override suspend fun deleteWallet(wallet: Wallet): Int =
            if (wallets.remove(wallet)) 1 else 0

        override fun observeWalletByName(displayName: String): Flow<Wallet?> =
            flowOf(wallets.firstOrNull { it.displayName == displayName })

        override suspend fun getWalletByName(displayName: String): Wallet? =
            wallets.firstOrNull { it.displayName == displayName }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun addWallet_returnsSuccessPromptly_whenMarketRefreshHangs() = runTest {
        val marketDao = InMemoryMarketDao()
        val walletDao = InMemoryWalletDao()
        val marketRepository = MarketRepository(
            marketDao = marketDao,
            marketApi = FailingMarketApi(),
            coinGeckoApi = HungCoinGeckoApi(),
            // Mocked: getResources() returns null -> repository falls back to USD-only.
            context = mock(Context::class.java)
        )
        val settingsRepository = mock(SettingsRepository::class.java)
        `when`(settingsRepository.getDefaultCurrency()).thenReturn("USD")
        val body = CypherAddressBalance().apply {
            balance = "5000000000" // 50 BTC in satoshi
            totalReceived = "5000000000"
            totalSent = "0"
            unconfirmedBalance = "0"
        }
        val repository = WalletRepository(
            walletDao = walletDao,
            marketDao = marketDao,
            marketRepository = marketRepository,
            walletApi = FakeWalletApi(body),
            settingsRepository = settingsRepository,
            notificationHelper = mock(NotificationHelper::class.java),
            connectivity = mock(Connectivity::class.java)
        )

        val start = currentTime
        // Public BTC genesis address (also used by the AddWallet UI test).
        val result = repository.addWallet(
            "Satoshi",
            "BTC",
            "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
        )
        val elapsed = currentTime - start

        assertTrue("expected Success, got $result", result is AddWalletResult.Success)
        assertTrue(
            "addWallet took ${elapsed}ms awaiting a hung market refresh; " +
                "post-insert refresh must stay time-bounded",
            elapsed < 120_000
        )
        assertTrue(
            "wallet was not persisted",
            walletDao.wallets.any { it.displayName == "Satoshi" }
        )
    }
}
