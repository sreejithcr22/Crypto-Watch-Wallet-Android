package com.codit.cryptowatchwallet.util

import java.math.BigDecimal
import java.math.RoundingMode

object Coin {
    const val BTC = "BTC"
    const val ETH = "ETH"
    const val BCH = "BCH"
    const val DASH = "DASH"
    const val LTC = "LTC"
    const val DOGE = "DOGE"
    const val XRP = "XRP"
    const val PRICE_NOT_AVAILABLE = "Not available"

    private const val SATOSHI = 1e8
    private const val WEI = 1e18

    val coinsData: LinkedHashMap<String, String> = linkedMapOf(
        "BTC" to "Bitcoin",
        "ETH" to "Ethereum",
        "XRP" to "Ripple",
        "BCH" to "Bitcoin Cash",
        "LTC" to "Litecoin",
        "ADA" to "Cardano",
        "XLM" to "Stellar",
        "NEO" to "NEO",
        "EOS" to "EOS",
        "IOTA" to "IOTA",
        "DASH" to "DASH",
        "XEM" to "NEM",
        "XMR" to "Monero",
        "LSK" to "LISK",
        "ETC" to "Ethereum Classic",
        "VEN" to "Ve Chain",
        "TRX" to "TRON",
        "BTG" to "Bitcoin Gold",
        "USDT" to "Tether",
        "OMG" to "OmiseGO",
        "ICX" to "ICON",
        "ZEC" to "Zcash",
        "XVG" to "Verge",
        "BCN" to "Bytecoin",
        "PPT" to "Populous",
        "STRAT" to "Stratis",
        "SC" to "Siacoin",
        "WAVES" to "Waves",
        "SNT" to "Status",
        "MKR" to "Maker",
        "BTS" to "BitShares",
        "VERI" to "Veritaseum",
        "AE" to "Aeternity",
        "WTC" to "Waltonchain",
        "ZCL" to "ZClassic",
        "DCR" to "Decred",
        "REP" to "Augur",
        "DGD" to "DigixDAO",
        "ARDR" to "Ardor",
        "ETN" to "Electroneum",
        "KMD" to "Komodo",
        "GAS" to "Gas",
        "ARK" to "Ark",
        "BTX" to "Bitcore",
        "VTC" to "Vertcoin",
        "DOGE" to "DogeCoin"
    )

    fun convertToBase(units: String, coinCode: String): String {
        val smallestUnit = when (coinCode) {
            BTC, BCH, DASH, LTC, DOGE, XRP -> SATOSHI
            ETH -> WEI
            else -> 1.0
        }
        return try {
            val smallUnit = BigDecimal(smallestUnit.toString())
            val unitsDecimal = BigDecimal(units)
            val result = unitsDecimal.divide(smallUnit, 18, RoundingMode.HALF_EVEN).stripTrailingZeros()
            if (result.compareTo(BigDecimal.ZERO) == 0) "0" else result.toPlainString()
        } catch (_: Exception) {
            units
        }
    }

    fun calculateCoinWorth(coinCount: String, coinRate: Double, currencyCode: String): String {
        if (coinRate == -1.0) return PRICE_NOT_AVAILABLE
        return try {
            val rate = BigDecimal(coinRate.toString()).multiply(BigDecimal(coinCount))
            val roundOff = rate.setScale(2, RoundingMode.HALF_EVEN)
            "${roundOff.toPlainString()} $currencyCode"
        } catch (_: Exception) {
            PRICE_NOT_AVAILABLE
        }
    }

    fun worthAsDouble(coinWorth: String?): Double {
        if (coinWorth.isNullOrBlank()) return 0.0
        return try {
            coinWorth.substringBefore(" ").toDouble()
        } catch (_: Exception) {
            0.0
        }
    }

    /**
     * Formats any displayed amount to exactly 2 decimal points.
     * Non-numeric values (e.g. "-", "Not available") are returned unchanged.
     */
    fun formatTwoDecimals(value: String?): String {
        if (value == null) return PRICE_NOT_AVAILABLE
        return try {
            BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
        } catch (_: Exception) {
            value
        }
    }

    fun formatTwoDecimals(value: Double): String {
        return try {
            BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_EVEN).toPlainString()
        } catch (_: Exception) {
            value.toString()
        }
    }

    fun getCoinName(coinCode: String): String = coinsData[coinCode] ?: coinCode
}
