package com.codit.cryptowatchwallet.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.App;

/**
 * Re-registers periodic background refresh (e.g. after boot).
 * WorkManager survives on its own; this is a safety net.
 */
public class ScheduleAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("wallet", "ScheduleAlarm onReceive: ");
        App.setUpPeriodicRefresh((android.app.Application) context.getApplicationContext());
    }


}
