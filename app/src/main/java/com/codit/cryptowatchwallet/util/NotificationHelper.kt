package com.codit.cryptowatchwallet.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.codit.cryptowatchwallet.CryptoWatchApp
import com.codit.cryptowatchwallet.R
import com.codit.cryptowatchwallet.activity.MainActivity
import com.codit.cryptowatchwallet.model.Transaction
import com.codit.cryptowatchwallet.model.Wallet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun showWalletNotification(wallet: Wallet, transaction: Transaction, notificationId: Int) {
        val title = "${transaction.tnxCount} new transactions !"
        var balanceDiff = transaction.balanceDiff ?: "0"
        if (!balanceDiff.contains("-")) balanceDiff = "+$balanceDiff"

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context, CryptoWatchApp.CHANNEL_ID)

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(Wallet.EXTRA_WALLET_NAME, wallet.displayName)
            putExtra("navigate_to_wallet_details", true)
        }
        val stackBuilder = TaskStackBuilder.create(context).apply {
            addParentStack(MainActivity::class.java)
            addNextIntent(intent)
        }
        val pendingIntent = stackBuilder.getPendingIntent(
            notificationId,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.setContentIntent(pendingIntent)
            .setContentText("${wallet.displayName} ($balanceDiff ${wallet.coinCode})")
            .setSubText("${Coin.getCoinName(wallet.coinCode)}(${wallet.coinCode})")
            .setContentTitle(title)
            .setSmallIcon(R.drawable.ic_notification)
            .setSound(soundUri)
            .setAutoCancel(true)
            .addAction(
                NotificationCompat.Action(
                    R.mipmap.ic_launcher,
                    "DETAILS",
                    pendingIntent
                )
            )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.notify(notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.d("wallet", "notification permission revoked, skipping")
        }
    }
}
