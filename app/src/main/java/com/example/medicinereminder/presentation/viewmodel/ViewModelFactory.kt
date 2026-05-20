package com.example.medicinereminder.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.medicinereminder.data.local.database.MedicineDatabase
import com.example.medicinereminder.data.local.store.UserPreferencesStore
import com.example.medicinereminder.data.repository.*
import com.example.medicinereminder.scheduler.AlarmSchedulerImpl

class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = MedicineDatabase.getDatabase(context)
        val preferencesStore = UserPreferencesStore(context)
        val scheduler = AlarmSchedulerImpl(context)
        
        val medicineDao = database.medicineDao()
        val reminderDao = database.reminderDao()
        val analyticsDao = database.analyticsDao()

        val medicineRepository = MedicineRepositoryImpl(medicineDao)
        val reminderRepository = ReminderRepositoryImpl(reminderDao, medicineDao, scheduler)
        val analyticsRepository = AnalyticsRepositoryImpl(analyticsDao, reminderDao)
        val aiRepository = AIRepositoryImpl()
        val ocrRepository = OCRRepositoryImpl()
        val profileRepository = ProfileRepositoryImpl()
        val syncRepository = SyncRepository(medicineDao, reminderDao, analyticsDao)
        val securityRepository = SecurityRepositoryImpl(preferencesStore)
        val caregiverRepository = CaregiverRepositoryImpl()
        val emergencyRepository = EmergencyRepositoryImpl()
        val familyProfileRepository = FamilyProfileRepositoryImpl()
        val adminRepository = AdminRepositoryImpl()
        
        return when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> {
                SplashViewModel(repository = AuthRepositoryImpl(), preferencesStore = preferencesStore) as T
            }
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository = AuthRepositoryImpl()) as T
            }
            modelClass.isAssignableFrom(ReminderViewModel::class.java) -> {
                ReminderViewModel(reminderRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(reminderRepository) as T
            }
            modelClass.isAssignableFrom(MedicineViewModel::class.java) -> {
                MedicineViewModel(repository = medicineRepository, reminderRepository = reminderRepository) as T
            }
            modelClass.isAssignableFrom(AnalyticsViewModel::class.java) -> {
                AnalyticsViewModel(repository = analyticsRepository) as T
            }
            modelClass.isAssignableFrom(AIChatViewModel::class.java) -> {
                AIChatViewModel(repository = aiRepository) as T
            }
            modelClass.isAssignableFrom(OCRViewModel::class.java) -> {
                OCRViewModel(repository = ocrRepository, aiRepository = aiRepository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository = profileRepository) as T
            }
            modelClass.isAssignableFrom(SyncViewModel::class.java) -> {
                SyncViewModel(syncRepository) as T
            }
            modelClass.isAssignableFrom(SecurityViewModel::class.java) -> {
                SecurityViewModel(securityRepository) as T
            }
            modelClass.isAssignableFrom(PreferencesViewModel::class.java) -> {
                PreferencesViewModel(preferencesStore) as T
            }
            modelClass.isAssignableFrom(NetworkViewModel::class.java) -> {
                NetworkViewModel(com.example.medicinereminder.utils.ConnectivityManagerNetworkMonitor(context)) as T
            }
            modelClass.isAssignableFrom(CaregiverViewModel::class.java) -> {
                CaregiverViewModel(caregiverRepository) as T
            }
            modelClass.isAssignableFrom(EmergencyViewModel::class.java) -> {
                EmergencyViewModel(emergencyRepository) as T
            }
            modelClass.isAssignableFrom(FamilyProfileViewModel::class.java) -> {
                FamilyProfileViewModel(familyProfileRepository) as T
            }
            modelClass.isAssignableFrom(AdminViewModel::class.java) -> {
                AdminViewModel(adminRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
