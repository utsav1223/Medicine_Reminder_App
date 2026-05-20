package com.example.medicinereminder.data.model

import com.google.firebase.Timestamp

data class Medicine(
    val medicineId: String = "",
    val medicineName: String = "",
    val medicineType: String = "Tablet",
    val dosage: String = "",
    val frequency: String = "Daily",
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000), // Default 1 week
    val timingList: List<String> = emptyList(),
    val notes: String = "",
    val colorTag: Int = 0,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val userId: String = ""
)

enum class MedicineType(val displayName: String) {
    TABLET("Tablet"),
    CAPSULE("Capsule"),
    SYRUP("Syrup"),
    INJECTION("Injection"),
    DROPS("Drops"),
    OTHER("Other")
}
