package com.codit.cryptowatchwallet.orm;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.codit.cryptowatchwallet.model.CoinPrices;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Dao
public interface MarketDao {

    @Query("Select * from CoinPrices")
    LiveData<List<CoinPrices>> getAllCoinPrices();

    @Query("Select * from CoinPrices where coinCode=:coinCode")
    CoinPrices getCoinPricesFor(String coinCode);

    @Insert(onConflict = REPLACE)
    void addCoinPrices(List<CoinPrices> prices);


}
