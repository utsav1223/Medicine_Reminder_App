package com.example.medicinereminder.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.User
import com.example.medicinereminder.data.repository.AuthRepository
import com.example.medicinereminder.data.repository.AuthRepositoryImpl
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _loading = mutableStateOf(false)
    val loading: State<Boolean> = _loading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val _userState = mutableStateOf<User?>(repository.currentUser)
    val userState: State<User?> = _userState

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val uid = repository.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                when (val result = repository.getUserProfile(uid)) {
                    is Resource.Success -> {
                        _userState.value = result.data
                    }
                    else -> {}
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }

        // Admin hardcoded login logic
        if (email == "admin" && password == "admin123") {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.Navigate(Screen.AdminDashboard.route))
            }
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.login(email, password)) {
                is Resource.Success -> {
                    _userState.value = result.data
                    _eventFlow.emit(UiEvent.Navigate(Screen.Home.route))
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun register(name: String, email: String, password: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        if (password != confirmPass) {
            _error.value = "Passwords do not match"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.register(name, email, password)) {
                is Resource.Success -> {
                    _userState.value = result.data
                    _eventFlow.emit(UiEvent.Navigate(Screen.Home.route))
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun onGoogleSignInResult(idToken: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.googleLogin(idToken)) {
                is Resource.Success -> {
                    _userState.value = result.data
                    _eventFlow.emit(UiEvent.Navigate(Screen.Home.route))
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun onError(message: String) {
        _error.value = message
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _error.value = "Please enter your email"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.resetPassword(email)) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar(result.data ?: "Email sent"))
                    _eventFlow.emit(UiEvent.Navigate(Screen.VerifyOtp.passEmail(email)))
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun resetPasswordWithCode(code: String, newPass: String, confirmPass: String) {
        if (newPass.isBlank() || confirmPass.isBlank()) {
            _error.value = "Please fill all fields"
            return
        }
        if (newPass != confirmPass) {
            _error.value = "Passwords do not match"
            return
        }
        if (newPass.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            when (val result = repository.confirmPasswordReset(code, newPass)) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.ShowSnackbar("Password reset successful. Please sign in."))
                    _eventFlow.emit(UiEvent.Navigate(Screen.Login.route))
                }
                is Resource.Error -> {
                    _error.value = result.message
                }
                else -> {}
            }
            _loading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _eventFlow.emit(UiEvent.Navigate(Screen.AppIntro.route))
        }
    }

    sealed class UiEvent {
        data class Navigate(val route: String) : UiEvent()
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
