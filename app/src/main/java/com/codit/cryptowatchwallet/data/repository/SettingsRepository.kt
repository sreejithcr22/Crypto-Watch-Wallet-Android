package com.codit.cryptowatchwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        const val DEFAULT_CURRENCY = "default_currency"
        const val REFRESH_INTERVAL = "refresh_interval"
        const val RATE_APP = "settings_rate_us"
        const val CONTACT_US = "settings_contact_us"
        const val SHARE_APP = "settings_share_app"
        const val DONATE = "settings_donate"
        const val CREDITS = "settings_credits"
        const val SESSION_COUNT = "session_count"
        const val UNIQUE_ID = "nitif_id"
        const val NOTIFICATION_Q = "notif_q"
    }

    private val prefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun observeDefaultCurrency(): Flow<String> =
        prefsFlow(DEFAULT_CURRENCY, "USD")

    fun getDefaultCurrency(): String = prefs.getString(DEFAULT_CURRENCY, "USD") ?: "USD"

    fun setDefaultCurrency(currency: String) {
        prefs.edit().putString(DEFAULT_CURRENCY, currency).apply()
    }

    fun getSessionCount(): Int = prefs.getInt(SESSION_COUNT, 0)

    fun incrementSessionCount(): Int {
        val next = getSessionCount() + 1
        prefs.edit().putInt(SESSION_COUNT, next).apply()
        return next
    }

    fun getNotificationQ(): String? = prefs.getString(NOTIFICATION_Q, null)

    fun updateNotificationQ(json: String?) {
        prefs.edit().putString(NOTIFICATION_Q, json).apply()
    }

    fun generateUniqueId(): Int {
        val next = prefs.getInt(UNIQUE_ID, 1) + 1
        prefs.edit().putInt(UNIQUE_ID, next).apply()
        return next
    }

    private fun prefsFlow(key: String, default: String): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, changedKey ->
            if (changedKey == key) {
                trySend(sp.getString(key, default) ?: default)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(key, default) ?: default)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.map { it }
}
