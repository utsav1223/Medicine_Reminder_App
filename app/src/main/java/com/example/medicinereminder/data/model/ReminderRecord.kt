package com.example.medicinereminder.data.model

data class ReminderRecord(
    val reminderId: String = "",
    val medicineId: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val reminderTime: String = "", // e.g., "08:00 AM"
    val reminderDate: Long = 0,    // Timestamp for the specific day
    val scheduledTime: Long = 0,   // Exact timestamp when it was supposed to trigger
    val status: ReminderStatus = ReminderStatus.PENDING,
    val imageUrl: String? = null,
    val completedAt: Long? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val userId: String = ""
)

enum class ReminderStatus {
    PENDING,
    TAKEN,
    SKIPPED,
    MISSED,
    SNOOZED
}
