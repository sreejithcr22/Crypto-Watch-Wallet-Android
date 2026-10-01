package com.codit.cryptowatchwallet.data.remote

import com.codit.cryptowatchwallet.model.BCHAddressBalance
import com.codit.cryptowatchwallet.model.CypherAddressBalance
import com.codit.cryptowatchwallet.model.RippleBalance
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface WalletApi {
    @GET
    suspend fun getCypherAddressBalance(@Url url: String): Response<CypherAddressBalance>

    @GET
    suspend fun getBCHAddressBalance(@Url url: String): Response<BCHAddressBalance>

    @GET
    suspend fun getRippleBalance(@Url url: String): Response<RippleBalance>
}
