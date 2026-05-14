package com.example.medicinereminder.data.model

import com.google.firebase.Timestamp

data class Caregiver(
    val caregiverId: String = "",
    val name: String = "",
    val email: String = "",
    val patientIds: List<String> = emptyList(),
    val invitedAt: Long = System.currentTimeMillis(),
    val status: CaregiverStatus = CaregiverStatus.PENDING
)

enum class CaregiverStatus {
    PENDING, ACCEPTED, REJECTED
}

data class FamilyProfile(
    val id: String = "",
    val name: String = "",
    val relationship: String = "Self", // Parent, Child, Elderly, Patient, Self
    val age: Int? = null,
    val gender: String? = null,
    val bloodGroup: String? = null,
    val photoUrl: String? = null,
    val isPrimary: Boolean = false,
    val userId: String = "" // Link to primary account
)

data class EmergencyContact(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val relationship: String = "",
    val priority: Int = 1,
    val userId: String = ""
)

data class EmergencyAlert(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val type: AlertType = AlertType.SOS,
    val timestamp: Long = System.currentTimeMillis(),
    val location: String? = null,
    val message: String = "",
    val status: AlertStatus = AlertStatus.ACTIVE
)

enum class AlertType {
    SOS, MISSED_CRITICAL_MED, ADHERENCE_DROP, MANUAL
}

enum class AlertStatus {
    ACTIVE, RESOLVED
}

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "general",
    val timestamp: Long = System.currentTimeMillis(),
    val targetUserId: String = "",
    val read: Boolean = false
)

data class AdminAnalytics(
    val totalUsers: Int = 0,
    val totalMedicinesTracked: Int = 0,
    val averageAdherence: Float = 0f,
    val activeAlerts: Int = 0,
    val dailyActiveUsers: Int = 0
)
