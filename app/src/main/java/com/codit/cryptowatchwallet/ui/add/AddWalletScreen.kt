package com.codit.cryptowatchwallet.ui.add

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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

    val coinOptions = remember {
        context.resources.getStringArray(R.array.add_wallet_spinner_items).toList()
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
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

    // While the add is in flight the form is locked: swallowing system back
    // (plus the disabled nav icon and inputs below) guarantees no other
    // operation can interleave with the in-progress submit.
    BackHandler(enabled = state.isLoading) {
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !state.isLoading) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
            Text(
                "Watch any address",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "We only watch addresses — private keys never leave your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Wallet name") },
                placeholder = { Text("e.g. My savings") },
                isError = state.nameError != null,
                supportingText = { state.nameError?.let { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = showCoinPicker && !state.isLoading,
                onExpandedChange = {
                    if (!state.isLoading) showCoinPicker = !showCoinPicker
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.coinLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Coin") },
                    placeholder = { Text("Select coin") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCoinPicker) },
                    isError = state.coinError != null,
                    supportingText = { state.coinError?.let { Text(it) } },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    enabled = !state.isLoading,
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
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                label = { Text("Address") },
                placeholder = { Text("Paste or scan the address") },
                isError = state.addressError != null,
                supportingText = { state.addressError?.let { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = false,
                minLines = 1,
                maxLines = 3,
                enabled = !state.isLoading,
                trailingIcon = {
                    Row {
                        if (state.address.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.onAddressChange("") },
                                enabled = !state.isLoading
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear address")
                            }
                        }
                        IconButton(
                            onClick = {
                                scanLauncher.launch(
                                    ScanOptions().apply {
                                        setPrompt("Scan address")
                                        setOrientationLocked(false)
                                    }
                                )
                            },
                            enabled = !state.isLoading
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { viewModel.submit() },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Adding…")
                } else {
                    Text("Add Wallet")
                }
            }
            }

            // Modal veil: swallows every touch while the add is in flight so
            // no other operation (field edits, scrolling taps) can interleave.
            if (state.isLoading) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.25f)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Adding wallet…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
