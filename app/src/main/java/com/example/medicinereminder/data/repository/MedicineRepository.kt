package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.local.entity.toEntity
import com.example.medicinereminder.data.local.entity.toMedicine
import com.example.medicinereminder.data.local.dao.MedicineDao
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface MedicineRepository {
    suspend fun addMedicine(medicine: Medicine): Resource<Unit>
    suspend fun updateMedicine(medicine: Medicine): Resource<Unit>
    suspend fun deleteMedicine(medicineId: String, userId: String): Resource<Unit>
    fun getMedicines(userId: String): Flow<Resource<List<Medicine>>>
    suspend fun getMedicineById(medicineId: String, userId: String): Resource<Medicine>
}

class MedicineRepositoryImpl(
    private val medicineDao: MedicineDao
) : MedicineRepository {

    override suspend fun addMedicine(medicine: Medicine): Resource<Unit> {
        return try {
            val medicineId = if (medicine.medicineId.isBlank()) 
                java.util.UUID.randomUUID().toString() 
            else medicine.medicineId
            
            val finalMedicine = medicine.copy(
                medicineId = medicineId,
                createdAt = System.currentTimeMillis()
            )
            medicineDao.insertMedicine(finalMedicine.toEntity(isSynced = false))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add medicine")
        }
    }

    override suspend fun updateMedicine(medicine: Medicine): Resource<Unit> {
        return try {
            medicineDao.insertMedicine(medicine.toEntity(isSynced = false))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update medicine")
        }
    }

    override suspend fun deleteMedicine(medicineId: String, userId: String): Resource<Unit> {
        return try {
            medicineDao.markAsDeleted(medicineId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete medicine")
        }
    }

    override fun getMedicines(userId: String): Flow<Resource<List<Medicine>>> = flow {
        emit(Resource.Loading())
        try {
            medicineDao.getMedicines(userId).collect { entities ->
                emit(Resource.Success(entities.map { it.toMedicine() }))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error fetching medicines"))
        }
    }

    override suspend fun getMedicineById(medicineId: String, userId: String): Resource<Medicine> {
        return try {
            val entity = medicineDao.getMedicineById(medicineId, userId)
            if (entity != null) Resource.Success(entity.toMedicine())
            else Resource.Error("Medicine not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error fetching medicine")
        }
    }
}
