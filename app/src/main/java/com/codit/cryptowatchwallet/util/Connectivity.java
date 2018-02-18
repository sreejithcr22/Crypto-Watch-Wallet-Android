package com.codit.cryptowatchwallet.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by Sreejith on 17-Feb-18.
 */

public class Connectivity {
    private Context mContext;

    public Connectivity(Context mContext) {
        this.mContext = mContext;
    }

    public boolean isConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnectedOrConnecting();

    }
}
