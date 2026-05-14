package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.repository.AuthRepository
import com.example.medicinereminder.data.repository.AuthRepositoryImpl
import com.example.medicinereminder.presentation.navigation.Screen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl(),
    private val preferencesStore: com.example.medicinereminder.data.local.store.UserPreferencesStore
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            delay(2000) // Animated splash delay
            
            val onboardingCompleted = preferencesStore.onboardingCompleted.first()
            
            if (!onboardingCompleted) {
                _eventFlow.emit(UiEvent.Navigate(Screen.AppIntro.route))
            } else if (repository.currentUser != null) {
                _eventFlow.emit(UiEvent.Navigate(Screen.Home.route))
            } else {
                _eventFlow.emit(UiEvent.Navigate(Screen.Login.route))
            }
        }
    }

    sealed class UiEvent {
        data class Navigate(val route: String) : UiEvent()
    }
}
