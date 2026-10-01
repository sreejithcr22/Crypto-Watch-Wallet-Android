package com.codit.cryptowatchwallet.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.MarketRepository
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.util.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val prices: List<CoinPrices> = emptyList(),
    val isRefreshing: Boolean = false,
    val query: String = "",
    val currency: String = "USD"
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val walletRepository: WalletRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val refreshing = MutableStateFlow(false)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<MarketUiState> = combine(
        marketRepository.observeCoinPrices(),
        settingsRepository.observeDefaultCurrency(),
        refreshing,
        query
    ) { prices, currency, isRefreshing, q ->
        val filtered = if (q.isBlank()) {
            prices
        } else {
            prices.filter {
                it.coinCode.contains(q, ignoreCase = true) ||
                    Coin.getCoinName(it.coinCode).contains(q, ignoreCase = true)
            }
        }
        val sorted = filtered.sortedByDescending {
            try {
                it.prices[currency] ?: Double.MIN_VALUE
            } catch (_: Exception) {
                Double.MIN_VALUE
            }
        }
        MarketUiState(sorted, isRefreshing, q, currency)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MarketUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun refresh() {
        viewModelScope.launch {
            refreshing.value = true
            try {
                marketRepository.refreshMarket()
                walletRepository.updateAllWalletsWorth(
                    uiState.value.currency
                )
            } finally {
                refreshing.value = false
            }
        }
    }
}
