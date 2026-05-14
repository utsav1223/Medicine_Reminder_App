package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.UserProfile
import com.example.medicinereminder.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface ProfileRepository {
    suspend fun getUserProfile(): Resource<UserProfile>
    suspend fun updateUserProfile(profile: UserProfile): Resource<Unit>
    fun getProfileFlow(): Flow<Resource<UserProfile>>
}

class ProfileRepositoryImpl : ProfileRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    override suspend fun getUserProfile(): Resource<UserProfile> {
        return try {
            val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            val document = usersCollection.document(userId).get().await()
            val profile = document.toObject(UserProfile::class.java) ?: UserProfile(userId = userId)
            Resource.Success(profile)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch profile")
        }
    }

    override suspend fun updateUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Resource.Error("User not logged in")
            usersCollection.document(userId).set(profile).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override fun getProfileFlow(): Flow<Resource<UserProfile>> = callbackFlow {
        var subscription: com.google.firebase.firestore.ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            subscription?.remove()
            val userId = firebaseAuth.currentUser?.uid
            
            if (userId != null) {
                subscription = usersCollection.document(userId).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Snapshot error"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val profile = snapshot.toObject(UserProfile::class.java)
                        if (profile != null) {
                            trySend(Resource.Success(profile))
                        }
                    } else {
                        trySend(Resource.Success(UserProfile(userId = userId)))
                    }
                }
            } else {
                trySend(Resource.Success(UserProfile()))
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            auth.removeAuthStateListener(authListener)
            subscription?.remove()
        }
    }
}
