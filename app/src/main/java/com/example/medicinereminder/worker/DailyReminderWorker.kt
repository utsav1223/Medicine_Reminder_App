package com.example.medicinereminder.worker

import android.content.Context
import androidx.work.*
import com.example.medicinereminder.data.repository.ReminderRepositoryImpl
import com.example.medicinereminder.scheduler.AlarmSchedulerImpl
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        val database = com.example.medicinereminder.data.local.database.MedicineDatabase.getDatabase(applicationContext)
        val repository = ReminderRepositoryImpl(
            reminderDao = database.reminderDao(),
            medicineDao = database.medicineDao(),
            scheduler = AlarmSchedulerImpl(applicationContext)
        )
        
        return try {
            repository.scheduleDailyReminders(userId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "DailyReminderWorker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
