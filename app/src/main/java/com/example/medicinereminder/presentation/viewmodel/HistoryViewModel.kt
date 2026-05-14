package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.repository.AuthRepository
import com.example.medicinereminder.data.repository.AuthRepositoryImpl
import com.example.medicinereminder.data.repository.ReminderRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*

class HistoryViewModel(
    private val repository: ReminderRepository,
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _historyState = MutableStateFlow<Resource<List<ReminderRecord>>>(Resource.Loading())
    val historyState: StateFlow<Resource<List<ReminderRecord>>> = _historyState.asStateFlow()

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        getHistory()
    }

    fun getHistory() {
        if (userId.isBlank()) return
        repository.getReminderHistory(userId).onEach { result ->
            _historyState.value = result
        }.launchIn(viewModelScope)
    }
}
