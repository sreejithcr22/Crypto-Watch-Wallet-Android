package com.codit.cryptowatchwallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.codit.cryptowatchwallet.worker.MarketRefreshWorker;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("wallet", "AlarmReceiver onReceive: ");
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MarketRefreshWorker.class).build();
        WorkManager.getInstance(context.getApplicationContext()).enqueue(request);
    }
}
