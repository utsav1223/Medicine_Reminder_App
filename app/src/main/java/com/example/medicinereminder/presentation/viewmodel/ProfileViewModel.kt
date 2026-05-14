package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.UserProfile
import com.example.medicinereminder.data.repository.ProfileRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccess: Boolean = false
)

class ProfileViewModel(private val repository: ProfileRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            repository.getProfileFlow().collectLatest { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            profile = result.data ?: UserProfile(),
                            isLoading = false
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }
            }
        }
    }

    fun loadProfile() {
        // Now handled by observeProfile
    }

    fun updateProfile(profile: UserProfile) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, isUpdateSuccess = false)
        viewModelScope.launch {
            when (val result = repository.updateUserProfile(profile)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        profile = profile,
                        isLoading = false,
                        isUpdateSuccess = true
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }
    
    fun resetUpdateSuccess() {
        _uiState.value = _uiState.value.copy(isUpdateSuccess = false)
    }
}
