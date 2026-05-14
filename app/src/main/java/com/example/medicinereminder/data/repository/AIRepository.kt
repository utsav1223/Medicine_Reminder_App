package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.ChatMessage
import com.example.medicinereminder.utils.Resource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AIRepository {
    suspend fun getChatResponse(message: String): Resource<String>
    suspend fun analyzeInteractions(medicines: List<String>): Resource<String>
}

class AIRepositoryImpl : AIRepository {
    
    // In a real app, the API key should be managed securely
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "YOUR_API_KEY_HERE" 
    )

    override suspend fun getChatResponse(message: String): Resource<String> {
        return try {
            val response = generativeModel.generateContent(
                content {
                    text("You are a helpful healthcare assistant for a medicine reminder app. Answer the following user query briefly and professionally: $message")
                }
            )
            Resource.Success(response.text ?: "I'm sorry, I couldn't process that.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to AI assistant")
        }
    }

    override suspend fun analyzeInteractions(medicines: List<String>): Resource<String> {
        if (medicines.size < 2) return Resource.Success("No interactions detected.")
        return try {
            val prompt = "Analyze potential interactions between these medicines: ${medicines.joinToString(", ")}. If there are common minor interactions, mention them briefly. If none, say 'No interactions detected.' Always advise consulting a doctor."
            val response = generativeModel.generateContent(prompt)
            Resource.Success(response.text ?: "No major interactions detected. Consult your doctor.")
        } catch (e: Exception) {
            Resource.Success("Unable to analyze interactions at the moment. Please consult a healthcare professional.")
        }
    }
}
