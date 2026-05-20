package com.example.medicinereminder.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.repository.AIRepository
import com.example.medicinereminder.data.repository.OCRRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

data class OCRUiState(
    val extractedText: String = "",
    val parsedJson: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val imageUri: Uri? = null,
    val capturedBitmap: Bitmap? = null
)

class OCRViewModel(
    private val repository: OCRRepository,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OCRUiState())
    val uiState: StateFlow<OCRUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onImageSelected(uri: Uri, context: Context) {
        _uiState.value = _uiState.value.copy(imageUri = uri, isLoading = true, error = null)
        viewModelScope.launch {
            val ocrResult = repository.recognizeText(uri, context)
            handleOcrResult(ocrResult)
        }
    }

    private suspend fun handleOcrResult(result: Resource<String>) {
        when (result) {
            is Resource.Success -> {
                val rawText = result.data ?: ""
                val parseResult = aiRepository.parsePrescription(rawText)
                if (parseResult is Resource.Success) {
                    _uiState.value = _uiState.value.copy(
                        extractedText = rawText,
                        parsedJson = parseResult.data ?: "{}",
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        extractedText = rawText,
                        isLoading = false,
                        error = "OCR success, but AI parsing failed"
                    )
                }
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

    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(capturedBitmap = bitmap, isLoading = true, error = null)
        viewModelScope.launch {
            val ocrResult = repository.recognizeText(bitmap)
            handleOcrResult(ocrResult)
        }
    }

    fun clearState() {
        _uiState.value = OCRUiState()
    }

    sealed class UiEvent {
        data class NavigateToAddMedicine(val json: String) : UiEvent()
    }
}
