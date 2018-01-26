package com.codit.cryptowatchwallet.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScheduleAlarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i("wallet", "ScheduleAlarm onReceive: ");
        //setup alarm
        Intent intent1 = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        PendingIntent pintent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5*60*1000, pintent);
    }


}
