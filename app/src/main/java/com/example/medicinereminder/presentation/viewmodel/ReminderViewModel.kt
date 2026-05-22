package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.data.repository.AuthRepository
import com.example.medicinereminder.data.repository.AuthRepositoryImpl
import com.example.medicinereminder.data.repository.ReminderRepository
import com.example.medicinereminder.data.repository.ReminderRepositoryImpl
import com.example.medicinereminder.scheduler.AlarmSchedulerImpl
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val repository: ReminderRepository,
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _remindersState = MutableStateFlow<Resource<List<ReminderRecord>>>(Resource.Loading())
    val remindersState: StateFlow<Resource<List<ReminderRecord>>> = _remindersState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        loadReminders()
    }

    private fun loadReminders() {
        if (userId.isBlank()) return
        
        // 1. Collect reminders in a long-running coroutine
        viewModelScope.launch {
            repository.getTodayReminders(userId).collectLatest { result ->
                _remindersState.value = result
            }
        }
        
        // 2. Schedule reminders in background
        viewModelScope.launch {
            repository.scheduleDailyReminders(userId)
        }
    }

    fun updateStatus(reminderId: String, status: ReminderStatus) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val result = repository.updateReminderStatus(reminderId, userId, status)
            if (result is Resource.Success) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Dose marked as ${status.name.lowercase()}"))
            } else {
                _eventFlow.emit(UiEvent.ShowSnackbar(result.message ?: "Failed to update status"))
            }
        }
    }

    fun snooze(reminderId: String, minutes: Int) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val result = repository.snoozeReminder(reminderId, userId, minutes)
            if (result is Resource.Success) {
                _eventFlow.emit(UiEvent.ShowSnackbar("Dose snoozed for $minutes mins"))
            }
        }
    }
    
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }
}
