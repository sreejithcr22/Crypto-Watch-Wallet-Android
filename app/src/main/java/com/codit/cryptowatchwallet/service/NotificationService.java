package com.codit.cryptowatchwallet.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.activity.MainActivity;
import com.codit.cryptowatchwallet.helper.PreferenceHelper;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class NotificationService extends IntentService {
public static final String WALLET_TITLE="wallet_title";
public static final String NEW_TRANS_COUNT="new_trans_count";
public static final String WALLET_BALANCE_DIFF="wallet_balance_diff";
PreferenceHelper helper;


    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {


            helper=new PreferenceHelper(this);

            String notificationTitle=intent.getStringExtra(WALLET_TITLE);
            String message1=String.valueOf(intent.getLongExtra(NEW_TRANS_COUNT,0));
            String message2=intent.getStringExtra(WALLET_BALANCE_DIFF);

            NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
            Intent intent1=new Intent(this, MainActivity.class);
            PendingIntent pendingIntent=PendingIntent.getActivity(this,0,intent1,0);
            builder.setContentIntent(pendingIntent)
                    .setContentText(message1+"-"+message2)
                    .setContentTitle(notificationTitle)
                    .setSmallIcon(R.mipmap.ic_launcher);

            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(helper.generateNotificationID(),builder.build());

        }
    }

}
