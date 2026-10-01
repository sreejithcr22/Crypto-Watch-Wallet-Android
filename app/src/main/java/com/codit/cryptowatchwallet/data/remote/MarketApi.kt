package com.codit.cryptowatchwallet.data.remote

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Uses JsonElement so CryptoCompare error envelopes
 * ({"Response":"Error","Message":...}) don't crash Gson parsing.
 * Mirrors Crypto-Converter-Android data/remote/MarketApiService.
 */
interface MarketApi {
    @GET("data/pricemulti")
    suspend fun getAllCoinPrices(
        @Query("fsyms") coinsList: String,
        @Query("tsyms") currencyList: String,
        @Query("api_key") apiKey: String?
    ): Response<JsonElement>
}
