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

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        getTodayReminders()
    }

    fun getTodayReminders() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            // Ensure reminders are scheduled for today
            repository.scheduleDailyReminders(userId)
            
            repository.getTodayReminders(userId).collectLatest { result ->
                _remindersState.value = result
            }
        }
    }

    fun updateStatus(reminderId: String, status: ReminderStatus) {
        viewModelScope.launch {
            repository.updateReminderStatus(reminderId, userId, status)
        }
    }

    fun snooze(reminderId: String, minutes: Int) {
        viewModelScope.launch {
            repository.snoozeReminder(reminderId, userId, minutes)
        }
    }
}
