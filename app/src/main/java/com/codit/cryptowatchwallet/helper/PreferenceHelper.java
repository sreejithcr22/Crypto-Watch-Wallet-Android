package com.codit.cryptowatchwallet.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Sreejith on 28-Nov-17.
 */

public class PreferenceHelper {

    public static final String DEFAULT_CURRENCY="default_currency";
    public static final String REFRESH_INTERVAL="refresh_interval";
    public static final String SESSION_COUNT="session_count";
    public static boolean SESSION_COUNT_UPDATED=false;
    SharedPreferences preferenceManager;

    public PreferenceHelper(Context context)
    {
        preferenceManager= PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setDefaultCurrency(int arrayPos){preferenceManager.edit().putInt(DEFAULT_CURRENCY,arrayPos).apply();}
    public int getDefaultCurrency(){return preferenceManager.getInt(DEFAULT_CURRENCY,0);}
    public void setSessionCount(int sessionCount){preferenceManager.edit().putInt(SESSION_COUNT,sessionCount).apply();}
    public int getSessionCount(){return preferenceManager.getInt(SESSION_COUNT,0);}

}
