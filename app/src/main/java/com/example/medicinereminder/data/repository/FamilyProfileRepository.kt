package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.FamilyProfile
import com.example.medicinereminder.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface FamilyProfileRepository {
    suspend fun addProfile(profile: FamilyProfile): Resource<Unit>
    fun getFamilyProfiles(userId: String): Flow<Resource<List<FamilyProfile>>>
    suspend fun deleteProfile(profileId: String, userId: String): Resource<Unit>
}

class FamilyProfileRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FamilyProfileRepository {

    override suspend fun addProfile(profile: FamilyProfile): Resource<Unit> {
        return try {
            val docRef = firestore.collection("users").document(profile.userId)
                .collection("family_profiles").document()
            val finalProfile = profile.copy(id = docRef.id)
            docRef.set(finalProfile).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add profile")
        }
    }

    override fun getFamilyProfiles(userId: String): Flow<Resource<List<FamilyProfile>>> = callbackFlow {
        trySend(Resource.Loading())
        val listener = firestore.collection("users").document(userId)
            .collection("family_profiles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(error.message ?: "Error"))
                    return@addSnapshotListener
                }
                val profiles = snapshot?.toObjects(FamilyProfile::class.java) ?: emptyList()
                trySend(Resource.Success(profiles))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteProfile(profileId: String, userId: String): Resource<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("family_profiles").document(profileId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete profile")
        }
    }
}
