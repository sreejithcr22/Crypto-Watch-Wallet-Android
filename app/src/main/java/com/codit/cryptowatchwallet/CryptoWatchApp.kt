package com.codit.cryptowatchwallet

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.worker.MarketRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CryptoWatchApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var settingsRepository: SettingsRepository

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Launch must never die from a startup side-effect (corrupt prefs,
        // WorkManager re-init, notification channel). Each step is isolated
        // so one failure cannot take the whole app down.
        try {
            Log.d(TAG, "onCreate")
        } catch (_: Throwable) {
        }
        try {
            createNotificationChannel()
        } catch (t: Throwable) {
            Log.w(TAG, "notification channel failed", t)
        }
        try {
            settingsRepository.incrementSessionCount()
        } catch (t: Throwable) {
            Log.w(TAG, "session count failed", t)
        }
        try {
            setUpPeriodicRefresh()
        } catch (t: Throwable) {
            Log.w(TAG, "periodic refresh scheduling failed", t)
        }
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID, "Wallet alerts", NotificationManager.IMPORTANCE_DEFAULT
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "createNotificationChannel failed", t)
        }
    }

    private fun setUpPeriodicRefresh() {
        try {
            val request = PeriodicWorkRequestBuilder<MarketRefreshWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                PERIODIC_WORK, ExistingPeriodicWorkPolicy.KEEP, request
            )
        } catch (t: Throwable) {
            Log.w(TAG, "setUpPeriodicRefresh failed", t)
        }
    }

    companion object {
        const val CHANNEL_ID = "wallet_alerts"
        private const val PERIODIC_WORK = "market-refresh"
        private const val TAG = "app"
    }
}
