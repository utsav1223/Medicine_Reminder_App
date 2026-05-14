package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.local.store.UserPreferencesStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(private val preferencesStore: UserPreferencesStore) : ViewModel() {

    val themeMode: StateFlow<String> = preferencesStore.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val dynamicColors: StateFlow<Boolean> = preferencesStore.dynamicColors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled: StateFlow<Boolean> = preferencesStore.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val onboardingCompleted: StateFlow<Boolean> = preferencesStore.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesStore.setThemeMode(mode) }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { preferencesStore.setDynamicColors(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesStore.setNotificationsEnabled(enabled) }
    }

    fun setOnboardingCompleted(completed: Boolean) {
        viewModelScope.launch { preferencesStore.setOnboardingCompleted(completed) }
    }
}
