package com.codit.cryptowatchwallet.model

import com.codit.cryptowatchwallet.util.Coin
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class RippleBalance {
    @SerializedName("result") @Expose var result: String? = null
    @SerializedName("ledger_index") @Expose var ledgerIndex: Int = 0
    @SerializedName("limit") @Expose var limit: Int = 0
    @SerializedName("balances") @Expose var walletBalances: List<WalletBalance>? = null

    fun getWalletBalance(coinCode: String): Balance {
        var balance = "0"
        walletBalances?.forEach {
            if (it.currency == Coin.XRP) {
                balance = it.value ?: "0"
                return@forEach
            }
        }
        return Balance(balance, "-", "-", "-", -1, -1)
    }

    class WalletBalance {
        @SerializedName("currency") @Expose var currency: String? = null
        @SerializedName("value") @Expose var value: String? = null
        @SerializedName("counterparty") @Expose var counterparty: String? = null
    }
}
