package com.codit.cryptowatchwallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.service.BaseService;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;
import com.codit.cryptowatchwallet.service.RefreshWalletService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("wallet", "AlarmReceiver onReceive: ");
        Intent serviceIntent=new Intent(context.getApplicationContext(),FetchMarketDataService.class);
        context.getApplicationContext().startService(serviceIntent);
    }
}
