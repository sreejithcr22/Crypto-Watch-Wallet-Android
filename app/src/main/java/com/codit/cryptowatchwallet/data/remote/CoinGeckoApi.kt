package com.codit.cryptowatchwallet.data.remote

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * CoinGecko keyless public API (no key required).
 * Mirrors Crypto-Converter-Android data/remote/CoinGeckoApiService.
 * Primary price source; CryptoCompare remains as fallback.
 * Docs: https://docs.coingecko.com/docs/keyless-public-api
 */
interface CoinGeckoApi {

    @GET("simple/price")
    suspend fun getPrices(
        @Query("symbols") symbols: String,
        @Query("vs_currencies") vsCurrencies: String,
        @Query("precision") precision: String = "full"
    ): Map<String, Map<String, Double>>

    @GET("exchange_rates")
    suspend fun getExchangeRates(): Response<JsonElement>
}
