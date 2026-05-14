package com.example.medicinereminder.data.local.dao

import androidx.room.*
import com.example.medicinereminder.data.local.entity.DailyAnalyticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: DailyAnalyticsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsList(analyticsList: List<DailyAnalyticsEntity>)

    @Query("SELECT * FROM daily_analytics WHERE userId = :userId ORDER BY date DESC")
    fun getAllAnalytics(userId: String): Flow<List<DailyAnalyticsEntity>>

    @Query("SELECT * FROM daily_analytics WHERE userId = :userId AND date = :date")
    suspend fun getAnalyticsForDate(userId: String, date: Long): DailyAnalyticsEntity?

    @Query("SELECT * FROM daily_analytics WHERE isSynced = 0")
    suspend fun getUnsyncedAnalytics(): List<DailyAnalyticsEntity>
}
