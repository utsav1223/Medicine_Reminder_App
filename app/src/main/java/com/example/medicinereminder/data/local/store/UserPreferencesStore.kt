package com.example.medicinereminder.data.local.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesStore(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesStore? = null

        fun getInstance(context: Context): UserPreferencesStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesStore(context.applicationContext).also { INSTANCE = it }
            }
        }

        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val APP_PIN = stringPreferencesKey("app_pin")
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val dynamicColors: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLORS] ?: true }
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val appLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[APP_LOCK_ENABLED] ?: false }
    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val appPin: Flow<String?> = context.dataStore.data.map { it[APP_PIN] }
    val lastSyncTime: Flow<Long> = context.dataStore.data.map { it[LAST_SYNC_TIME] ?: 0L }
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { it[ONBOARDING_COMPLETED] ?: false }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLORS] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAppPin(pin: String?) {
        context.dataStore.edit { 
            if (pin == null) it.remove(APP_PIN)
            else it[APP_PIN] = pin
        }
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_TIME] = time }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }
}
