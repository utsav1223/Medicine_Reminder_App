package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    suspend fun scheduleDailyReminders(userId: String): Resource<Unit>
    suspend fun updateReminderStatus(reminderId: String, userId: String, status: ReminderStatus): Resource<Unit>
    suspend fun snoozeReminder(reminderId: String, userId: String, minutes: Int): Resource<Unit>
    fun getTodayReminders(userId: String): Flow<Resource<List<ReminderRecord>>>
    fun getReminderHistory(userId: String): Flow<Resource<List<ReminderRecord>>>
    suspend fun getPendingReminders(userId: String): List<ReminderRecord>
}
