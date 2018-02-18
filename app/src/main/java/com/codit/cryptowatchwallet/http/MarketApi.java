package com.codit.cryptowatchwallet.http;


import java.util.HashMap;
import java.util.LinkedHashMap;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Sreejith on 22-Nov-17.
 */

public interface MarketApi {

    @GET("data/pricemulti")
    Call<LinkedHashMap<String,HashMap<String,Double> >> getAllCoinPrices(@Query("fsyms") String coinsList,
                                                                         @Query("tsyms") String currencyList);


}
