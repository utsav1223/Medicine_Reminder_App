package com.example.medicinereminder.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.medicinereminder.data.local.dao.AnalyticsDao
import com.example.medicinereminder.data.local.dao.MedicineDao
import com.example.medicinereminder.data.local.dao.ReminderDao
import com.example.medicinereminder.data.local.entity.DailyAnalyticsEntity
import com.example.medicinereminder.data.local.entity.MedicineEntity
import com.example.medicinereminder.data.local.entity.ReminderRecordEntity

@Database(
    entities = [
        MedicineEntity::class,
        ReminderRecordEntity::class,
        DailyAnalyticsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MedicineDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: MedicineDatabase? = null

        fun getDatabase(context: Context): MedicineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicineDatabase::class.java,
                    "medicine_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
