package com.example.medicinereminder.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medicinereminder.data.model.ChatMessage
import com.example.medicinereminder.data.repository.AIRepository
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AIChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AIChatViewModel(private val repository: AIRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        // Initial AI greeting
        _uiState.update { 
            it.copy(
                messages = listOf(
                    ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = "Hello! I'm your health assistant. How can I help you with your medications today?",
                        isUser = false
                    )
                )
            )
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = text,
            isUser = true
        )

        _uiState.update { 
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = repository.getChatResponse(text)
            when (result) {
                is Resource.Success -> {
                    val aiMessage = ChatMessage(
                        id = UUID.randomUUID().toString(),
                        text = result.data ?: "I'm not sure how to answer that.",
                        isUser = false
                    )
                    _uiState.update { 
                        it.copy(
                            messages = it.messages + aiMessage,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                else -> {}
            }
        }
    }
}
