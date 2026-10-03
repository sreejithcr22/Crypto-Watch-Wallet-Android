package com.codit.cryptowatchwallet

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codit.cryptowatchwallet.activity.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end add-wallet test using a real public BTC address.
 *
 * Address: Bitcoin genesis block reward address
 * (1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa) — viewable on any public block
 * chain explorer, e.g.
 * https://www.blockchain.com/explorer/addresses/btc/1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa
 * Balance is fetched live from api.blockcypher.com, so this test needs
 * internet on the emulator/device.
 */
@RunWith(AndroidJUnit4::class)
class AddWalletFlowTest {

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

    private fun nodesWithContentDescription(desc: String) = try {
        compose.onAllNodesWithContentDescription(desc)
            .fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        // No compose hierarchy attached yet (cold start); keep polling.
        false
    }

    private fun nodesWithText(text: String, substring: Boolean = false) = try {
        compose.onAllNodesWithText(text, substring = substring)
            .fetchSemanticsNodes().isNotEmpty()
    } catch (_: IllegalStateException) {
        false
    }

    @Test
    fun addWalletWithPublicBtcAddress() {
        val walletName = "Satoshi ${System.currentTimeMillis() % 100000}"
        val genesisAddress = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"

        // Wallets tab is the start destination; open the add form via the FAB.
        // Cold first-install starts are slow, so wait for content to attach.
        compose.waitUntil(timeoutMillis = 60_000) {
            nodesWithContentDescription("Add wallet")
        }
        compose.onNodeWithContentDescription("Add wallet").performClick()

        // Fill the form.
        compose.onNode(hasSetTextAction() and hasText("Wallet name"))
            .performTextInput(walletName)
        // The readOnly coin field exposes its label (not the placeholder) in
        // semantics; tapping it expands the dropdown.
        compose.onNodeWithText("Coin").performClick()
        compose.onNodeWithText("Bitcoin (BTC)").performClick()
        compose.onNode(hasSetTextAction() and hasText("Address"))
            .performTextInput(genesisAddress)

        // Submit ("Add Wallet" title has no click action; the button does).
        compose.onNode(hasText("Add Wallet") and hasClickAction()).performClick()

        // Success pops back to the wallet list showing the new wallet.
        // Balance lookup + follow-up market refresh hit live APIs.
        compose.waitUntil(timeoutMillis = 180_000) {
            nodesWithText(walletName)
        }
        compose.onNodeWithText(walletName).assertIsDisplayed()
    }
}
