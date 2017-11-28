package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.util.Currency;

import java.util.ArrayList;
import java.util.List;


public class RefreshWalletService extends BaseService {

    PreferenceHelper helper;
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null)
        {
            helper=new PreferenceHelper(this.getApplicationContext());
            Log.d("wallet", "refresh: ");
            initializeDB();
            refreshWallets();
            updateWalletsWorth();
        }
    }


void refreshWallets()
{
    List<Wallet> wallets=getAllWallets();
    List<Wallet>updated=new ArrayList<>();
    if(wallets==null)return;

    for (Wallet wallet:wallets)
    {
        Balance newBalance=getBalanceFromServer(wallet.getCoinCode(),wallet.getWalletAddress());
        if(newBalance!=null)
        {
            wallet.setBalance(newBalance);
            updated.add(wallet);}
    }
    if (updated.size()!=0) {
        updateWalletsDB(updated);


    }
    else Log.d("wallet", "refreshWallets: no update available");

}

void updateWalletsWorth()
{
    Intent intent1=new Intent(getApplicationContext(),UpdateWalletsWorthService.class);
    intent1.putExtra(Currency.EXTRA_DATA_CURRENCY_CODE,Currency.currencyArray[helper.getDefaultCurrency()]);
    startService(intent1);
}

}
