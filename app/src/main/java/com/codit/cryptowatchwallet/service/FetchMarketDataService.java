package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.http.ApiClient;
import com.codit.cryptowatchwallet.http.MarketApi;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.MarketDao;
import com.codit.cryptowatchwallet.util.UrlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class FetchMarketDataService extends IntentService {


    public FetchMarketDataService() {
        super("FetchMarketDataService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d("wallet", "fetch: ");
            updateDB(fetchDataFromServer());
            Log.d("wallet", "fetch: ignore="+intent.getBooleanExtra(BaseService.EXTRA_SHOULD_IGNORE_WALLET_REFRESH,false));

            if(!intent.getBooleanExtra(BaseService.EXTRA_SHOULD_IGNORE_WALLET_REFRESH,false))
            {
                Intent serviceIntent=new Intent(this,RefreshWalletService.class);
                startService(serviceIntent);
            }
            else
            {
                startService(new Intent(this,UpdateWalletsWorthService.class));
            }

        }
    }

    LinkedHashMap<String, HashMap<String, Double>>  fetchDataFromServer()
    {
        Retrofit retrofit= ApiClient.getInstance().getMarketClient();
        MarketApi marketApi=retrofit.create(MarketApi.class);

        Call<LinkedHashMap<String,HashMap<String,Double> >> call=marketApi.getAllCoinPrices(UrlBuilder.buildCoinList(),
                UrlBuilder.buildCurrencyList());
        try {
            Response<LinkedHashMap<String, HashMap<String, Double>>> response=call.execute();
            if (response.isSuccessful())
            {
                return response.body();
            }
            else {

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void updateDB(LinkedHashMap<String, HashMap<String, Double>> prices)
    {
        if(prices==null) return;
        List<CoinPrices> coinPricesList=new ArrayList<>();
        for(Map.Entry<String, HashMap<String, Double>> entry : prices.entrySet()) {
            coinPricesList.add(new CoinPrices(entry.getKey(),entry.getValue()));

        }
        MarketDao dao= AppDatabase.getDatabase(getApplicationContext()).marketDao();
        dao.addCoinPrices(coinPricesList);



    }




}
