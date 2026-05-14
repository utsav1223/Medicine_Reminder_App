package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.AnalyticsSummary
import com.example.medicinereminder.data.model.HealthInsight
import com.example.medicinereminder.data.repository.AnalyticsRepository
import com.example.medicinereminder.data.repository.AuthRepository
import com.example.medicinereminder.data.repository.AuthRepositoryImpl
import com.example.medicinereminder.utils.Resource
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnalyticsViewModel(
    private val repository: AnalyticsRepository,
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    val modelProducer = CartesianChartModelProducer()

    private val _summaryState = MutableStateFlow<Resource<AnalyticsSummary>>(Resource.Loading())
    val summaryState = _summaryState.asStateFlow()

    private val _insightsState = MutableStateFlow<Resource<List<HealthInsight>>>(Resource.Loading())
    val insightsState = _insightsState.asStateFlow()

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    init {
        loadAnalytics()
        // Mock chart data
        viewModelScope.launch {
            modelProducer.runTransaction {
                columnSeries {
                    series(5, 8, 7, 6, 9, 10, 8)
                }
            }
        }
    }

    fun loadAnalytics() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _summaryState.value = Resource.Loading()
            _summaryState.value = repository.getAnalyticsSummary(userId)
            
            _insightsState.value = Resource.Loading()
            _insightsState.value = repository.getHealthInsights(userId)
        }
    }
}
