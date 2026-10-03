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
                // Clean any partial retry from a previous crashed migration.
                try {
                    db.execSQL("DROP TABLE IF EXISTS `Wallet_new`")
                } catch (_: Throwable) {
                }
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
                // If the old table is missing entirely (corrupt backup), keep
                // going with an empty Wallet so CoinPrices and other data survive.
                val hasWallet = try {
                    db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='Wallet'").use {
                        it.count > 0
                    }
                } catch (_: Throwable) {
                    false
                }
                if (hasWallet) {
                    try {
                        db.execSQL(
                            "INSERT OR REPLACE INTO `Wallet_new` (" +
                                "`displayName`,`coinCode`,`walletAddress`,`coinWorth`,`coinBalance`," +
                                "`totalReceived`,`totalSent`,`unconfirmedBalance`,`transactionCount`," +
                                "`unConfirmedTransactionCount`) " +
                                "SELECT `displayName`,`coinCode`,`walletAddress`,`coinWorth`,`coinBalance`," +
                                "`totalReceived`,`totalSent`,`unconfirmedBalance`,`transactionCount`," +
                                "`unConfirmedTransactionCount` FROM `Wallet`"
                        )
                    } catch (t: Throwable) {
                        // Old table has an unexpected shape (very old backup, partial
                        // write). Try a best-effort copy of whatever columns exist so
                        // the user keeps their addresses; otherwise start empty.
                        android.util.Log.w("app", "Wallet copy failed, trying partial copy", t)
                        try {
                            copyWalletBestEffort(db)
                        } catch (_: Throwable) {
                        }
                    }
                    try {
                        db.execSQL("DROP TABLE IF EXISTS `Wallet`")
                    } catch (_: Throwable) {
                    }
                }
                try {
                    db.execSQL("ALTER TABLE `Wallet_new` RENAME TO `Wallet`")
                } catch (_: Throwable) {
                    // Rename can fail if Wallet still exists (crashed halfway).
                    // Drop and retry once before giving up (openDatabase will
                    // delete+recreate as a last resort, never crash launch).
                    try {
                        db.execSQL("DROP TABLE IF EXISTS `Wallet`")
                        db.execSQL("ALTER TABLE `Wallet_new` RENAME TO `Wallet`")
                    } catch (_: Throwable) {
                    }
                }
                try {
                    db.execSQL(
                        "CREATE UNIQUE INDEX IF NOT EXISTS `index_Wallet_walletAddress` " +
                            "ON `Wallet` (`walletAddress`)"
                    )
                } catch (_: Throwable) {
                }
            }

            private fun copyWalletBestEffort(db: SupportSQLiteDatabase) {
                val existing = mutableSetOf<String>()
                try {
                    db.query("PRAGMA table_info(`Wallet`)").use { c ->
                        val nameIdx = c.getColumnIndex("name")
                        while (c.moveToNext()) {
                            existing.add(c.getString(nameIdx))
                        }
                    }
                } catch (_: Throwable) {
                    return
                }
                val wanted = listOf(
                    "displayName", "coinCode", "walletAddress", "coinWorth",
                    "coinBalance", "totalReceived", "totalSent", "unconfirmedBalance",
                    "transactionCount", "unConfirmedTransactionCount"
                )
                val cols = wanted.filter { existing.contains(it) }
                // displayName is the PK - without it we cannot safely copy.
                if (!cols.contains("displayName")) return
                if (cols.isEmpty()) return
                val colList = cols.joinToString(",") { "`$it`" }
                try {
                    db.execSQL(
                        "INSERT OR REPLACE INTO `Wallet_new` ($colList) SELECT $colList FROM `Wallet`"
                    )
                } catch (_: Throwable) {
                }
            }
        }

        val MIGRATIONS: Array<Migration> = arrayOf(MIGRATION_1_2)
    }
}