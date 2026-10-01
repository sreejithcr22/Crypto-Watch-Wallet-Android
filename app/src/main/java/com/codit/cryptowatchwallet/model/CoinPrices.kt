package com.codit.cryptowatchwallet.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
data class CoinPrices(
    @PrimaryKey
    var coinCode: String = "",
    var jsonPricesString: String = "{}"
) {
    @Ignore
    var prices: HashMap<String, Double> = hashMapOf()
        get() {
            if (field.isEmpty() && jsonPricesString.isNotEmpty()) {
                field = try {
                    val type = object : TypeToken<HashMap<String, Double>>() {}.type
                    Gson().fromJson<HashMap<String, Double>>(jsonPricesString, type) ?: hashMapOf()
                } catch (_: Exception) {
                    hashMapOf()
                }
            }
            return field
        }

    constructor(coinCode: String, prices: HashMap<String, Double>) : this(
        coinCode = coinCode,
        jsonPricesString = Gson().toJson(prices)
    ) {
        this.prices = prices
    }
}
