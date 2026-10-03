package com.codit.cryptowatchwallet.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.MarketRepository
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.model.CoinPrices
import com.codit.cryptowatchwallet.util.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketUiState(
    val prices: List<CoinPrices> = emptyList(),
    val isRefreshing: Boolean = false,
    val query: String = "",
    val currency: String = "USD",
    val errorMessage: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val walletRepository: WalletRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val refreshing = MutableStateFlow(true)
    private val query = MutableStateFlow("")
    private val error = MutableStateFlow<String?>(null)

    /** Single-flight guard: concurrent refreshes stampede the rate-limited API. */
    private var refreshJob: Job? = null

    companion object {
        /** Initial auto-load retries before the error state is shown. */
        private const val INITIAL_ATTEMPTS = 3
        private const val INITIAL_RETRY_BASE_DELAY_MS = 2000L
    }

    val uiState: StateFlow<MarketUiState> = combine(
        marketRepository.observeCoinPrices(),
        settingsRepository.observeDefaultCurrency(),
        refreshing,
        query,
        error
    ) { prices, currency, isRefreshing, q, err ->
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
        MarketUiState(sorted, isRefreshing, q, currency, err)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MarketUiState(isRefreshing = true))

    init {
        // Fresh installs have an empty DB and the periodic worker only runs
        // every 15 min, so load once automatically when there is no cached data.
        // refreshing starts as true so first open shows loading, not empty.
        viewModelScope.launch {
            try {
                val cached: List<CoinPrices> = try {
                    marketRepository.observeCoinPrices().first()
                } catch (_: Throwable) {
                    emptyList()
                }
                if (cached.isEmpty()) {
                    refreshInitialWithRetry()
                } else {
                    refreshing.value = false
                }
            } catch (_: Throwable) {
                try {
                    refreshing.value = false
                } catch (_: Throwable) {
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    /**
     * First-launch load: cold-start network calls (DNS warm-up, API rate
     * limits when the background worker fires at the same time) often fail
     * once and succeed on retry. Keep the loading indicator across retries
     * and only surface the error when every attempt fails.
     */
    private fun refreshInitialWithRetry() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshing.value = true
            error.value = null
            var ok = false
            for (attempt in 1..INITIAL_ATTEMPTS) {
                if (attempt > 1) {
                    try {
                        delay(INITIAL_RETRY_BASE_DELAY_MS * (attempt - 1))
                    } catch (_: Throwable) {
                    }
                }
                ok = try {
                    marketRepository.refreshMarket()
                } catch (_: Throwable) {
                    false
                }
                if (ok) break
            }
            try {
                walletRepository.updateAllWalletsWorth(uiState.value.currency)
            } catch (_: Throwable) {
            }
            if (!ok) {
                error.value = "Couldn't load market data. Check your connection and try again."
            }
            refreshing.value = false
        }
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshing.value = true
            error.value = null
            try {
                val ok = try {
                    marketRepository.refreshMarket()
                } catch (_: Throwable) {
                    false
                }
                try {
                    walletRepository.updateAllWalletsWorth(
                        uiState.value.currency
                    )
                } catch (_: Throwable) {
                }
                if (!ok) {
                    error.value = "Couldn't load market data. Check your connection and try again."
                }
            } catch (_: Throwable) {
                error.value = "Couldn't load market data. Check your connection and try again."
            } finally {
                refreshing.value = false
            }
        }
    }
}
