package com.codit.cryptowatchwallet.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class BCHAddressBalance {
    @SerializedName("addrStr") @Expose var addrStr: String? = null
    @SerializedName("balance") @Expose var balance: String? = null
    @SerializedName("balanceSat") @Expose var balanceSat: String? = null
    @SerializedName("totalReceived") @Expose var totalReceived: String? = null
    @SerializedName("totalReceivedSat") @Expose var totalReceivedSat: String? = null
    @SerializedName("totalSent") @Expose var totalSent: String? = null
    @SerializedName("totalSentSat") @Expose var totalSentSat: String? = null
    @SerializedName("unconfirmedBalance") @Expose var unconfirmedBalance: String? = null
    @SerializedName("unconfirmedBalanceSat") @Expose var unconfirmedBalanceSat: String? = null
    @SerializedName("unconfirmedTxApperances") @Expose var unconfirmedTxApperances: Long = 0L
    @SerializedName("txApperances") @Expose var txApperances: Long = 0L
    @SerializedName("transactions") @Expose var transactions: List<String>? = null

    fun getWalletBalance(coinCode: String): Balance {
        return Balance(
            balance ?: "0",
            totalReceived ?: "0",
            totalSent ?: "0",
            unconfirmedBalance ?: "0",
            txApperances,
            unconfirmedTxApperances
        )
    }
}
