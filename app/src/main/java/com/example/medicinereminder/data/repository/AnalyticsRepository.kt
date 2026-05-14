package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.local.dao.AnalyticsDao
import com.example.medicinereminder.data.local.dao.ReminderDao
import com.example.medicinereminder.data.local.entity.toReminderRecord
import com.example.medicinereminder.data.model.*
import com.example.medicinereminder.utils.DateUtils
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.first

interface AnalyticsRepository {
    suspend fun getAnalyticsSummary(userId: String): Resource<AnalyticsSummary>
    suspend fun getWeeklyStats(userId: String): Resource<List<DailyAnalytics>>
    suspend fun getMonthlyStats(userId: String): Resource<List<DailyAnalytics>>
    suspend fun getHealthInsights(userId: String): Resource<List<HealthInsight>>
}

class AnalyticsRepositoryImpl(
    private val analyticsDao: AnalyticsDao,
    private val reminderDao: ReminderDao
) : AnalyticsRepository {

    override suspend fun getAnalyticsSummary(userId: String): Resource<AnalyticsSummary> {
        return try {
            val allReminders = reminderDao.getReminderHistory(userId).first()
                .map { it.toReminderRecord() }

            val totalTaken = allReminders.count { it.status == ReminderStatus.TAKEN }
            val totalMissed = allReminders.count { it.status == ReminderStatus.MISSED }
            
            val dailyGroups = allReminders.groupBy { it.reminderDate }
            val sortedDates = dailyGroups.keys.sortedDescending()
            
            var currentStreak = 0
            val today = DateUtils.getStartOfDay()
            
            for (i in sortedDates.indices) {
                val date = sortedDates[i]
                val dayReminders = dailyGroups[date] ?: emptyList()
                
                if (date == today) {
                    if (dayReminders.any { it.status == ReminderStatus.MISSED }) break
                    if (dayReminders.isNotEmpty() && dayReminders.all { it.status == ReminderStatus.TAKEN }) currentStreak++
                    continue
                }
                
                if (dayReminders.isNotEmpty() && dayReminders.all { it.status == ReminderStatus.TAKEN }) {
                    currentStreak++
                } else if (dayReminders.isNotEmpty()) {
                    break
                }
            }

            val countForAdherence = allReminders.count { it.status != ReminderStatus.PENDING }
            val averageAdherence = if (countForAdherence > 0) {
                (totalTaken.toFloat() / countForAdherence) * 100
            } else 0f

            Resource.Success(AnalyticsSummary(
                totalTaken = totalTaken,
                totalMissed = totalMissed,
                currentStreak = currentStreak,
                averageAdherence = if (averageAdherence.isNaN()) 0f else averageAdherence
            ))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch analytics")
        }
    }

    override suspend fun getWeeklyStats(userId: String): Resource<List<DailyAnalytics>> {
        return Resource.Success(emptyList()) 
    }

    override suspend fun getMonthlyStats(userId: String): Resource<List<DailyAnalytics>> {
        return Resource.Success(emptyList())
    }

    override suspend fun getHealthInsights(userId: String): Resource<List<HealthInsight>> {
        val insights = listOf(
            HealthInsight("Great Consistency!", "You've taken 90% of your medicines this week.", InsightType.POSITIVE),
            HealthInsight("Evening Pattern", "You tend to miss your evening doses more often.", InsightType.WARNING)
        )
        return Resource.Success(insights)
    }
}
