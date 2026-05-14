package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.*
import com.example.medicinereminder.data.repository.CaregiverRepository
import com.example.medicinereminder.data.repository.EmergencyRepository
import com.example.medicinereminder.data.repository.FamilyProfileRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CaregiverViewModel(private val repository: CaregiverRepository) : ViewModel() {
    private val _inviteState = MutableStateFlow<Resource<Unit>?>(null)
    val inviteState = _inviteState.asStateFlow()

    fun inviteCaregiver(email: String, senderName: String, senderId: String) {
        viewModelScope.launch {
            _inviteState.value = Resource.Loading()
            _inviteState.value = repository.inviteCaregiver(email, senderName, senderId)
        }
    }

    fun getPatientData(patientId: String) = combine(
        repository.monitorPatientMedicines(patientId),
        repository.monitorPatientReminders(patientId)
    ) { medicines, reminders ->
        Pair(medicines, reminders)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(Resource.Loading(), Resource.Loading()))
}

class EmergencyViewModel(private val repository: EmergencyRepository) : ViewModel() {
    private val _emergencyContacts = MutableStateFlow<Resource<List<EmergencyContact>>>(Resource.Loading())
    val emergencyContacts = _emergencyContacts.asStateFlow()

    fun loadContacts(userId: String) {
        viewModelScope.launch {
            repository.getEmergencyContacts(userId).collect { _emergencyContacts.value = it }
        }
    }

    fun triggerSOS(userId: String, userName: String) {
        viewModelScope.launch {
            val alert = EmergencyAlert(
                userId = userId,
                userName = userName,
                type = AlertType.SOS,
                message = "Emergency SOS triggered by $userName"
            )
            repository.triggerEmergencyAlert(alert)
        }
    }

    fun addContact(contact: EmergencyContact) {
        viewModelScope.launch { repository.addEmergencyContact(contact) }
    }
}

class FamilyProfileViewModel(private val repository: FamilyProfileRepository) : ViewModel() {
    private val _profiles = MutableStateFlow<Resource<List<FamilyProfile>>>(Resource.Loading())
    val profiles = _profiles.asStateFlow()

    private val _currentProfile = MutableStateFlow<FamilyProfile?>(null)
    val currentProfile = _currentProfile.asStateFlow()

    fun loadProfiles(userId: String) {
        viewModelScope.launch {
            repository.getFamilyProfiles(userId).collect {
                _profiles.value = it
                if (_currentProfile.value == null && it is Resource.Success) {
                    _currentProfile.value = it.data?.find { p -> p.isPrimary } ?: it.data?.firstOrNull()
                }
            }
        }
    }

    fun switchProfile(profile: FamilyProfile) {
        _currentProfile.value = profile
    }

    fun addProfile(profile: FamilyProfile) {
        viewModelScope.launch { repository.addProfile(profile) }
    }
}

class AdminViewModel(private val repository: com.example.medicinereminder.data.repository.AdminRepository) : ViewModel() {
    private val _analytics = MutableStateFlow<Resource<AdminAnalytics>>(Resource.Loading())
    val analytics = _analytics.asStateFlow()

    private val _users = MutableStateFlow<Resource<List<com.example.medicinereminder.data.model.User>>>(Resource.Loading())
    val users = _users.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _analytics.value = Resource.Loading()
            _users.value = Resource.Loading()
            _analytics.value = repository.getSystemAnalytics()
            _users.value = repository.getAllUsers()
        }
    }
}
