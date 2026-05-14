package com.example.medicinereminder.data.local.dao

import androidx.room.*
import com.example.medicinereminder.data.local.entity.ReminderRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderRecordEntity>)

    @Query("SELECT * FROM reminder_records WHERE userId = :userId AND reminderDate = :date")
    fun getRemindersForDate(userId: String, date: Long): Flow<List<ReminderRecordEntity>>

    @Query("SELECT * FROM reminder_records WHERE reminderId = :reminderId")
    suspend fun getReminderById(reminderId: String): ReminderRecordEntity?

    @Query("SELECT * FROM reminder_records WHERE isSynced = 0")
    suspend fun getUnsyncedReminders(): List<ReminderRecordEntity>

    @Query("SELECT * FROM reminder_records WHERE userId = :userId ORDER BY scheduledTime DESC")
    fun getReminderHistory(userId: String): Flow<List<ReminderRecordEntity>>

    @Query("DELETE FROM reminder_records WHERE reminderId = :reminderId")
    suspend fun deleteReminder(reminderId: String)
}
