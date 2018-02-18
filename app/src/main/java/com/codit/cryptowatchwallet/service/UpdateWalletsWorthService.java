package com.codit.cryptowatchwallet.service;

import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.model.Wallet;

import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.List;


public class UpdateWalletsWorthService extends BaseService {

    private SharedPreferenceManager helper;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            helper=new SharedPreferenceManager(this);
            Log.d("wallet", "UpdateWalletsWorthService ");
            initializeDB();
            String currencyCode=Currency.currencyArray[helper.getDefaultCurrency()];
            Boolean startNotificationService=intent.getBooleanExtra(BaseService.EXTRA_SHOULD_START_NOTIFICATION,false);
            if(currencyCode!=null)
            {
                updateCoinsWorth(currencyCode);

                if(startNotificationService)
                {
                    startService(new Intent(this,NotificationService.class));
                }
            }
        }
    }


    void updateCoinsWorth(String currencyCode)
    {
        List<Wallet> wallets=getAllWallets();

        if (wallets!=null)
        {
            for (Wallet wallet:wallets) {
                wallet.setCoinWorth(Coin.calculateCoinWorth(wallet.getBalance().getCoinBalance(),getCoinRateFromDB(wallet.getCoinCode(), currencyCode),currencyCode));
            }

            updateWalletsDB(wallets);
        }
        else {
            Log.d("wallets", "updateCoinsWorth: wallets null");
        }
    }

}
