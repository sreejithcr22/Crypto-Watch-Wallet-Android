package com.codit.cryptowatchwallet.ui.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWalletScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showCoinPicker by remember { mutableStateOf(false) }
    var showBackConfirm by remember { mutableStateOf(false) }

    val coinOptions = remember {
        context.resources.getStringArray(R.array.add_wallet_spinner_items).toList()
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan failed, Please try again !", Toast.LENGTH_LONG).show()
        } else {
            viewModel.onAddressChange(result.contents)
        }
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(context, "Wallet added successfully", Toast.LENGTH_SHORT).show()
            onSuccess()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add wallet") },
                navigationIcon = {
                    TextButton(onClick = { showBackConfirm = true }) { Text("Back") }
                },
                actions = {
                    TextButton(onClick = { viewModel.clear() }) { Text("Clear") }
                    TextButton(onClick = { viewModel.submit() }) { Text("Save") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Wallet name") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = showCoinPicker,
                onExpandedChange = { showCoinPicker = !showCoinPicker }
            ) {
                OutlinedTextField(
                    value = state.coinLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Coin") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCoinPicker) },
                    isError = state.coinError != null,
                    supportingText = { state.coinError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showCoinPicker,
                    onDismissRequest = { showCoinPicker = false }
                ) {
                    coinOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                viewModel.onCoinChange(option)
                                showCoinPicker = false
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address") },
                isError = state.addressError != null,
                supportingText = { state.addressError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row {
                OutlinedButton(
                    onClick = {
                        try {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            if (clipboard.hasPrimaryClip()) {
                                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                                if (text.isNotEmpty()) viewModel.onAddressChange(text)
                                else Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "Clipboard empty", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Paste") }
                Spacer(Modifier.padding(4.dp))
                OutlinedButton(
                    onClick = {
                        scanLauncher.launch(
                            ScanOptions().apply {
                                setPrompt("Scan address")
                                setOrientationLocked(false)
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Scan QR") }
            }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submit() },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Add wallet")
                }
            }
        }
    }

    if (showBackConfirm) {
        AlertDialog(
            onDismissRequest = { showBackConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    showBackConfirm = false
                    onBack()
                }) { Text("BACK") }
            },
            dismissButton = {
                TextButton(onClick = { showBackConfirm = false }) { Text("CANCEL") }
            },
            text = { Text("Are you sure you want to go back?") }
        )
    }
}
