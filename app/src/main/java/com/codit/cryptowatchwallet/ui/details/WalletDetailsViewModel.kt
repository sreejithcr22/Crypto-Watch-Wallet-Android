package com.codit.cryptowatchwallet.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.model.Wallet
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletDetailsViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val walletName: String = savedStateHandle["walletName"] ?: ""

    val wallet: StateFlow<Wallet?> = walletRepository.observeWallet(walletName)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteWallet(wallet: Wallet, onDone: () -> Unit) {
        viewModelScope.launch {
            walletRepository.deleteWallet(wallet)
            onDone()
        }
    }
}
