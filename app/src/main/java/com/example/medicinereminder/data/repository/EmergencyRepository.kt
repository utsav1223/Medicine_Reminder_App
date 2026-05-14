package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.EmergencyAlert
import com.example.medicinereminder.data.model.EmergencyContact
import com.example.medicinereminder.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface EmergencyRepository {
    suspend fun addEmergencyContact(contact: EmergencyContact): Resource<Unit>
    suspend fun deleteEmergencyContact(contactId: String, userId: String): Resource<Unit>
    fun getEmergencyContacts(userId: String): Flow<Resource<List<EmergencyContact>>>
    suspend fun triggerEmergencyAlert(alert: EmergencyAlert): Resource<Unit>
    fun getActiveAlerts(userId: String): Flow<Resource<List<EmergencyAlert>>>
}

class EmergencyRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EmergencyRepository {

    override suspend fun addEmergencyContact(contact: EmergencyContact): Resource<Unit> {
        return try {
            val docRef = firestore.collection("users").document(contact.userId)
                .collection("emergency_contacts").document()
            val finalContact = contact.copy(id = docRef.id)
            docRef.set(finalContact).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add contact")
        }
    }

    override suspend fun deleteEmergencyContact(contactId: String, userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("emergency_contacts").document(contactId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete contact")
        }
    }

    override fun getEmergencyContacts(userId: String): Flow<Resource<List<EmergencyContact>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(userId)
            .collection("emergency_contacts")
            .orderBy("priority")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error fetching contacts"))
                    return@addSnapshotListener
                }
                val contacts = snapshot?.toObjects(EmergencyContact::class.java) ?: emptyList()
                trySend(Resource.Success(contacts))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun triggerEmergencyAlert(alert: EmergencyAlert): Resource<Unit> {
        return try {
            val alertId = firestore.collection("emergency_alerts").document().id
            val finalAlert = alert.copy(id = alertId)
            firestore.collection("emergency_alerts").document(alertId).set(finalAlert).await()
            
            // In a real app, this would trigger a Cloud Function to send FCM push
            // and possibly SMS/Call via Twilio.
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to trigger alert")
        }
    }

    override fun getActiveAlerts(userId: String): Flow<Resource<List<EmergencyAlert>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("emergency_alerts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val alerts = snapshot?.toObjects(EmergencyAlert::class.java) ?: emptyList()
                trySend(Resource.Success(alerts))
            }
        awaitClose { listener.remove() }
    }
}
