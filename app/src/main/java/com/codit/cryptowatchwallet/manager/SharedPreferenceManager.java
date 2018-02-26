package com.codit.cryptowatchwallet.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class SharedPreferenceManager {

    public static final String DEFAULT_CURRENCY="default_currency";
    public static final String REFRESH_INTERVAL="refresh_interval";
    public static final String RATE_APP = "settings_rate_us";
    public static final String CONTACT_US = "settings_contact_us";
    public static final String SHARE_APP = "settings_share_app";
    public static final String DONATE = "settings_donate";
    public static final String SESSION_COUNT="session_count";
    public static boolean SESSION_COUNT_UPDATED=false;
    public  static final String UNIQUE_ID ="nitif_id";
    public static final String NOTIFICATION_Q="notif_q";

    SharedPreferences preferenceManager;

    public SharedPreferenceManager(Context context)
    {
        preferenceManager= android.preference.PreferenceManager.getDefaultSharedPreferences(context);

    }

    public void setDefaultCurrency(String currency){preferenceManager.edit().putString(DEFAULT_CURRENCY,currency).apply();}
    public String getDefaultCurrency(){return preferenceManager.getString(DEFAULT_CURRENCY,"USD");}
    public void setSessionCount(int sessionCount){preferenceManager.edit().putInt(SESSION_COUNT,sessionCount).apply();}
    public int getSessionCount(){return preferenceManager.getInt(SESSION_COUNT,0);}
    private int getUniqueID() {return preferenceManager.getInt(UNIQUE_ID,1);}
    public String getNotificationQ(){return preferenceManager.getString(NOTIFICATION_Q,null);}

    public int generateUniqueID()
    {
        preferenceManager.edit().putInt(UNIQUE_ID, getUniqueID()+1).commit();
        return getUniqueID();
    }

    public void updateNotificationQ(String jsonString)
    {
        preferenceManager.edit().putString(NOTIFICATION_Q,jsonString).commit();
    }

}
