package com.codit.cryptowatchwallet.api;


import com.codit.cryptowatchwallet.model.CypherAddressBalance;

import java.util.HashMap;
import java.util.LinkedHashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public interface MarketApi {

    @GET("data/pricemulti?fsyms=BTC,ETH,BCH,XRP,LTC,DASH,NEO,DOGE,ETC,XMR,LSK&tsyms=INR,USD,EUR,AUD,CAD,NZD,GBP")
    Call<LinkedHashMap<String,HashMap<String,Double> >> getAllCoinPrices();


}
