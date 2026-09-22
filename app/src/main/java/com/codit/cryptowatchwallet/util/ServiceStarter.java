package com.codit.cryptowatchwallet.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Starts services without crashing the app when the system forbids
 * background service starts (Android 8+ limits, enforced as
 * BackgroundServiceStartNotAllowedException on Android 12+).
 *
 * Callers in the foreground (activities, visible fragments) are unaffected.
 * Calls from the background (Application.onCreate, receivers) are skipped
 * with a log instead of killing the process.
 */
public class ServiceStarter {

    private static final String TAG = "wallet";

    public static void start(Context context, Intent intent) {
        if (context == null || intent == null) return;
        try {
            context.startService(intent);
        } catch (RuntimeException e) {
            // IllegalStateException (API 26-30),
            // SecurityException and BackgroundServiceStartNotAllowedException (API 31+)
            Log.d(TAG, "service start not allowed now, skipping: " + intent.getComponent());
        }
    }
}
