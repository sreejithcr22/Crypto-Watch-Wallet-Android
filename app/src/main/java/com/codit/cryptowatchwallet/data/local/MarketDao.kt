package com.codit.cryptowatchwallet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.codit.cryptowatchwallet.model.CoinPrices
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("Select * from CoinPrices")
    fun observeCoinPrices(): Flow<List<CoinPrices>>

    @Query("Select * from CoinPrices")
    suspend fun getAllCoinPricesList(): List<CoinPrices>

    @Query("Select * from CoinPrices where coinCode=:coinCode")
    suspend fun getCoinPricesFor(coinCode: String): CoinPrices?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCoinPrices(prices: List<CoinPrices>)
}
