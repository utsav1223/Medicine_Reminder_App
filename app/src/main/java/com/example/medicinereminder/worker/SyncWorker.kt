package com.example.medicinereminder.worker

import android.content.Context
import androidx.work.*
import com.example.medicinereminder.data.local.database.MedicineDatabase
import com.example.medicinereminder.data.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
        
        val database = MedicineDatabase.getDatabase(applicationContext)
        val syncRepository = SyncRepository(
            medicineDao = database.medicineDao(),
            reminderDao = database.reminderDao(),
            analyticsDao = database.analyticsDao()
        )
        
        return try {
            syncRepository.syncMedicines(userId)
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "SyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }
        
        fun runOnce(context: Context) {
             val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(syncRequest)
        }
    }
}
