package com.codit.cryptowatchwallet.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.SettingsRepository
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    val currency: StateFlow<String> = settingsRepository.observeDefaultCurrency()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), settingsRepository.getDefaultCurrency())

    fun setCurrency(code: String) {
        viewModelScope.launch {
            settingsRepository.setDefaultCurrency(code)
            walletRepository.updateAllWalletsWorth(code)
        }
    }
}
