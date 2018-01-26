package com.codit.cryptowatchwallet.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codit.cryptowatchwallet.R;
import com.codit.cryptowatchwallet.activity.WalletDetailsActivity;
import com.codit.cryptowatchwallet.helper.PreferenceHelper;
import com.codit.cryptowatchwallet.model.Transaction;
import com.codit.cryptowatchwallet.model.Wallet;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;


public class NotificationService extends BaseService {
public static final String WALLET_TITLE="wallet_title";
public static final String NEW_TRANS_COUNT="new_trans_count";
public static final String WALLET_BALANCE_DIFF="wallet_balance_diff";
PreferenceHelper helper;



    String TAG="wallet";

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            Log.d(TAG, "NotificationService: ");
            helper=new PreferenceHelper(this);
            initializeDB();

            String json=helper.getNotificationQ();
            if(json!=null)
            {
                HashMap<String,String> notificationQ =new Gson().fromJson(json,HashMap.class);
                Log.d(TAG, String.valueOf(notificationQ.size()));


                for (Map.Entry entry:notificationQ.entrySet()) {

                    String name= (String) entry.getKey();
                    Transaction transaction= (Transaction) new Gson().fromJson(String.valueOf(entry.getValue()),Transaction.class);
                    Log.d(TAG,name+" , "+transaction.getTnxCount()+" , "+transaction.getBalanceDiff());

                    Wallet wallet=getWalletByName(name);
                    if(wallet!=null)
                    {
                        showNotification(wallet,transaction);
                    }


                }



                //clear notification q
                helper.updateNotificationQ(null);

            }



        }


    }

    private void showNotification(Wallet wallet,Transaction transaction)
    {
         String notificationTitle= String.valueOf(transaction.getTnxCount())+" new transactions !";

            String balanceDiff=transaction.getBalanceDiff();


            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder=new NotificationCompat.Builder(this);

            Intent intent1=new Intent(this, WalletDetailsActivity.class);
            intent1.putExtra(Wallet.EXTRA_WALLET_OBJECT,wallet);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(WalletDetailsActivity.class);
            stackBuilder.addNextIntent(intent1);

        int id=helper.generateUniqueID();
            PendingIntent pendingIntent=stackBuilder.getPendingIntent(id,PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent)
                    .setContentText(wallet.getDisplayName())
                    .setSubText(balanceDiff+" "+wallet.getCoinCode())
                    .setContentTitle(notificationTitle)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setSound(uri)
                    .addAction(new NotificationCompat.Action(R.mipmap.ic_launcher,"DETAILS",pendingIntent));

            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Log.d(TAG, "Notification id= "+String.valueOf(id));
            manager.notify(id,builder.build());
    }

}
