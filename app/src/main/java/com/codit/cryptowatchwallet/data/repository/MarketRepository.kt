package com.codit.cryptowatchwallet.data.repository

import android.content.Context
import android.util.Log
import com.codit.cryptowatchwallet.BuildConfig
import com.codit.cryptowatchwallet.R
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.remote.MarketApi
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.util.UrlBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepository @Inject constructor(
    private val marketDao: MarketDao,
    private val marketApi: MarketApi,
    @ApplicationContext private val context: Context
) {
    fun observeCoinPrices(): Flow<List<CoinPrices>> = marketDao.observeCoinPrices()

    suspend fun getCoinRate(coinCode: String, currency: String): Double? {
        return try {
            marketDao.getCoinPricesFor(coinCode)?.prices?.get(currency)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetches latest market prices from CryptoCompare and persists them.
     * Returns true on success, false otherwise (never throws for network errors).
     */
    suspend fun refreshMarket(): Boolean {
        return try {
            val apiKey = BuildConfig.CRYPTOCOMPARE_API_KEY.trim().ifEmpty { null }
            val currencies = context.resources.getStringArray(R.array.currencies)
            val response = marketApi.getAllCoinPrices(
                UrlBuilder.buildCoinList(),
                UrlBuilder.buildCurrencyList(currencies),
                apiKey
            )
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.map { (code, prices) ->
                    CoinPrices(code, prices)
                }
                marketDao.addCoinPrices(list)
                true
            } else {
                Log.d("wallet", "refreshMarket failed: code=${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.d("wallet", "refreshMarket exception: $e")
            false
        }
    }
}
