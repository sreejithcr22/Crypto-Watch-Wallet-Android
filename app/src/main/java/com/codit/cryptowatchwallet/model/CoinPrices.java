package com.codit.cryptowatchwallet.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sreejith on 22-Nov-17.
 */
@Entity
public class CoinPrices {

    public CoinPrices(){

    }

    @NonNull
    @PrimaryKey
    String coinCode;

    @Ignore
    public HashMap<String,Double> getPrices() {

        return new Gson().fromJson(jsonPricesString,HashMap.class);


    }

    @Ignore
    HashMap<String,Double> prices;

    public CoinPrices(String coinCode, HashMap<String,Double> prices) {
        this.coinCode = coinCode;
        this.jsonPricesString=new Gson().toJson(prices);
        this.prices=prices;

    }


    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }




    public String getJsonPricesString() {
        return jsonPricesString;
    }

    public void setJsonPricesString(String jsonPricesString) {
        this.jsonPricesString = jsonPricesString;
    }

    String jsonPricesString;
}
