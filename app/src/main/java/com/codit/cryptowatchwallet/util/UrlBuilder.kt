package com.codit.cryptowatchwallet.util

import android.util.Log

object UrlBuilder {
    fun buildCoinList(): String {
        val coinList = Coin.coinsData.keys.joinToString(",")
        Log.d("url", coinList)
        return coinList
    }

    fun buildCurrencyList(currencyArray: Array<String>): String {
        Log.d("url", "buildCurrencyList: ${currencyArray.size}")
        val currencyList = currencyArray.joinToString(",")
        Log.d("url", currencyList)
        return currencyList
    }
}
