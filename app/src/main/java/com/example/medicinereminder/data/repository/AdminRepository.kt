package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.AdminAnalytics
import com.example.medicinereminder.utils.Resource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

interface AdminRepository {
    suspend fun getSystemAnalytics(): Resource<AdminAnalytics>
    suspend fun getAllUsers(): Resource<List<com.example.medicinereminder.data.model.User>>
}

class AdminRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {
    override suspend fun getSystemAnalytics(): Resource<AdminAnalytics> {
        return try {
            val usersCount = firestore.collection("users").get().await().size()
            val alertsCount = firestore.collection("emergency_alerts").get().await().size()
            
            Resource.Success(AdminAnalytics(
                totalUsers = usersCount,
                activeAlerts = alertsCount,
                totalMedicinesTracked = usersCount * 5, // Mock data
                averageAdherence = 82.5f
            ))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch analytics")
        }
    }

    override suspend fun getAllUsers(): Resource<List<com.example.medicinereminder.data.model.User>> {
        return try {
            val snapshot = firestore.collection("users").get().await()
            val users = snapshot.toObjects(com.example.medicinereminder.data.model.User::class.java)
            Resource.Success(users)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch users")
        }
    }
}
