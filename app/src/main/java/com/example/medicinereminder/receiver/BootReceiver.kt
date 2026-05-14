package com.example.medicinereminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medicinereminder.data.repository.ReminderRepositoryImpl
import com.example.medicinereminder.scheduler.AlarmSchedulerImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || 
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val database = com.example.medicinereminder.data.local.database.MedicineDatabase.getDatabase(context)
            val repository = ReminderRepositoryImpl(
                reminderDao = database.reminderDao(),
                medicineDao = database.medicineDao(),
                scheduler = AlarmSchedulerImpl(context)
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                val pending = repository.getPendingReminders(userId)
                val scheduler = AlarmSchedulerImpl(context)
                pending.forEach { scheduler.schedule(it) }
            }
        }
    }
}
