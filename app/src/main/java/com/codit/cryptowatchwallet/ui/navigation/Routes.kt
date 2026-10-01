package com.codit.cryptowatchwallet.ui.navigation

object Routes {
    const val WALLETS = "wallets"
    const val MARKET = "market"
    const val SETTINGS = "settings"
    const val ADD_WALLET = "add_wallet"
    const val WALLET_DETAILS = "wallet_details/{walletName}"

    fun walletDetails(name: String) = "wallet_details/${java.net.URLEncoder.encode(name, "UTF-8")}"
}
