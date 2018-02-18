package com.codit.cryptowatchwallet.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class ApiClient {

    private static final int CONNECTION_TIME_OUT=20;
    private final static String BASE_URL_BLOCKCYPHER="https://api.blockcypher.com/v1/";
    private static final String BASE_URL_MARKET="https://min-api.cryptocompare.com/";

    private static ApiClient instance=null;
    private Retrofit walletClient =null, marketClient=null;

    private ApiClient()
    {
        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                .build();
         walletClient = new Retrofit.Builder().baseUrl(BASE_URL_BLOCKCYPHER)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

         marketClient=new Retrofit.Builder()
                .baseUrl(BASE_URL_MARKET)
                 .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static ApiClient getInstance()
    {
        if(instance==null)
        {
            instance=new ApiClient();
        }

        return instance;
    }

    public Retrofit getWalletClient()
    {
        return walletClient;
    }

    public Retrofit getMarketClient()
    {
        return marketClient;
    }
}
