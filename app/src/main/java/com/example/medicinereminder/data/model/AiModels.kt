package com.example.medicinereminder.data.model

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val isUser: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val dateOfBirth: Long? = null,
    val gender: String? = null,
    val weight: Float? = null,
    val healthConditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    val bloodGroup: String? = null,
    val notificationEnabled: Boolean = true,
    val themeMode: String = "system", // "light", "dark", "system"
    val dynamicColors: Boolean = true
)
