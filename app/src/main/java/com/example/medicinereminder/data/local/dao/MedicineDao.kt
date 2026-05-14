package com.example.medicinereminder.data.local.dao

import androidx.room.*
import com.example.medicinereminder.data.local.entity.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getMedicines(userId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE medicineId = :medicineId AND userId = :userId")
    suspend fun getMedicineById(medicineId: String, userId: String): MedicineEntity?

    @Query("SELECT * FROM medicines WHERE isSynced = 0")
    suspend fun getUnsyncedMedicines(): List<MedicineEntity>

    @Query("DELETE FROM medicines WHERE medicineId = :medicineId")
    suspend fun deleteMedicinePermanently(medicineId: String)

    @Query("UPDATE medicines SET isDeleted = 1, isSynced = 0, lastModified = :timestamp WHERE medicineId = :medicineId")
    suspend fun markAsDeleted(medicineId: String, timestamp: Long = System.currentTimeMillis())
}
