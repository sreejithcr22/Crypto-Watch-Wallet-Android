package com.codit.cryptowatchwallet.orm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.codit.cryptowatchwallet.model.Wallet;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

/**
 * Created by Sreejith on 24-Nov-17.
 */
@Dao
public interface WalletDao {

    @Query("Select * from Wallet")
    LiveData<List<Wallet>> getAllWalletsLive();

    @Query("Select * from Wallet")
    List<Wallet> getAllWallets();

    @Insert
    long addNewWallet(Wallet wallets);

    @Query("Select * from Wallet where displayName=:displayName COLLATE NOCASE")
    Wallet checkIfNameDuplicate(String displayName);

    @Query("Select * from Wallet where walletAddress=:walletAddress")
    Wallet checkIfAddressDuplicate(String walletAddress);

    @Update
    int updateWallets(List<Wallet> wallets);

    @Delete
    int deleteWallet(Wallet wallet);

    @Query("Select * from Wallet where displayName=:displayName")
    Wallet getWalletByName(String displayName);

}
