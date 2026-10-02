package com.codit.cryptowatchwallet.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.model.Wallet

/**
 * NOTE ON `version`
 *
 * v1 was the schema shipped by the published Play builds (Room 1.0.0, Java entities):
 * every column was NOT NULL, including the ones coming from the embedded `Balance`.
 *
 * The Compose/Kotlin rewrite declared the embedded balance nullable
 * (`@Embedded var balance: Balance?`), so Room now generates *nullable* columns for
 * `coinBalance`, `totalReceived`, `totalSent`, `unconfirmedBalance`, `transactionCount`
 * and `unConfirmedTransactionCount`.
 *
 * The version number was never bumped, so Room saw "version 1 -> version 1" and therefore
 * never ran `onUpgrade` - `fallbackToDestructiveMigration()` was never triggered either.
 * On an existing `app_db` (created by the published app) Room validated the schema,
 * found the mismatch and threw
 *
 *     IllegalStateException: Pre-packaged database has an invalid schema: Wallet(...)
 *
 * from the very first wallets query, i.e. on app launch - a hard crash for every user
 * upgrading from the published build (fresh installs create the current schema and are
 * fine, which is why it never showed up on a clean emulator install).
 *
 * v2 therefore rebuilds `Wallet` with the current schema and copies the user's wallets
 * over, so upgrading users keep their addresses instead of losing them.
 */
@Database(entities = [CoinPrices::class, Wallet::class], version = AppDatabase.VERSION, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun marketDao(): MarketDao
    abstract fun walletDao(): WalletDao

    companion object {
        const val VERSION = 2
        const val NAME = "app_db"

        /** Rebuilds `Wallet` with the current (nullable embedded balance) schema. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `Wallet_new` (" +
                        "`displayName` TEXT NOT NULL, " +
                        "`coinCode` TEXT NOT NULL, " +
                        "`walletAddress` TEXT NOT NULL, " +
                        "`coinWorth` TEXT NOT NULL, " +
                        "`coinBalance` TEXT, " +
                        "`totalReceived` TEXT, " +
                        "`totalSent` TEXT, " +
                        "`unconfirmedBalance` TEXT, " +
                        "`transactionCount` INTEGER, " +
                        "`unConfirmedTransactionCount` INTEGER, " +
                        "PRIMARY KEY(`displayName`))"
                )
                db.execSQL(
                    "INSERT OR REPLACE INTO `Wallet_new` (" +
                        "`displayName`,`coinCode`,`walletAddress`,`coinWorth`,`coinBalance`," +
                        "`totalReceived`,`totalSent`,`unconfirmedBalance`,`transactionCount`," +
                        "`unConfirmedTransactionCount`) " +
                        "SELECT `displayName`,`coinCode`,`walletAddress`,`coinWorth`,`coinBalance`," +
                        "`totalReceived`,`totalSent`,`unconfirmedBalance`,`transactionCount`," +
                        "`unConfirmedTransactionCount` FROM `Wallet`"
                )
                db.execSQL("DROP TABLE `Wallet`")
                db.execSQL("ALTER TABLE `Wallet_new` RENAME TO `Wallet`")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_Wallet_walletAddress` " +
                        "ON `Wallet` (`walletAddress`)"
                )
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}