package com.codit.cryptowatchwallet.model

import com.codit.cryptowatchwallet.util.Coin
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CypherAddressBalance {
    @SerializedName("address") @Expose var address: String? = null
    @SerializedName("total_received") @Expose var totalReceived: String? = null
    @SerializedName("total_sent") @Expose var totalSent: String? = null
    @SerializedName("balance") @Expose var balance: String? = null
    @SerializedName("unconfirmed_balance") @Expose var unconfirmedBalance: String? = null
    @SerializedName("final_balance") @Expose var finalBalance: String? = null
    @SerializedName("n_tx") @Expose var nTx: Long = 0L
    @SerializedName("unconfirmed_n_tx") @Expose var unconfirmedNTx: Long = 0L
    @SerializedName("final_n_tx") @Expose var finalNTx: Long = 0L

    fun getWalletBalance(coinCode: String): Balance {
        val coinBalance = Coin.convertToBase(balance ?: "0", coinCode)
        val totalReceivedBase = Coin.convertToBase(totalReceived ?: "0", coinCode)
        val totalSentBase = Coin.convertToBase(totalSent ?: "0", coinCode)
        val unconfirmedBase = Coin.convertToBase(unconfirmedBalance ?: "0", coinCode)
        return Balance(coinBalance, totalReceivedBase, totalSentBase, unconfirmedBase, nTx, unconfirmedNTx)
    }
}
