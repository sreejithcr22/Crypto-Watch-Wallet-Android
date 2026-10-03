package com.codit.cryptowatchwallet.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.codit.cryptowatchwallet.ui.main.MainScreen
import com.codit.cryptowatchwallet.ui.theme.CryptoWatchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Draw behind system bars (required since Android 15 enforces
        // edge-to-edge) and keep dark status-bar icons so the bar stays
        // visible over the app's light theme. Scaffold/TopAppBar consume
        // the insets, so content never hides under the bar.
        try {
            enableEdgeToEdge()
            WindowCompat.getInsetsController(window, window.decorView)
                .isAppearanceLightStatusBars = true
        } catch (_: Throwable) {
        }
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                val granted = try {
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } catch (_: Throwable) {
                    true
                }
                if (!granted) {
                    try {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } catch (_: Throwable) {
                    }
                }
            }
        } catch (_: Throwable) {
        }
        try {
            setContent {
                CryptoWatchTheme {
                    MainScreen()
                }
            }
        } catch (t: Throwable) {
            // Last-resort: never show a black crash. Finish gracefully.
            android.util.Log.e("app", "MainActivity setContent failed", t)
            finish()
        }
    }
}
