package com.codit.cryptowatchwallet.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.model.Wallet
import com.codit.cryptowatchwallet.util.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletListUiState(
    val wallets: List<Wallet> = emptyList(),
    val isRefreshing: Boolean = false,
    val query: String = "",
    val currency: String = "USD"
)

@HiltViewModel
class WalletListViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<WalletListUiState> = combine(
        walletRepository.observeWallets(),
        settingsRepository.observeDefaultCurrency(),
        refreshing,
        query
    ) { wallets, currency, isRefreshing, q ->
        val filtered = if (q.isBlank()) wallets else wallets.filter {
            it.displayName.contains(q, ignoreCase = true) ||
                it.coinCode.contains(q, ignoreCase = true)
        }
        val sorted = filtered.sortedByDescending { Coin.worthAsDouble(it.coinWorth) }
        WalletListUiState(sorted, isRefreshing, q, currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WalletListUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            try {
                walletRepository.refreshWalletsAndWorth(refreshBalances = true)
            } finally {
                refreshing.value = false
            }
        }
    }

    fun onCurrencyChanged() {
        // Worth recalculation happens in repository after currency change;
        // trigger a lightweight revaluation here.
        viewModelScope.launch {
            refreshing.value = true
            try {
                walletRepository.refreshWalletsAndWorth(refreshBalances = false)
            } finally {
                refreshing.value = false
            }
        }
    }
}
