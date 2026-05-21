package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.local.dao.MedicineDao
import com.example.medicinereminder.data.local.dao.ReminderDao
import com.example.medicinereminder.data.local.entity.toEntity
import com.example.medicinereminder.data.local.entity.toMedicine
import com.example.medicinereminder.data.local.entity.toReminderRecord
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.scheduler.AlarmScheduler
import com.example.medicinereminder.utils.DateUtils
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao,
    private val medicineDao: MedicineDao,
    private val scheduler: AlarmScheduler
) : ReminderRepository {

    override suspend fun scheduleDailyReminders(userId: String): Resource<Unit> {
        return try {
            val now = System.currentTimeMillis()
            val todayStart = DateUtils.getStartOfDay()
            val tomorrowStart = DateUtils.getStartOfDay(DateUtils.getNextDay())
            
            val daysToSchedule = listOf(todayStart, tomorrowStart)

            // 1. Get all active medicines from local Room
            val medicines = medicineDao.getMedicines(userId).first()
                .map { it.toMedicine() }

            // 2. For each medicine, create reminders for today and tomorrow if not already created
            for (medicine in medicines) {
                for (startOfDay in daysToSchedule) {
                    val endOfDay = DateUtils.getEndOfDay(startOfDay)
                    
                    if (medicine.startDate > endOfDay || medicine.endDate < startOfDay) continue

                    for (time in medicine.timingList) {
                        val scheduledTime = DateUtils.getTimeInMillis(time, startOfDay)
                        
                        if (scheduledTime < now) continue 

                        val reminderId = "${medicine.medicineId}_${startOfDay}_${time.replace(" ", "_")}"
                        
                        val existing = reminderDao.getReminderById(reminderId)
                        if (existing == null) {
                            val reminder = ReminderRecord(
                                reminderId = reminderId,
                                medicineId = medicine.medicineId,
                                medicineName = medicine.medicineName,
                                dosage = medicine.dosage,
                                reminderTime = time,
                                reminderDate = startOfDay,
                                scheduledTime = scheduledTime,
                                status = ReminderStatus.PENDING,
                                imageUrl = medicine.imageUrl,
                                userId = userId
                            )
                            reminderDao.insertReminder(reminder.toEntity(isSynced = false))
                            scheduler.schedule(reminder)
                        }
                    }
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to schedule reminders")
        }
    }

    override suspend fun updateReminderStatus(
        reminderId: String,
        userId: String,
        status: ReminderStatus
    ): Resource<Unit> {
        return try {
            val reminder = reminderDao.getReminderById(reminderId) ?: return Resource.Error("Not found")
            val updated = reminder.toReminderRecord().copy(
                status = status,
                completedAt = if (status == ReminderStatus.TAKEN) System.currentTimeMillis() else null
            )
            reminderDao.insertReminder(updated.toEntity(isSynced = false))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status")
        }
    }

    override suspend fun snoozeReminder(
        reminderId: String,
        userId: String,
        minutes: Int
    ): Resource<Unit> {
        return try {
            val entity = reminderDao.getReminderById(reminderId) ?: return Resource.Error("Not found")
            val reminder = entity.toReminderRecord()
            
            val newTime = System.currentTimeMillis() + (minutes * 60 * 1000)
            val updatedReminder = reminder.copy(
                scheduledTime = newTime,
                status = ReminderStatus.SNOOZED
            )
            
            reminderDao.insertReminder(updatedReminder.toEntity(isSynced = false))
            scheduler.schedule(updatedReminder)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to snooze")
        }
    }

    override fun getTodayReminders(userId: String): Flow<Resource<List<ReminderRecord>>> = flow {
        emit(Resource.Loading())
        val startOfDay = DateUtils.getStartOfDay()
        reminderDao.getRemindersForDate(userId, startOfDay).collect { entities ->
            emit(Resource.Success(entities.map { it.toReminderRecord() }))
        }
    }

    override fun getReminderHistory(userId: String): Flow<Resource<List<ReminderRecord>>> = flow {
        emit(Resource.Loading())
        reminderDao.getReminderHistory(userId).collect { entities ->
            emit(Resource.Success(entities.map { it.toReminderRecord() }))
        }
    }

    override suspend fun getPendingReminders(userId: String): List<ReminderRecord> {
        // This is used for syncing/debugging, can be simplified
        return emptyList()
    }
}
