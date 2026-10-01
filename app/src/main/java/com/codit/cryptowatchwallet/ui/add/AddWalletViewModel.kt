package com.codit.cryptowatchwallet.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codit.cryptowatchwallet.data.repository.AddWalletResult
import com.codit.cryptowatchwallet.data.repository.WalletRepository
import com.codit.cryptowatchwallet.util.Connectivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddWalletUiState(
    val name: String = "",
    val coinLabel: String = "",
    val address: String = "",
    val nameError: String? = null,
    val coinError: String? = null,
    val addressError: String? = null,
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val connectivity: Connectivity
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddWalletUiState())
    val uiState: StateFlow<AddWalletUiState> = _uiState.asStateFlow()

    fun onNameChange(v: String) {
        _uiState.value = _uiState.value.copy(name = v, nameError = null, errorMessage = null)
    }

    fun onCoinChange(v: String) {
        _uiState.value = _uiState.value.copy(coinLabel = v, coinError = null, errorMessage = null)
    }

    fun onAddressChange(v: String) {
        _uiState.value = _uiState.value.copy(address = v, addressError = null, errorMessage = null)
    }

    fun clear() {
        _uiState.value = AddWalletUiState()
    }

    fun coinCodeOrNull(): String? {
        val label = _uiState.value.coinLabel
        if (label.isBlank()) return null
        return try {
            label.substring(label.indexOf("(") + 1, label.indexOf(")"))
        } catch (_: Exception) {
            null
        }
    }

    fun submit() {
        val current = _uiState.value
        var nameError: String? = null
        var coinError: String? = null
        var addressError: String? = null

        if (current.name.trim().isEmpty()) nameError = "Please enter a name"
        if (current.coinLabel.isEmpty() || coinCodeOrNull() == null) coinError = "Please select a coin"
        if (current.address.trim().isEmpty()) {
            addressError = "Please enter an address"
        } else if (current.address.trim().contains(" ")) {
            addressError = "Address cannot contain whitespaces"
        }

        if (nameError != null || coinError != null || addressError != null) {
            _uiState.value = current.copy(
                nameError = nameError,
                coinError = coinError,
                addressError = addressError
            )
            return
        }

        if (!connectivity.isConnected()) {
            _uiState.value = current.copy(errorMessage = "No internet connection")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            val result = walletRepository.addWallet(
                current.name.trim(),
                coinCodeOrNull()!!,
                current.address.trim()
            )
            when (result) {
                is AddWalletResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                }
                is AddWalletResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
