package com.codit.cryptowatchwallet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codit.cryptowatchwallet.activity.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * First-launch regression test: on a fresh install the Market tab must fetch
 * prices automatically. It used to show "Couldn't load market data" because
 * the cold-start fetch raced the background worker into CoinGecko rate
 * limits; the initial load now retries before surfacing an error.
 */
@RunWith(AndroidJUnit4::class)
class MarketFirstLaunchTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun grantNotificationPermission() {
        // Fresh installs pop a system notification-permission dialog over the
        // app on first launch, which would swallow test taps. Pre-grant it
        // before the rule launches the activity.
        try {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand(
                    "pm grant com.codit.cryptowatchwallet " +
                        "android.permission.POST_NOTIFICATIONS"
                )
        } catch (_: Throwable) {
        }
    }

    private fun nodesWithText(text: String, substring: Boolean = false) = try {
        compose.onAllNodesWithText(text, substring = substring)
            .fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        // No compose hierarchy attached yet (cold start); keep polling.
        false
    }

    @Test
    fun marketTabLoadsPricesWithoutError() {
        compose.waitUntil(timeoutMillis = 60_000) {
            nodesWithText("Market")
        }
        compose.onNodeWithText("Market").performClick()

        // Wait for the load to settle: either coin rows appear (success)
        // or the error text appears (failure). Cold network can be slow,
        // and the initial load retries, so allow generous time.
        compose.waitUntil(timeoutMillis = 180_000) {
            nodesWithText("Bitcoin (BTC)") ||
                nodesWithText("Couldn't load", substring = true)
        }

        compose.onNodeWithText("Bitcoin (BTC)").assertIsDisplayed()
        compose.onNodeWithText("Couldn't load", substring = true).assertDoesNotExist()
    }
}
