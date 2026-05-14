package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.User
import com.example.medicinereminder.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    val currentUser: User?
    suspend fun getUserProfile(uid: String): Resource<User>
    suspend fun login(email: String, password: String): Resource<User>
    suspend fun googleLogin(idToken: String): Resource<User>
    suspend fun register(name: String, email: String, password: String): Resource<User>
    suspend fun logout()
    suspend fun resetPassword(email: String): Resource<String>
}

class AuthRepositoryImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUser: User?
        get() = auth.currentUser?.let {
            User(uid = it.uid, email = it.email ?: "", name = it.displayName ?: "")
        }

    override suspend fun getUserProfile(uid: String): Resource<User> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val user = snapshot.toObject(User::class.java)
            if (user != null) Resource.Success(user)
            else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found")
            getUserProfile(uid)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun googleLogin(idToken: String): Resource<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user?.let {
                val newUser = User(uid = it.uid, name = it.displayName ?: "", email = it.email ?: "")
                // Check if user exists in Firestore, if not create
                val snapshot = firestore.collection("users").document(it.uid).get().await()
                if (!snapshot.exists()) {
                    firestore.collection("users").document(it.uid).set(newUser).await()
                }
                newUser
            }
            if (user != null) Resource.Success(user)
            else Resource.Error("Google Login failed: User null")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user?.let {
                val newUser = User(uid = it.uid, name = name, email = email)
                // Save user to Firestore
                firestore.collection("users").document(it.uid).set(newUser).await()
                newUser
            }
            if (user != null) Resource.Success(user)
            else Resource.Error("Registration failed: User null")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun logout() {
        auth.signOut()
    }

    override suspend fun resetPassword(email: String): Resource<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success("Password reset email sent")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An unknown error occurred")
        }
    }
}
