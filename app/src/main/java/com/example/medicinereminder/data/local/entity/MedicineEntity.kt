package com.example.medicinereminder.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.medicinereminder.data.model.Medicine

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey
    val medicineId: String,
    val medicineName: String,
    val medicineType: String,
    val dosage: String,
    val frequency: String,
    val startDate: Long,
    val endDate: Long,
    val timingList: String, // Comma separated
    val notes: String,
    val colorTag: Int,
    val createdAt: Long,
    val lastModified: Long,
    val userId: String,
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false
)

fun MedicineEntity.toMedicine(): Medicine {
    return Medicine(
        medicineId = medicineId,
        medicineName = medicineName,
        medicineType = medicineType,
        dosage = dosage,
        frequency = frequency,
        startDate = startDate,
        endDate = endDate,
        timingList = timingList.split(",").filter { it.isNotBlank() },
        notes = notes,
        colorTag = colorTag,
        createdAt = createdAt,
        lastModified = lastModified,
        userId = userId
    )
}

fun Medicine.toEntity(isSynced: Boolean = false, isDeleted: Boolean = false, lastModified: Long = System.currentTimeMillis()): MedicineEntity {
    return MedicineEntity(
        medicineId = medicineId,
        medicineName = medicineName,
        medicineType = medicineType,
        dosage = dosage,
        frequency = frequency,
        startDate = startDate,
        endDate = endDate,
        timingList = timingList.joinToString(","),
        notes = notes,
        colorTag = colorTag,
        createdAt = createdAt,
        lastModified = if (lastModified > this.lastModified) lastModified else this.lastModified,
        userId = userId,
        isSynced = isSynced,
        isDeleted = isDeleted
    )
}
