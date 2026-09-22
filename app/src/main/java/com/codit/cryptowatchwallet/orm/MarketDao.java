package com.codit.cryptowatchwallet.orm;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.codit.cryptowatchwallet.model.CoinPrices;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

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
