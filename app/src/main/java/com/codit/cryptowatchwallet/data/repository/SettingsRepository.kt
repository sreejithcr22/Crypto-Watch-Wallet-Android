package com.codit.cryptowatchwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
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

    fun getDefaultCurrency(): String = try {
        prefs.getString(DEFAULT_CURRENCY, "USD") ?: "USD"
    } catch (t: Throwable) {
        // Corrupt prefs (wrong type from backup/downgrade) must never crash launch.
        try {
            prefs.edit().remove(DEFAULT_CURRENCY).apply()
        } catch (_: Throwable) {
        }
        "USD"
    }

    fun setDefaultCurrency(currency: String) {
        try {
            prefs.edit().putString(DEFAULT_CURRENCY, currency).apply()
        } catch (_: Throwable) {
        }
    }

    fun getSessionCount(): Int = try {
        prefs.getInt(SESSION_COUNT, 0)
    } catch (t: Throwable) {
        try {
            prefs.edit().remove(SESSION_COUNT).apply()
        } catch (_: Throwable) {
        }
        0
    }

    fun incrementSessionCount(): Int {
        return try {
            val next = getSessionCount() + 1
            prefs.edit().putInt(SESSION_COUNT, next).apply()
            next
        } catch (_: Throwable) {
            1
        }
    }

    fun getNotificationQ(): String? = try {
        prefs.getString(NOTIFICATION_Q, null)
    } catch (t: Throwable) {
        try {
            prefs.edit().remove(NOTIFICATION_Q).apply()
        } catch (_: Throwable) {
        }
        null
    }

    fun updateNotificationQ(json: String?) {
        try {
            prefs.edit().putString(NOTIFICATION_Q, json).apply()
        } catch (_: Throwable) {
        }
    }

    fun generateUniqueId(): Int {
        return try {
            val next = try {
                prefs.getInt(UNIQUE_ID, 1) + 1
            } catch (_: Throwable) {
                try {
                    prefs.edit().remove(UNIQUE_ID).apply()
                } catch (_: Throwable) {
                }
                2
            }
            prefs.edit().putInt(UNIQUE_ID, next).apply()
            next
        } catch (_: Throwable) {
            1
        }
    }

    private fun prefsFlow(key: String, default: String): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, changedKey ->
            if (changedKey == key) {
                val v = try {
                    sp.getString(key, default) ?: default
                } catch (_: Throwable) {
                    default
                }
                trySend(v)
            }
        }
        try {
            prefs.registerOnSharedPreferenceChangeListener(listener)
        } catch (_: Throwable) {
        }
        val initial = try {
            prefs.getString(key, default) ?: default
        } catch (_: Throwable) {
            default
        }
        trySend(initial)
        awaitClose {
            try {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            } catch (_: Throwable) {
            }
        }
    }.map { it }.catch { emit(default) }
}
