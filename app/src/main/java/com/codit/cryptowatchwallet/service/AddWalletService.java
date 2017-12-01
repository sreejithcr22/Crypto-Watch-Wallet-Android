package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.EditText;

import com.codit.cryptowatchwallet.api.WalletApi;
import com.codit.cryptowatchwallet.model.BCHAddressBalance;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.CypherAddressBalance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.MarketDao;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AddWalletService extends BaseService {


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            String walletName=intent.getStringExtra(Wallet.WALLET_NAME);
            String walletCoinCode=intent.getStringExtra(Wallet.WALLET_COIN_CODE);
            String walletAddress=intent.getStringExtra(Wallet.WALLET_ADDRESS);

            initializeDB();
            //check values not null
            if(walletName==null||walletCoinCode==null||walletAddress==null||checkIfWalletDuplicate(walletName,walletAddress))return;



            Log.d("wallet", "onHandleIntent: past unique check");
                Balance balance=getBalanceFromServer(walletCoinCode,walletAddress);
            if(balance!=null)
            {
                //calculate coin worth
                //get coin price in default currency

              try{

                  String coinWorth=Coin.calculateCoinWorth(balance.getCoinBalance(),getCoinRateFromDB(walletCoinCode,Currency.USD),Currency.USD);
                  addWalletToDB(new Wallet(walletName,walletCoinCode,walletAddress,balance,coinWorth));
                  reportStatus(SUCCESS_WALLET_ADDED,false);

              }catch (Exception e){reportStatus(ERROR_UNKNOWN,true);}




            }
            else {
                //broadcast error to main activity
                reportStatus(ERROR_UNKNOWN,true);

            }



        }
    }

   
    void addWalletToDB(Wallet wallet)
    {
        try {
            walletDao.addNewWallet(wallet);

        }catch (Exception e)
        {
            reportStatus(ERROR_UNKNOWN,true);
        }
    }


    public boolean checkIfWalletDuplicate(String walletName,String walletAddress)
    {
        Wallet object=walletDao.checkIfNameDuplicate(walletName);
        if(object!=null)
        {
            //send error broadcast to activity
            reportStatus(ERRROR_DUPLICATE_WALLET,true);
            return true;
        }
        Wallet object2=walletDao.checkIfAddressDuplicate(walletAddress);
        if(object2!=null)
        {
            reportStatus("Address already exists with wallet: "+object2.getDisplayName(),true);
            return true;
        }

        return false;
    }


}
