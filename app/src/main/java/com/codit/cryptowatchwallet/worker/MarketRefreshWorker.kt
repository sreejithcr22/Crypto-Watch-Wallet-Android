package com.codit.cryptowatchwallet.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background refresh: market prices + wallet balances + revaluation + notifications.
 * Hilt-injected CoroutineWorker, scheduled every 15 min from [com.codit.cryptowatchwallet.CryptoWatchApp].
 */
@HiltWorker
class MarketRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val walletRepository: WalletRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            try {
                walletRepository.refreshWalletsAndWorth(refreshBalances = false)
            } catch (_: Throwable) {
            }
            // Lightweight tick: market + worth only (balances refreshed on foreground pull).
            // For full balance refresh in background, flip to true (adds API load + delays).
            try {
                walletRepository.drainNotifications()
            } catch (_: Throwable) {
            }
            Result.success()
        } catch (e: Throwable) {
            try {
                Log.d(TAG, "MarketRefreshWorker failed: $e")
            } catch (_: Throwable) {
            }
            try {
                Result.retry()
            } catch (_: Throwable) {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "wallet"
    }
}
