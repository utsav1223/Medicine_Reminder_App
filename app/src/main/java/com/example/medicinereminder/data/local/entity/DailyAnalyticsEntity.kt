package com.example.medicinereminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medicinereminder.data.model.DailyAnalytics

@Entity(tableName = "daily_analytics")
data class DailyAnalyticsEntity(
    @PrimaryKey
    val date: Long,
    val userId: String,
    val totalScheduled: Int,
    val takenCount: Int,
    val missedCount: Int,
    val skippedCount: Int,
    val adherencePercentage: Float,
    val lastModified: Long,
    val isSynced: Boolean = false
)

fun DailyAnalyticsEntity.toDailyAnalytics(): DailyAnalytics {
    return DailyAnalytics(
        date = date,
        totalScheduled = totalScheduled,
        takenCount = takenCount,
        missedCount = missedCount,
        skippedCount = skippedCount,
        adherencePercentage = adherencePercentage,
        lastModified = lastModified
    )
}

fun DailyAnalytics.toEntity(userId: String, isSynced: Boolean = false, lastModified: Long = System.currentTimeMillis()): DailyAnalyticsEntity {
    return DailyAnalyticsEntity(
        date = date,
        userId = userId,
        totalScheduled = totalScheduled,
        takenCount = takenCount,
        missedCount = missedCount,
        skippedCount = skippedCount,
        adherencePercentage = adherencePercentage,
        lastModified = if (lastModified > this.lastModified) lastModified else this.lastModified,
        isSynced = isSynced
    )
}
