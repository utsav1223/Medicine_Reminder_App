package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.Caregiver
import com.example.medicinereminder.data.model.CaregiverStatus
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface CaregiverRepository {
    suspend fun inviteCaregiver(email: String, senderName: String, senderId: String): Resource<Unit>
    suspend fun acceptInvitation(caregiverId: String, patientId: String): Resource<Unit>
    fun getPatients(caregiverId: String): Flow<Resource<List<Caregiver>>>
    fun monitorPatientMedicines(patientId: String): Flow<Resource<List<Medicine>>>
    fun monitorPatientReminders(patientId: String): Flow<Resource<List<ReminderRecord>>>
}

class CaregiverRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CaregiverRepository {

    override suspend fun inviteCaregiver(email: String, senderName: String, senderId: String): Resource<Unit> {
        return try {
            val inviteId = firestore.collection("caregiver_invites").document().id
            val invite = hashMapOf(
                "inviteId" to inviteId,
                "patientId" to senderId,
                "patientName" to senderName,
                "caregiverEmail" to email,
                "status" to "PENDING",
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("caregiver_invites").document(inviteId).set(invite).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to invite caregiver")
        }
    }

    override suspend fun acceptInvitation(caregiverId: String, patientId: String): Resource<Unit> {
        return try {
            val caregiverRef = firestore.collection("caregivers").document(caregiverId)
            val caregiverDoc = caregiverRef.get().await()
            
            if (caregiverDoc.exists()) {
                val currentPatients = caregiverDoc.get("patientIds") as? List<String> ?: emptyList()
                caregiverRef.update("patientIds", currentPatients + patientId).await()
            } else {
                val newCaregiver = Caregiver(
                    caregiverId = caregiverId,
                    patientIds = listOf(patientId),
                    status = CaregiverStatus.ACCEPTED
                )
                caregiverRef.set(newCaregiver).await()
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to accept invitation")
        }
    }

    override fun getPatients(caregiverId: String): Flow<Resource<List<Caregiver>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("caregivers")
            .document(caregiverId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val caregiver = snapshot?.toObject(Caregiver::class.java)
                // In a real app, we'd fetch the User profiles for each patientId here
                trySend(Resource.Success(listOfNotNull(caregiver)))
            }
        awaitClose { listener.remove() }
    }

    override fun monitorPatientMedicines(patientId: String): Flow<Resource<List<Medicine>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users")
            .document(patientId)
            .collection("medicines")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val medicines = snapshot?.toObjects(Medicine::class.java) ?: emptyList()
                trySend(Resource.Success(medicines))
            }
        awaitClose { listener.remove() }
    }

    override fun monitorPatientReminders(patientId: String): Flow<Resource<List<ReminderRecord>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users")
            .document(patientId)
            .collection("reminders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val reminders = snapshot?.toObjects(ReminderRecord::class.java) ?: emptyList()
                trySend(Resource.Success(reminders))
            }
        awaitClose { listener.remove() }
    }
}
