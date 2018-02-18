package com.codit.cryptowatchwallet;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.codit.cryptowatchwallet.manager.SharedPreferenceManager;
import com.codit.cryptowatchwallet.receiver.ScheduleAlarm;
import com.codit.cryptowatchwallet.service.FetchMarketDataService;

/**
 * Created by Sreejith on 18-Feb-18.
 */

public class App extends Application {
    private static final String TAG="app";
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");

        sharedPreferenceManager =new SharedPreferenceManager(getApplicationContext());
        initSession();
    }

    private void initSession() {
        if (sharedPreferenceManager.getSessionCount() == 0) {
            setUpAlarm();
        }
        sharedPreferenceManager.setSessionCount(sharedPreferenceManager.getSessionCount() + 1);
        Intent intent = new Intent(this, FetchMarketDataService.class);
        startService(intent);

       /* if (!SharedPreferenceManager.SESSION_COUNT_UPDATED) {

            SharedPreferenceManager.SESSION_COUNT_UPDATED = true;

        }*/
        Log.d(TAG, "session count=" + sharedPreferenceManager.getSessionCount());
    }

    private void setUpAlarm() {

        /*Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pintent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5*60*1000, pintent);*/
        Intent intent = new Intent(this, ScheduleAlarm.class);
        sendBroadcast(intent);
    }
}
