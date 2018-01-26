package com.codit.cryptowatchwallet.service;

import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.Transaction;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.util.Currency;
import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RefreshWalletService extends BaseService {

    PreferenceHelper helper;
    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null)
        {
            initializeDB();
            helper=new PreferenceHelper(this.getApplicationContext());
            Log.d("wallet", "RefreshWalletService ");

            int updatesCount=refreshWallets();
            updateWalletsWorth(updatesCount>0);

        }
    }


private int refreshWallets()
{
    List<Wallet> wallets=getAllWallets();
    List<Wallet>updated=new ArrayList<>();
    HashMap<String,String> notifications=new HashMap<>();
    if(wallets==null)return 0;


    for (Wallet wallet:wallets)
    {
        Balance newBalance=getBalanceFromServer(wallet.getCoinCode(),wallet.getWalletAddress());
        if(newBalance!=null)
        {
            Log.d("wallet", "refreshWallets: got balance");
            Transaction transaction=checkForNewTnx(wallet.getBalance(),newBalance);
            if(transaction.getTnxCount()>0) {
                wallet.setBalance(newBalance);
                updated.add(wallet);
                notifications.put(wallet.getDisplayName(),new Gson().toJson(transaction));
            }
        }

        //delay the api call so that the ip address is not banned by api provider
        try {

            Thread.sleep(3000);

        } catch (InterruptedException e) {}
    }

    if (updated.size()!=0)
    {
        updateWalletsDB(updated);
        updateNotificationsQueue(notifications);
        return updated.size();

    }
    else
    {
        Log.d("wallet", "refreshWallets: no update available");
        return 0;
    }

}

private void updateNotificationsQueue(HashMap<String,String> notifications)
{
    String jsonString=new Gson().toJson(notifications);
    helper.updateNotificationQ(jsonString);
}


    void updateWalletsWorth(boolean shouldStartNotification)
{
    Intent intent1=new Intent(getApplicationContext(),UpdateWalletsWorthService.class);
    intent1.putExtra(BaseService.EXTRA_SHOULD_START_NOTIFICATION,shouldStartNotification);

    startService(intent1);
}


    private Transaction checkForNewTnx(Balance oldBalance, Balance newBalance)
    {
        BigDecimal oldCoinBalance=new BigDecimal(oldBalance.getCoinBalance());
        BigDecimal newCoinBalance=new BigDecimal(newBalance.getCoinBalance());

        String coinBalanceDifference;
        long newTransactionsCount;

        if((newTransactionsCount=newBalance.getTransactionCount()-oldBalance.getTransactionCount())>0) {

                coinBalanceDifference = newCoinBalance.subtract(oldCoinBalance).toPlainString();
                return new Transaction(newTransactionsCount,coinBalanceDifference);
        }

        return new Transaction(0,null);

    }


}
