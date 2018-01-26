package com.codit.cryptowatchwallet.service;

import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;


public class AddWalletService extends BaseService {


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            String walletName=intent.getStringExtra(Wallet.WALLET_NAME);
            String walletCoinCode=intent.getStringExtra(Wallet.WALLET_COIN_CODE);
            String walletAddress=intent.getStringExtra(Wallet.WALLET_ADDRESS);

            initializeDB();
            //check values not null
            if(walletName==null||walletCoinCode==null||walletAddress==null|| isWalletDuplicate(walletName,walletAddress))return;


            Log.d("wallet", "onHandleIntent: past unique check");
            Balance balance=getBalanceFromServer(walletCoinCode,walletAddress);
            if(balance!=null)
            {
                //calculate coin worth
                //get coin price in default currency
                try{

                  String coinWorth=Coin.calculateCoinWorth(balance.getCoinBalance(),getCoinRateFromDB(walletCoinCode,Currency.USD),Currency.USD);
                  addWalletToDB(new Wallet(walletName,walletCoinCode,walletAddress,balance,coinWorth));
                  reportStatus(SUCCESS_WALLET_ADDED,REPOT_TYPE_SUCCESS);

                  //fetch market data and refresh wallets
                    Intent marketIntent=new Intent(this,FetchMarketDataService.class);
                    startService(marketIntent);


                }catch (Exception e){reportStatus(ERROR_UNKNOWN,REPOT_TYPE_FAILURE);}

            }
            else {
                //broadcast error to main activity
                reportStatus(ERROR_UNKNOWN,REPOT_TYPE_FAILURE);

            }



        }
    }

   
    void addWalletToDB(Wallet wallet)
    {
        walletDao.addNewWallet(wallet);
    }


    public boolean isWalletDuplicate(String walletName, String walletAddress)
    {
        Wallet object=walletDao.checkIfNameDuplicate(walletName);
        if(object!=null)
        {
            //send error broadcast to activity
            reportStatus(ERRROR_DUPLICATE_WALLET,REPOT_TYPE_FAILURE);
            return true;
        }
        Wallet object2=walletDao.checkIfAddressDuplicate(walletAddress);
        if(object2!=null)
        {
            reportStatus("Address already exists with wallet: "+object2.getDisplayName(),REPOT_TYPE_FAILURE);
            return true;
        }

        return false;
    }


}
