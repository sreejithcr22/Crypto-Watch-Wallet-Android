package com.codit.cryptowatchwallet.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;

import java.util.List;

/**
 * Created by Sreejith on 23-Nov-17.
 */

public class MarketViewModel extends AndroidViewModel {


    public LiveData<List<CoinPrices>> getAllCoinPrices() {
        return allCoinPrices;
    }

    private final LiveData<List<CoinPrices>> allCoinPrices;


    private AppDatabase appDatabase;

    public MarketViewModel(@NonNull Application application) {
        super(application);

        appDatabase = AppDatabase.getDatabase(this.getApplication());

        allCoinPrices = appDatabase.marketDao().getAllCoinPrices();


    }
}
