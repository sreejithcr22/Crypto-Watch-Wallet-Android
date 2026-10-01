package com.codit.cryptowatchwallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.model.Wallet

@Database(entities = [CoinPrices::class, Wallet::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao
    abstract fun walletDao(): WalletDao
}
