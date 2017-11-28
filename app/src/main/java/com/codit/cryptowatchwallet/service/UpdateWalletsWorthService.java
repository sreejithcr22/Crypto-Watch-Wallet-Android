package com.codit.cryptowatchwallet.service;

import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.model.Wallet;

import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.List;


public class UpdateWalletsWorthService extends BaseService {

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("wallet", "updateworth: ");
            initializeDB();
            String currencyCode=intent.getStringExtra(Currency.EXTRA_DATA_CURRENCY_CODE);
            if(currencyCode!=null)
            {
                updateCoinsWorth(currencyCode);
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
