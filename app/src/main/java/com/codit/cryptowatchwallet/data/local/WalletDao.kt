package com.codit.cryptowatchwallet.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.codit.cryptowatchwallet.model.Wallet
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("Select * from Wallet")
    fun observeWallets(): Flow<List<Wallet>>

    @Query("Select * from Wallet")
    suspend fun getAllWallets(): List<Wallet>

    @Insert
    suspend fun addNewWallet(wallet: Wallet): Long

    @Query("Select * from Wallet where displayName=:displayName COLLATE NOCASE")
    suspend fun checkIfNameDuplicate(displayName: String): Wallet?

    @Query("Select * from Wallet where walletAddress=:walletAddress")
    suspend fun checkIfAddressDuplicate(walletAddress: String): Wallet?

    @Update
    suspend fun updateWallets(wallets: List<Wallet>): Int

    @Update
    suspend fun updateWallet(wallet: Wallet): Int

    @Delete
    suspend fun deleteWallet(wallet: Wallet): Int

    @Query("Select * from Wallet where displayName=:displayName")
    fun observeWalletByName(displayName: String): Flow<Wallet?>

    @Query("Select * from Wallet where displayName=:displayName")
    suspend fun getWalletByName(displayName: String): Wallet?
}
