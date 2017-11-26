package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.arch.persistence.room.Update;
import android.content.Intent;

import com.codit.cryptowatchwallet.api.MarketApi;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.orm.MarketDao;
import com.codit.cryptowatchwallet.orm.WalletDao;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.Currency;

import java.io.IOException;
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
            updateDB(fetchDataFromServer());

            Intent intent1=new Intent(getApplicationContext(),RefreshWalletService.class);
            //get def currency from preference
            //intent1.putExtra(Currency.EXTRA_DATA_CURRENCY_CODE,Currency.USD);
            startService(intent1);
        }
    }

    LinkedHashMap<String, HashMap<String, Double>>  fetchDataFromServer()
    {
        Retrofit retrofit=new Retrofit.Builder()
                .baseUrl("https://min-api.cryptocompare.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MarketApi marketApi=retrofit.create(MarketApi.class);
        Call<LinkedHashMap<String,HashMap<String,Double> >> call=marketApi.getAllCoinPrices();
        try {
            Response<LinkedHashMap<String, HashMap<String, Double>>> response=call.execute();
            if (response.isSuccessful())
            {
                return response.body();
            }
            else {

                return null;
            }
        } catch (IOException e) {
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
