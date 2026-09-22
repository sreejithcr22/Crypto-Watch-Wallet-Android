package com.codit.cryptowatchwallet.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.codit.cryptowatchwallet.BuildConfig;
import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.http.ApiClient;
import com.codit.cryptowatchwallet.http.MarketApi;
import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.model.CoinPrices;
import com.codit.cryptowatchwallet.model.Wallet;
import com.codit.cryptowatchwallet.orm.AppDatabase;
import com.codit.cryptowatchwallet.util.Coin;
import com.codit.cryptowatchwallet.util.UrlBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

/**
 * Background replacement for the old repeating-alarm refresh.
 * Fetches market prices and revalues wallets. Safe to run while
 * the app is in the background (no startService involved).
 */
public class MarketRefreshWorker extends Worker {

    private static final String TAG = "wallet";

    public MarketRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            String apiKey = BuildConfig.CRYPTOCOMPARE_API_KEY;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                Log.d(TAG, "MarketRefreshWorker: no api key, skipping");
                return Result.success();
            }
            MarketApi marketApi = ApiClient.getInstance().getMarketClient().create(MarketApi.class);
            Response<LinkedHashMap<String, HashMap<String, Double>>> response = marketApi.getAllCoinPrices(
                    UrlBuilder.buildCoinList(),
                    UrlBuilder.buildCurrencyList(getApplicationContext().getResources().getStringArray(R.array.currencies)),
                    apiKey.trim()).execute();
            if (!response.isSuccessful() || response.body() == null) {
                Log.d(TAG, "MarketRefreshWorker: code=" + response.code());
                return Result.retry();
            }
            List<CoinPrices> coinPricesList = new ArrayList<>();
            for (Map.Entry<String, HashMap<String, Double>> entry : response.body().entrySet()) {
                coinPricesList.add(new CoinPrices(entry.getKey(), entry.getValue()));
            }
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.marketDao().addCoinPrices(coinPricesList);

            String currency = new SharedPreferenceManager(getApplicationContext()).getDefaultCurrency();
            List<Wallet> wallets = db.walletDao().getAllWallets();
            if (wallets != null) {
                for (Wallet wallet : wallets) {
                    try {
                        double rate = db.marketDao().getCoinPricesFor(wallet.getCoinCode()).getPrices().get(currency);
                        wallet.setCoinWorth(Coin.calculateCoinWorth(wallet.getBalance().getCoinBalance(), rate, currency));
                    } catch (Exception ignored) {
                    }
                }
                db.walletDao().updateWallets(wallets);
            }
            return Result.success();
        } catch (Exception e) {
            Log.d(TAG, "MarketRefreshWorker failed: " + e);
            return Result.retry();
        }
    }
}
