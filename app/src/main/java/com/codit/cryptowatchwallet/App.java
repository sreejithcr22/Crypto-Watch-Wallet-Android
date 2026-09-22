package com.codit.cryptowatchwallet;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.util.ServiceStarter;
import com.codit.cryptowatchwallet.worker.MarketRefreshWorker;

import java.util.concurrent.TimeUnit;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class App extends Application {
    private static final String TAG="app";
    public static final String CHANNEL_ID = "wallet_alerts";
    private static final String PERIODIC_WORK = "market-refresh";
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        sharedPreferenceManager =new SharedPreferenceManager(getApplicationContext());
        createNotificationChannel();
        initSession();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Wallet alerts", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void initSession() {
        setUpPeriodicRefresh();
        sharedPreferenceManager.setSessionCount(sharedPreferenceManager.getSessionCount() + 1);
        Intent intent = new Intent(this, FetchMarketDataService.class);
        ServiceStarter.start(this, intent);

        Log.d(TAG, "session count=" + sharedPreferenceManager.getSessionCount());
    }

    public static void setUpPeriodicRefresh(Application app) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                MarketRefreshWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(app).enqueueUniquePeriodicWork(
                PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    private void setUpPeriodicRefresh() {
        setUpPeriodicRefresh(this);
    }
}
