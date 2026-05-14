package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.repository.SyncRepository
import com.example.medicinereminder.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(private val repository: SyncRepository) : ViewModel() {
    private val _syncState = MutableStateFlow<Resource<Unit>?>(null)
    val syncState: StateFlow<Resource<Unit>?> = _syncState.asStateFlow()

    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    fun syncData() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _syncState.value = Resource.Loading()
            _syncState.value = repository.syncMedicines(userId)
        }
    }
}
