package com.example.medicinereminder.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ErrorManager {
    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    suspend fun showError(message: String) {
        _errorFlow.emit(message)
    }
}
