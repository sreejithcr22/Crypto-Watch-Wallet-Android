package com.codit.cryptowatchwallet.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;

import java.util.List;

/**
 * Created by Sreejith on 25-Nov-17.
 */

public class WalletViewModel extends AndroidViewModel {

    private AppDatabase appDatabase;

    public LiveData<List<Wallet>> getAllWalletsLive() {
        return allWalletsLive;
    }

    private final LiveData<List<Wallet>> allWalletsLive;

    public WalletViewModel(@NonNull Application application) {
        super(application);
        appDatabase = AppDatabase.getDatabase(this.getApplication());
        allWalletsLive = appDatabase.walletDao().getAllWalletsLive();
    }

}
