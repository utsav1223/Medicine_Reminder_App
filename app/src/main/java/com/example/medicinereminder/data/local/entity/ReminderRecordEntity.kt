package com.example.medicinereminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus

@Entity(tableName = "reminder_records")
data class ReminderRecordEntity(
    @PrimaryKey
    val reminderId: String,
    val medicineId: String,
    val medicineName: String,
    val dosage: String,
    val reminderTime: String,
    val reminderDate: Long,
    val scheduledTime: Long,
    val status: String,
    val completedAt: Long?,
    val userId: String,
    val lastModified: Long,
    val isSynced: Boolean = false
)

fun ReminderRecordEntity.toReminderRecord(): ReminderRecord {
    return ReminderRecord(
        reminderId = reminderId,
        medicineId = medicineId,
        medicineName = medicineName,
        dosage = dosage,
        reminderTime = reminderTime,
        reminderDate = reminderDate,
        scheduledTime = scheduledTime,
        status = ReminderStatus.valueOf(status),
        completedAt = completedAt,
        lastModified = lastModified,
        userId = userId
    )
}

fun ReminderRecord.toEntity(isSynced: Boolean = false, lastModified: Long = System.currentTimeMillis()): ReminderRecordEntity {
    return ReminderRecordEntity(
        reminderId = reminderId,
        medicineId = medicineId,
        medicineName = medicineName,
        dosage = dosage,
        reminderTime = reminderTime,
        reminderDate = reminderDate,
        scheduledTime = scheduledTime,
        status = status.name,
        completedAt = completedAt,
        userId = userId,
        lastModified = if (lastModified > this.lastModified) lastModified else this.lastModified,
        isSynced = isSynced
    )
}
