package com.example.medicinereminder.data.repository

import android.util.Log
import com.example.medicinereminder.data.local.dao.AnalyticsDao
import com.example.medicinereminder.data.local.dao.MedicineDao
import com.example.medicinereminder.data.local.dao.ReminderDao
import com.example.medicinereminder.data.local.entity.toEntity
import com.example.medicinereminder.data.local.entity.toMedicine
import com.example.medicinereminder.data.local.entity.toReminderRecord
import com.example.medicinereminder.data.local.entity.toDailyAnalytics
import com.example.medicinereminder.data.model.DailyAnalytics
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncRepository(
    private val medicineDao: MedicineDao,
    private val reminderDao: ReminderDao,
    private val analyticsDao: AnalyticsDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun getUserDoc(userId: String) = firestore.collection("users").document(userId)

    suspend fun syncMedicines(userId: String): Resource<Unit> {
        return try {
            // 1. Sync Medicines
            val unsyncedMedicines = medicineDao.getUnsyncedMedicines()
            for (entity in unsyncedMedicines) {
                if (entity.isDeleted) {
                    getUserDoc(userId).collection("medicines").document(entity.medicineId).delete().await()
                    medicineDao.deleteMedicinePermanently(entity.medicineId)
                } else {
                    val medicine = entity.toMedicine()
                    getUserDoc(userId).collection("medicines").document(medicine.medicineId).set(medicine).await()
                    medicineDao.insertMedicine(entity.copy(isSynced = true))
                }
            }
            val remoteMedicines = getUserDoc(userId).collection("medicines").get().await().toObjects(Medicine::class.java)
            for (remote in remoteMedicines) {
                val local = medicineDao.getMedicineById(remote.medicineId, userId)
                if (local == null || remote.lastModified > local.lastModified) {
                    medicineDao.insertMedicine(remote.toEntity(isSynced = true, lastModified = remote.lastModified))
                }
            }

            // 2. Sync Reminders
            val unsyncedReminders = reminderDao.getUnsyncedReminders()
            for (entity in unsyncedReminders) {
                val reminder = entity.toReminderRecord()
                getUserDoc(userId).collection("reminders").document(reminder.reminderId).set(reminder).await()
                reminderDao.insertReminder(entity.copy(isSynced = true))
            }
            val remoteReminders = getUserDoc(userId).collection("reminders").get().await().toObjects(ReminderRecord::class.java)
            for (remote in remoteReminders) {
                val local = reminderDao.getReminderById(remote.reminderId)
                if (local == null || remote.lastModified > local.lastModified) {
                    reminderDao.insertReminder(remote.toEntity(isSynced = true, lastModified = remote.lastModified))
                }
            }

            // 3. Sync Analytics
            val unsyncedAnalytics = analyticsDao.getUnsyncedAnalytics()
            for (entity in unsyncedAnalytics) {
                val analytics = entity.toDailyAnalytics()
                getUserDoc(userId).collection("analytics").document(analytics.date.toString()).set(analytics).await()
                analyticsDao.insertAnalytics(entity.copy(isSynced = true))
            }
            val remoteAnalytics = getUserDoc(userId).collection("analytics").get().await().toObjects(DailyAnalytics::class.java)
            for (remote in remoteAnalytics) {
                val local = analyticsDao.getAnalyticsForDate(userId, remote.date)
                if (local == null || remote.lastModified > local.lastModified) {
                    analyticsDao.insertAnalytics(remote.toEntity(userId, isSynced = true, lastModified = remote.lastModified))
                }
            }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Log.e("SyncRepository", "Sync failed", e)
            Resource.Error(e.message ?: "Sync failed")
        }
    }
}
