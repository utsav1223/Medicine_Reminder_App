package com.example.medicinereminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.receiver.ReminderReceiver

interface AlarmScheduler {
    fun schedule(reminder: ReminderRecord)
    fun cancel(reminder: ReminderRecord)
}

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(reminder: ReminderRecord) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.reminderId)
            putExtra("MEDICINE_NAME", reminder.medicineName)
            putExtra("DOSAGE", reminder.dosage)
            putExtra("USER_ID", reminder.userId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminder.scheduledTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminder.scheduledTime,
                pendingIntent
            )
        }
    }

    override fun cancel(reminder: ReminderRecord) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
