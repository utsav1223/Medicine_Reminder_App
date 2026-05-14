package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SecurityViewModel(private val repository: SecurityRepository) : ViewModel() {

    val isAppLockEnabled: StateFlow<Boolean> = repository.isAppLockEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = repository.isBiometricEnabled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val savedPin: StateFlow<String?> = repository.getAppPin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAppLockEnabled(enabled) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    fun setAppPin(pin: String?) {
        viewModelScope.launch { repository.setAppPin(pin) }
    }

    fun authenticatePin(pin: String): Boolean {
        val isValid = pin == savedPin.value
        if (isValid) _isAuthorized.value = true
        return isValid
    }

    fun authenticateBiometric() {
        _isAuthorized.value = true
    }
    
    fun lockApp() {
        _isAuthorized.value = false
    }
}
