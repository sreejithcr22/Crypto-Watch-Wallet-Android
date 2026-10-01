package com.codit.cryptowatchwallet.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MarketApi {
    @GET("data/pricemulti")
    suspend fun getAllCoinPrices(
        @Query("fsyms") coinsList: String,
        @Query("tsyms") currencyList: String,
        @Query("api_key") apiKey: String?
    ): Response<LinkedHashMap<String, HashMap<String, Double>>>
}
