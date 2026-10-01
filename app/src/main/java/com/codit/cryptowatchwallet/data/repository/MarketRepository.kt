package com.codit.cryptowatchwallet.data.repository

import android.content.Context
import android.util.Log
import com.codit.cryptowatchwallet.BuildConfig
import com.codit.cryptowatchwallet.R
import com.codit.cryptowatchwallet.data.local.MarketDao
import com.codit.cryptowatchwallet.data.remote.CoinGeckoApi
import com.codit.cryptowatchwallet.data.remote.MarketApi
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.util.Coin
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Dual-source market prices, mirroring Crypto-Converter-Android
 * data/repository/MarketRepositoryImpl: primary = CoinGecko keyless API
 * (no key), fallback = CryptoCompare min-api.cryptocompare.com + api_key.
 */
@Singleton
class MarketRepository @Inject constructor(
    private val marketDao: MarketDao,
    private val marketApi: MarketApi,
    private val coinGeckoApi: CoinGeckoApi,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "wallet"
        private const val COINGECKO_SYMBOLS_PER_CALL = 40
        private const val CRYPTOCOMPARE_FSYS_LIMIT = 50
        private const val CRYPTOCOMPARE_TSYS_LIMIT = 20
    }

    private val gson = Gson()

    fun observeCoinPrices(): Flow<List<CoinPrices>> = marketDao.observeCoinPrices()

    suspend fun getCoinRate(coinCode: String, currency: String): Double? {
        return try {
            marketDao.getCoinPricesFor(coinCode)?.prices?.get(currency)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Fetches latest market prices and persists them.
     * Returns true on success, false otherwise (never throws for network errors).
     */
    suspend fun refreshMarket(): Boolean {
        return try {
            val cryptoCodes = Coin.coinsData.keys.toList()
            val fiatCodes = context.resources.getStringArray(R.array.currencies).toList()
            val apiKey = BuildConfig.CRYPTOCOMPARE_API_KEY.trim().ifEmpty { null }

            val matrix = fetchCoinGeckoMatrix(cryptoCodes, fiatCodes)
                .ifEmpty { fetchCryptoCompareMatrix(cryptoCodes, fiatCodes, apiKey) }
            if (matrix.isEmpty()) {
                Log.d(TAG, "refreshMarket: no prices returned")
                return false
            }
            marketDao.addCoinPrices(matrix.map { (code, prices) -> CoinPrices(code, prices) })
            Log.d(TAG, "refreshMarket: loaded ${matrix.size} coins")
            true
        } catch (e: Exception) {
            Log.d(TAG, "refreshMarket exception: $e")
            false
        }
    }

    /** CoinGecko symbol-based simple/price + exchange_rates FX derivation. */
    private suspend fun fetchCoinGeckoMatrix(
        cryptoCodes: List<String>,
        fiatCodes: List<String>
    ): LinkedHashMap<String, HashMap<String, Double>> {
        val result = LinkedHashMap<String, HashMap<String, Double>>()
        if (cryptoCodes.isEmpty()) return result
        val cryptoSet = cryptoCodes.toHashSet()

        val vsToOriginal = LinkedHashMap<String, String>()
        for (fiat in fiatCodes) {
            vsCodeFor(fiat)?.let { vsToOriginal[it] = fiat }
        }
        val vsCurrencies = ArrayList(vsToOriginal.keys)
        if (!vsCurrencies.contains("btc")) vsCurrencies.add("btc")
        val vsParam = vsCurrencies.joinToString(",")

        val lower = cryptoCodes.map { it.lowercase() }
        val chunks = lower.chunked(COINGECKO_SYMBOLS_PER_CALL)
        for (chunk in chunks) {
            try {
                val resp = coinGeckoApi.getPrices(chunk.joinToString(","), vsParam)
                for ((key, prices) in resp) {
                    val sym = key.uppercase()
                    if (!cryptoSet.contains(sym)) continue
                    val map = result.getOrPut(sym) { HashMap() }
                    for ((priceKey, value) in prices) {
                        if (value <= 0) continue
                        val k = priceKey.lowercase()
                        if (k == "btc") {
                            map["BTC"] = value
                        } else {
                            val original = vsToOriginal[k] ?: continue
                            map[original] = value
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "CoinGecko chunk failed: $e")
            }
        }

        // FX derivation for fiats simple/price doesn't quote (e.g. ZMW).
        try {
            val ratesResp = coinGeckoApi.getExchangeRates()
            val rates = ratesResp.body()?.asJsonObject?.getAsJsonObject("rates")
            if (ratesResp.isSuccessful && rates != null) {
                val rateByCode = HashMap<String, Double>()
                for ((code, entry) in rates.entrySet()) {
                    try {
                        val v = entry.asJsonObject.get("value").asDouble
                        if (v > 0) rateByCode[code.lowercase()] = v
                    } catch (_: Exception) {
                    }
                }
                val usdRate = rateByCode["usd"] ?: 0.0
                if (usdRate > 0) {
                    for (map in result.values) {
                        val usd = map["USD"] ?: continue
                        if (usd <= 0) continue
                        for (fiat in fiatCodes) {
                            if (map.containsKey(fiat)) continue
                            val r = rateByCode[fiat.lowercase()] ?: continue
                            if (r > 0) map[fiat] = usd * (r / usdRate)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "CoinGecko exchange_rates failed: $e")
        }
        return result
    }

    /** Legacy CryptoCompare full-matrix fetch, batched like the converter repo. */
    private suspend fun fetchCryptoCompareMatrix(
        cryptoCodes: List<String>,
        fiatCodes: List<String>,
        apiKey: String?
    ): LinkedHashMap<String, HashMap<String, Double>> {
        val result = LinkedHashMap<String, HashMap<String, Double>>()
        if (cryptoCodes.isEmpty()) return result

        val batches = ArrayList<Pair<String, String>>()
        var fsysStart = 0
        while (fsysStart < cryptoCodes.size) {
            val fsysEnd = minOf(fsysStart + CRYPTOCOMPARE_FSYS_LIMIT, cryptoCodes.size)
            val fsys = cryptoCodes.subList(fsysStart, fsysEnd).joinToString(",")
            var toStart = 0
            while (toStart < fiatCodes.size) {
                val toEnd = minOf(toStart + CRYPTOCOMPARE_TSYS_LIMIT, fiatCodes.size)
                batches.add(fsys to fiatCodes.subList(toStart, toEnd).joinToString(","))
                toStart += CRYPTOCOMPARE_TSYS_LIMIT
            }
            fsysStart += CRYPTOCOMPARE_FSYS_LIMIT
        }

        for ((fsys, tsyms) in batches) {
            try {
                val response = marketApi.getAllCoinPrices(fsys, tsyms, apiKey)
                if (!response.isSuccessful) {
                    Log.d(TAG, "CryptoCompare batch failed: code=${response.code()}")
                    continue
                }
                val parsed = parseBodyOrNull(response.body()) ?: continue
                for ((code, prices) in parsed) {
                    result.getOrPut(code) { HashMap() }.putAll(prices)
                }
            } catch (e: Exception) {
                Log.d(TAG, "CryptoCompare batch failed: $e")
            }
        }
        return result
    }

    private fun vsCodeFor(fiatCode: String): String? {
        if (fiatCode == "RUR") return "rub"
        return fiatCode.lowercase()
    }

    private fun parseBodyOrNull(body: JsonElement?): LinkedHashMap<String, HashMap<String, Double>>? {
        if (body == null || body.isJsonNull) return null
        return try {
            if (body.isJsonObject && body.asJsonObject.has("Response")) {
                val status = body.asJsonObject.get("Response").asString
                if (status.equals("Error", ignoreCase = true)) {
                    val msg = if (body.asJsonObject.has("Message")) {
                        body.asJsonObject.get("Message").asString
                    } else "unknown error"
                    Log.d(TAG, "CryptoCompare API error: $msg")
                    return null
                }
            }
            val type = object : TypeToken<LinkedHashMap<String, HashMap<String, Double>>>() {}.type
            gson.fromJson(body, type)
        } catch (e: Exception) {
            Log.d(TAG, "parseBodyOrNull failed: $e")
            null
        }
    }
}
