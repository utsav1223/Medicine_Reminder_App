package com.example.medicinereminder.data.model

data class DailyAnalytics(
    val date: Long = 0,
    val totalScheduled: Int = 0,
    val takenCount: Int = 0,
    val missedCount: Int = 0,
    val skippedCount: Int = 0,
    val adherencePercentage: Float = 0f,
    val lastModified: Long = System.currentTimeMillis()
)

data class AnalyticsSummary(
    val totalTaken: Int = 0,
    val totalMissed: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val averageAdherence: Float = 0f,
    val weeklyProgress: List<Float> = emptyList(),
    val monthlyAdherence: Float = 0f
)

data class HealthInsight(
    val title: String,
    val description: String,
    val type: InsightType
)

enum class InsightType {
    POSITIVE, NEUTRAL, WARNING
}
