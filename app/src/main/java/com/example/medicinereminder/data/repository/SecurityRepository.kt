package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.local.store.UserPreferencesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface SecurityRepository {
    fun isAppLockEnabled(): Flow<Boolean>
    fun isBiometricEnabled(): Flow<Boolean>
    fun getAppPin(): Flow<String?>
    suspend fun setAppLockEnabled(enabled: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setAppPin(pin: String?)
    suspend fun validatePin(pin: String): Boolean
}

class SecurityRepositoryImpl(
    private val preferencesStore: UserPreferencesStore
) : SecurityRepository {

    override fun isAppLockEnabled(): Flow<Boolean> = preferencesStore.appLockEnabled
    override fun isBiometricEnabled(): Flow<Boolean> = preferencesStore.biometricEnabled
    override fun getAppPin(): Flow<String?> = preferencesStore.appPin

    override suspend fun setAppLockEnabled(enabled: Boolean) = preferencesStore.setAppLockEnabled(enabled)
    override suspend fun setBiometricEnabled(enabled: Boolean) = preferencesStore.setBiometricEnabled(enabled)
    override suspend fun setAppPin(pin: String?) = preferencesStore.setAppPin(pin)

    override suspend fun validatePin(pin: String): Boolean {
        val savedPin = preferencesStore.appPin.first()
        return savedPin == pin
    }
}
