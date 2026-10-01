package com.codit.cryptowatchwallet.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codit.cryptowatchwallet.R

@Composable
fun SettingsScreen(
    onCurrencyClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCurrency by remember { mutableStateOf(false) }
    var showDonate by remember { mutableStateOf(false) }
    var showCredits by remember { mutableStateOf(false) }

    val currencies = remember {
        context.resources.getStringArray(R.array.currencies).sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        SettingRow("Currency", currency) { showCurrency = true }
        SettingRow("Rate us", "Rate the app on playstore") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
            context.startActivity(intent)
        }
        SettingRow("Share app", "Spread the word") {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=${context.packageName}")
            }
            context.startActivity(intent)
        }
        SettingRow("Feedback", "codit.apps@gmail.com") {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "codit.apps@gmail.com", null)).apply {
                putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            }
            context.startActivity(Intent.createChooser(intent, "Send feedback"))
        }
        SettingRow("Donate", "Donate using crypto currency") { showDonate = true }
        SettingRow("Credits", null) { showCredits = true }
    }

    if (showCurrency) {
        AlertDialog(
            onDismissRequest = { showCurrency = false },
            confirmButton = {},
            title = { Text("Change currency") },
            text = {
                Column {
                    currencies.forEach { code ->
                        ListItem(
                            headlineContent = { Text(code) },
                            trailingContent = { RadioButton(selected = code == currency, onClick = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setCurrency(code)
                                    showCurrency = false
                                }
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCurrency = false }) { Text("Cancel") }
            }
        )
    }

    if (showDonate) {
        val addresses = remember {
            linkedMapOf(
                "Bitcoin" to context.getString(R.string.bitcoin_address),
                "Litecoin" to context.getString(R.string.lite_address),
                "Ripple" to context.getString(R.string.ripple_address),
                "Ethereum" to context.getString(R.string.eth_address)
            )
        }
        AlertDialog(
            onDismissRequest = { showDonate = false },
            confirmButton = {
                TextButton(onClick = { showDonate = false }) { Text("Dismiss") }
            },
            title = { Text("Copy address") },
            text = {
                Column {
                    addresses.forEach { (name, addr) ->
                        ListItem(
                            headlineContent = { Text(name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    copyToClipboard(context, addr)
                                    Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                                    showDonate = false
                                }
                        )
                    }
                }
            }
        )
    }

    if (showCredits) {
        AlertDialog(
            onDismissRequest = { showCredits = false },
            confirmButton = {
                TextButton(onClick = { showCredits = false }) { Text("Ok") }
            },
            title = { Text("Credits") },
            text = { Text(context.getString(R.string.credits)) }
        )
    }
}

@Composable
private fun SettingRow(title: String, summary: String?, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title)
            summary?.let { Text(it) }
        }
    }
}

private fun copyToClipboard(context: Context, address: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("address", address))
    } catch (_: Exception) {
        Toast.makeText(context, "Address could not be copied !", Toast.LENGTH_SHORT).show()
    }
}
