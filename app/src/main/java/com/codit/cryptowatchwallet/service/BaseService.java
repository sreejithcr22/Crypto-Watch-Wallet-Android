package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.codit.cryptowatchwallet.http.ApiClient;
import com.codit.cryptowatchwallet.http.WalletApi;
import com.codit.cryptowatchwallet.model.BCHAddressBalance;
import com.codit.cryptowatchwallet.model.Balance;
import com.codit.cryptowatchwallet.model.CypherAddressBalance;
import com.codit.cryptowatchwallet.model.RippleBalance;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.MarketDao;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Connectivity;

import java.io.IOException;
import java.util.List;

import retrofit2.Response;
import retrofit2.Retrofit;


public abstract class BaseService extends IntentService {

    public static final String REPORT_STATUS="report_status";
    public static final String REPORT_DATA="report_data";
    public static final String ERROR_MESSAGE_BAD_REQUEST="Invalid address";
    public static final String ERROR_MESSAGE_NO_INTERNET="No internet connection";
    public static final String ERROR_REQUEST_TIME_OUT="Connection timed out, please try again";
    public static final String ERROR_UNKNOWN="Sorry, something went wrong";
    public static final String SUCCESS_WALLET_ADDED="Wallet added successfully";
    public static final String ERRROR_DUPLICATE_WALLET="Wallet name already exists";
    public static final String IS_ERROR="is-error";
    public static final String REPORT_TYPE="report_type";
    public static final String REPOT_TYPE_SUCCESS="report_success";
    public static final String REPOT_TYPE_FAILURE="report_failure";
    public static final String REPORT_TYPE_WARNING="report_warning";
    public static final String EXTRA_SHOULD_IGNORE_WALLET_REFRESH="ignore_wallet_refresh";

    private final static String BASE_URL_BLOCKCYPHER="https://api.blockcypher.com/v1/";
    private final static String BASE_URL_BCASH="https://blockdozer.com/insight-api/";
    private final static String BASE_URL_RIPPLE="https://data.ripple.com/v2/accounts/";


    public static final String EXTRA_SHOULD_START_NOTIFICATION="start_notification";
    public static final String EXTRA_SHOULD_UPDATE_WALLET_WORTH="update_wallet_worth";

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
        Retrofit retrofit= ApiClient.getInstance().getWalletClient();
        WalletApi walletApi=retrofit.create(WalletApi.class);

        try {

            if (coinCode.equals(Coin.BTC) || coinCode.equals(Coin.ETH) || coinCode.equals(Coin.LTC) || coinCode.equals(Coin.DASH) || coinCode.equalsIgnoreCase(Coin.DOGE)) {

                Response<CypherAddressBalance> response=walletApi.getCypherAddressBalance(BASE_URL_BLOCKCYPHER+coinCode.toLowerCase()+"/main/addrs/"+address+"/balance").execute();

                if(response.isSuccessful())
                {
                    return response.body().getWalletBalance(coinCode);
                }
                else {
                    //check error
                    reportStatus(ERROR_MESSAGE_BAD_REQUEST,REPOT_TYPE_FAILURE);
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
                    reportStatus(ERROR_MESSAGE_BAD_REQUEST,REPOT_TYPE_FAILURE);
                    Log.d("wallet", "getBalanceFromServer: message="+response.message()+", code-"+response.code()+" errorbody="+response.errorBody().string());
                    return null;
                }

            }

            else if(coinCode.equals(Coin.XRP))
            {

                Response<RippleBalance> response=walletApi.getRippleBalance("https://data.ripple.com/v2/accounts/"+address+"/balances").execute();

                if(response.isSuccessful())
                {
                    Log.d("wallet", "getBalanceFromServer: "+response.raw().toString());
                    return response.body().getWalletBalance(coinCode);
                }
                else {
                    //check error
                    reportStatus(ERROR_MESSAGE_BAD_REQUEST,REPOT_TYPE_FAILURE);
                    Log.d("wallet", "getBalanceFromServer: message="+response.message()+", code-"+response.code()+" errorbody="+response.errorBody().string());
                    return null;
                }
            }

        }catch (IOException e) {
            //check error
            Log.d("wallet", "getBalanceFromServer: catch clause1--"+e.getMessage());
            if(e.getMessage().contains("timeout"))reportStatus(ERROR_REQUEST_TIME_OUT,REPOT_TYPE_FAILURE);
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

    Wallet getWalletByName(String walletName)
    {
        try {
            Wallet wallet=walletDao.getWalletByName(walletName);
            return wallet;
        }catch (Exception e)
        {
            return null;
        }

    }

    synchronized int updateWalletsDB(List<Wallet>wallets)
    {
        try {
            int rows= walletDao.updateWallets(wallets);
            Log.d("wallet", "updateWalletsDB(): "+String.valueOf(rows)+" rows updated");
            return rows;
        }catch (Exception e)
        {
            return -1;
        }

    }

    double getCoinRateFromDB(String coinCode,String currency)
    {
        try{
            return marketDao.getCoinPricesFor(coinCode).getPrices().get(currency);
        }
        catch (NullPointerException e)
        {
            reportStatus("Cannot calculate wallet value since market data is not available, please refresh 'Market' tab to fetch coin prices.",REPORT_TYPE_WARNING);
            return -1;
        }



    }

    void reportStatus(String message,String reportType)
    {
        Connectivity connectivity=new Connectivity(this.getApplicationContext());
        if(reportType.equals(REPOT_TYPE_FAILURE)&&! connectivity.isConnected())
        {
            message=ERROR_MESSAGE_NO_INTERNET;
        }
        Intent intent=new Intent(REPORT_STATUS);
        intent.putExtra(REPORT_DATA,message);
        intent.putExtra(REPORT_TYPE,reportType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
