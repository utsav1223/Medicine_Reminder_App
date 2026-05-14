package com.example.medicinereminder.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.repository.OCRRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OCRUiState(
    val extractedText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageUri: Uri? = null,
    val capturedBitmap: Bitmap? = null
)

class OCRViewModel(private val repository: OCRRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(OCRUiState())
    val uiState: StateFlow<OCRUiState> = _uiState.asStateFlow()

    fun onImageSelected(uri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(imageUri = uri, isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.recognizeText(uri, context)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        extractedText = result.data ?: "",
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(capturedBitmap = bitmap, isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = repository.recognizeText(bitmap)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        extractedText = result.data ?: "",
                        isLoading = false
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
                else -> {}
            }
        }
    }

    fun clearState() {
        _uiState.value = OCRUiState()
    }
}
