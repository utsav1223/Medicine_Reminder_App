package com.example.medicinereminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.data.repository.ReminderRepositoryImpl
import com.example.medicinereminder.scheduler.AlarmSchedulerImpl
import com.example.medicinereminder.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: return
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("DOSAGE") ?: ""
        val userId = intent.getStringExtra("USER_ID") ?: ""

        val notificationHelper = NotificationHelper(context)
        val database = com.example.medicinereminder.data.local.database.MedicineDatabase.getDatabase(context)
        val repository = ReminderRepositoryImpl(
            reminderDao = database.reminderDao(),
            medicineDao = database.medicineDao(),
            scheduler = AlarmSchedulerImpl(context)
        )

        when (intent.action) {
            NotificationHelper.ACTION_TAKEN -> {
                notificationHelper.cancelNotification(reminderId)
                CoroutineScope(Dispatchers.IO).launch {
                    repository.updateReminderStatus(reminderId, userId, ReminderStatus.TAKEN)
                }
            }
            NotificationHelper.ACTION_SNOOZE -> {
                notificationHelper.cancelNotification(reminderId)
                CoroutineScope(Dispatchers.IO).launch {
                    repository.snoozeReminder(reminderId, userId, 10) // Snooze for 10 mins
                }
            }
            else -> {
                // Triggered by AlarmManager
                notificationHelper.showNotification(reminderId, medicineName, dosage, userId)
            }
        }
    }
}
