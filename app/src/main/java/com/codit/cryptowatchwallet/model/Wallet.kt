package com.codit.cryptowatchwallet.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["walletAddress"], unique = true)])
data class Wallet(
    @PrimaryKey
    var displayName: String = "",
    var coinCode: String = "",
    var walletAddress: String = "",
    @Embedded
    var balance: Balance? = null,
    var coinWorth: String = ""
) {
    companion object {
        const val EXTRA_WALLET_NAME = "wallet_name"
        const val WALLET_ADDRESS = "wallet_address"
        const val WALLET_COIN_CODE = "wallet_coin_code"
    }
}
