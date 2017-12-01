package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codit.cryptowatchwallet.api.WalletApi;
import com.codit.cryptowatchwallet.model.BCHAddressBalance;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.CypherAddressBalance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.MarketDao;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.util.Coin;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public abstract class BaseService extends IntentService {

    public static final String REPORT_STATUS="report_status";
    public static final String REPORT_DATA="report_data";
    public static final String ERROR_MESSAGE_BAD_REQUEST="Invalid address";
    public static final String ERROR_MESSAGE_NO_INTERNET="No internet connection";
    public static final String ERROR_REQUEST_TIME_OUT="Request timed out, please try again";
    public static final String ERROR_UNKNOWN="Sorry, something went wrong";
    public static final String SUCCESS_WALLET_ADDED="Wallet added successfully";
    public static final String ERRROR_DUPLICATE_WALLET="Wallet name already exists";
    public static final String IS_ERROR="is-error";

    public   MarketDao marketDao;
    public   WalletDao walletDao;

    public BaseService() {super("BaseService");}


    public void initializeDB()
    {
        marketDao= AppDatabase.getDatabase(getApplicationContext()).marketDao();
        walletDao=AppDatabase.getDatabase(getApplicationContext()).walletDao();
    }

    Balance getBalanceFromServer(String coinCode, String address)
    {

        Retrofit retrofit=new Retrofit.Builder().baseUrl("https://api.blockcypher.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        WalletApi walletApi=retrofit.create(WalletApi.class);

        try {

            if (coinCode.equals(Coin.BTC) || coinCode.equals(Coin.ETH) || coinCode.equals(Coin.LTC) || coinCode.equals(Coin.DASH) || coinCode.equalsIgnoreCase(Coin.DOGE)) {

                Response<CypherAddressBalance> response=walletApi.getCypherAddressBalance("https://api.blockcypher.com/v1/"+coinCode.toLowerCase()+"/main/addrs/"+address+"/balance").execute();


                if(response.isSuccessful())
                {
                    return response.body().getWalletBalance(coinCode);
                }
                else {
                    //check error
                    reportStatus(ERROR_MESSAGE_BAD_REQUEST,true);
                    Log.d("wallet", "getBalanceFromServer: message="+response.message()+", code-"+response.code()+" errorbody="+response.errorBody().string());
                    return null;
                }

            } else if (coinCode.equals(Coin.BCH)) {

                Response<BCHAddressBalance> response=walletApi.getBCHAddressBalance("https://blockdozer.com/insight-api/addr/"+address).execute();


                if(response.isSuccessful())
                {
                    return response.body().getWalletBalance(coinCode);
                }
                else {
                    //check error
                    reportStatus(ERROR_MESSAGE_BAD_REQUEST,true);
                    Log.d("wallet", "getBalanceFromServer: message="+response.message()+", code-"+response.code()+" errorbody="+response.errorBody().string());
                    return null;
                }

            }

        }catch (IOException e) {
            //check error
            Log.d("wallet", "getBalanceFromServer: catch clause1--"+e.getMessage());
            if(e.getMessage().equals("timeout"))reportStatus(ERROR_REQUEST_TIME_OUT,true);
            e.printStackTrace();
            return null;

        }
        catch (Exception e)
        {

            Log.d("wallet", "getBalanceFromServer: catch clause2 --"+e.getMessage());

            return null;
        }
        return null;
    }

    List<Wallet> getAllWallets()
    {

        try {
            List<Wallet>wallets=walletDao.getAllWallets();
            return wallets;
        }catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    int updateWalletsDB(List<Wallet>wallets)
    {
        try {
            int rows= walletDao.updateWallets(wallets);
            Log.d("wallet", "updateCoinsWorth: "+String.valueOf(rows)+" rows updated");
            return rows;
        }catch (Exception e)
        {
            return -1;
        }

    }

    double getCoinRateFromDB(String coinCode,String currency)
    {
        return marketDao.getCoinPricesFor(coinCode).getPrices().get(currency);

    }

    void reportStatus(String message,boolean isError)
    {
        Intent intent=new Intent(REPORT_STATUS);
        intent.putExtra(REPORT_DATA,message);
        intent.putExtra(IS_ERROR,isError);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
