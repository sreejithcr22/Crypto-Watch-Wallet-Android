package com.codit.cryptowatchwallet.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codit.cryptowatchwallet.R
import com.codit.cryptowatchwallet.ui.add.AddWalletScreen
import com.codit.cryptowatchwallet.ui.details.WalletDetailsScreen
import com.codit.cryptowatchwallet.ui.market.MarketScreen
import com.codit.cryptowatchwallet.ui.navigation.Routes
import com.codit.cryptowatchwallet.ui.settings.SettingsScreen
import com.codit.cryptowatchwallet.ui.settings.SettingsViewModel
import com.codit.cryptowatchwallet.ui.wallet.WalletListScreen
import java.net.URLDecoder

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCurrency by remember { mutableStateOf(false) }

    val tabs = listOf(
        Tab(Routes.WALLETS, "Wallets", Icons.Default.AccountBalanceWallet),
        Tab(Routes.MARKET, "Market", Icons.Default.ShoppingCart),
        Tab(Routes.SETTINGS, "Settings", Icons.Default.Settings)
    )

    val title = when (currentRoute) {
        Routes.MARKET -> "Market"
        Routes.SETTINGS -> "Settings"
        else -> "Wallets"
    }
    val isTopLevel = currentRoute in listOf(Routes.WALLETS, Routes.MARKET, Routes.SETTINGS)
    val showBottomBar = isTopLevel

    Scaffold(
        topBar = {
            // Sub-screens (Add wallet, Details) own their TopAppBar with back
            // navigation, so only show the shared bar for top-level tabs.
            // This keeps a single title + single back button and leaves the
            // system status bar visible.
            if (isTopLevel) {
                TopAppBar(
                    title = { Text(title) },
                    actions = {
                        if (currentRoute in listOf(Routes.WALLETS, Routes.MARKET)) {
                            TextButton(onClick = { showCurrency = true }) {
                                Text(currency)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WALLETS,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.WALLETS) {
                WalletListScreen(
                    onWalletClick = { navController.navigate(Routes.walletDetails(it)) },
                    onAddClick = { navController.navigate(Routes.ADD_WALLET) }
                )
            }
            composable(Routes.MARKET) {
                MarketScreen()
            }
            composable(Routes.SETTINGS) {
                SettingsScreen()
            }
            composable(Routes.ADD_WALLET) {
                AddWalletScreen(
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(
                Routes.WALLET_DETAILS,
                arguments = listOf(navArgument("walletName") { type = NavType.StringType })
            ) { entry ->
                val encoded = entry.arguments?.getString("walletName").orEmpty()
                val decoded = try {
                    URLDecoder.decode(encoded, "UTF-8")
                } catch (_: Exception) {
                    encoded
                }
                // WalletDetailsViewModel reads walletName from SavedStateHandle automatically.
                WalletDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onDeleted = {
                        navController.popBackStack(Routes.WALLETS, inclusive = false)
                    }
                )
            }
        }
    }

    if (showCurrency) {
        val currencies = remember {
            try {
                context.resources.getStringArray(R.array.currencies).sorted()
            } catch (_: Throwable) {
                listOf("USD")
            }
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCurrency = false },
            confirmButton = {},
            title = { Text("Change currency") },
            text = {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(currencies.size) { index ->
                        val code = currencies[index]
                        androidx.compose.material3.ListItem(
                            headlineContent = { Text(code) },
                            trailingContent = {
                                androidx.compose.material3.RadioButton(
                                    selected = code == currency,
                                    onClick = null
                                )
                            },
                            modifier = Modifier.clickable {
                                settingsViewModel.setCurrency(code)
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
}
