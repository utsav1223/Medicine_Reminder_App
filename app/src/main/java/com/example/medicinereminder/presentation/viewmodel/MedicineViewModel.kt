package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.repository.*
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MedicineViewModel(
    private val repository: MedicineRepository,
    private val authRepository: AuthRepository = AuthRepositoryImpl(),
    private val reminderRepository: ReminderRepository? = null,
    private val aiRepository: AIRepository = AIRepositoryImpl()
) : ViewModel() {

    private val _medicinesState = MutableStateFlow<Resource<List<Medicine>>>(Resource.Loading())
    val medicinesState: StateFlow<Resource<List<Medicine>>> = _medicinesState.asStateFlow()

    private val _interactionWarning = MutableStateFlow<String?>(null)
    val interactionWarning: StateFlow<String?> = _interactionWarning.asStateFlow()

    private val _addMedicineState = MutableStateFlow<Resource<Unit>?>(null)
    val addMedicineState = _addMedicineState.asStateFlow()

    private val _currentMedicine = MutableStateFlow<Resource<Medicine>>(Resource.Loading())
    val currentMedicine: StateFlow<Resource<Medicine>> = _currentMedicine.asStateFlow()

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        getMedicines()
    }

    fun resetAddMedicineState() {
        _addMedicineState.value = null
    }

    fun getMedicines() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            repository.getMedicines(userId).collect {
                _medicinesState.value = it
            }
        }
    }

    fun getMedicineById(medicineId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _currentMedicine.value = Resource.Loading()
            _currentMedicine.value = repository.getMedicineById(medicineId, userId)
        }
    }

    fun addMedicine(medicine: Medicine) {
        viewModelScope.launch {
            _addMedicineState.value = Resource.Loading()
            
            // Check for drug interactions using AI
            val existingMedicines = (medicinesState.value as? Resource.Success)?.data ?: emptyList()
            if (existingMedicines.isNotEmpty()) {
                val medicineNames = existingMedicines.map { it.medicineName } + medicine.medicineName
                val interactionResult = aiRepository.analyzeInteractions(medicineNames)
                if (interactionResult is Resource.Success && interactionResult.data != "No interactions detected.") {
                    _interactionWarning.value = interactionResult.data
                }
            }

            val result = repository.addMedicine(medicine.copy(userId = userId))
            _addMedicineState.value = result
            if (result is Resource.Success) {
                reminderRepository?.scheduleDailyReminders(userId)
            }
        }
    }

    fun updateMedicine(medicine: Medicine) {
        viewModelScope.launch {
            _addMedicineState.value = Resource.Loading()
            val result = repository.updateMedicine(medicine)
            _addMedicineState.value = result
            if (result is Resource.Success) {
                reminderRepository?.scheduleDailyReminders(userId)
            }
        }
    }

    fun deleteMedicine(medicineId: String) {
        viewModelScope.launch {
            repository.deleteMedicine(medicineId, userId)
        }
    }
    
    fun clearWarning() {
        _interactionWarning.value = null
    }
}
