package com.codit.cryptowatchwallet.orm;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.model.Wallet;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Database(entities = {CoinPrices.class, Wallet.class}, version = 1)

public abstract class AppDatabase extends RoomDatabase{

    private static AppDatabase instance;

    public static AppDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "app_db")
                            .build();
        }
        return instance;
    }

    public abstract MarketDao marketDao();
    public abstract WalletDao walletDao();
}
