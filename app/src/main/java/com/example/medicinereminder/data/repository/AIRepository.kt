package com.example.medicinereminder.data.repository

import com.example.medicinereminder.data.model.ChatMessage
import com.example.medicinereminder.utils.Resource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface AIRepository {
    suspend fun getChatResponse(message: String): Resource<String>
    suspend fun analyzeInteractions(medicines: List<String>): Resource<String>
    suspend fun parsePrescription(rawText: String): Resource<String>
}

class AIRepositoryImpl : AIRepository {
    
    // IMPORTANT: Get your API key from https://aistudio.google.com/
    // and replace "YOUR_API_KEY_HERE" with your actual key.
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-pro",
        apiKey = "API_KEY",
        generationConfig = generationConfig {
            temperature = 1f
            topP = 0.95f
            topK = 64
        }
    )

    private val systemPrompt = """
        You are a highly specialized Medical and Pharmaceutical Assistant for the 'MedReminder' app.
        Your primary goal is to help users understand their medications, health conditions, and wellness.
        
        STRICT RULES:
        1. ONLY answer questions related to medicine, health, pharmacy, nutrition, and medical conditions.
        2. If the user asks about anything outside these fields (e.g., coding, politics, sports, general history, etc.), 
           politely but firmly decline by saying: "I am your specialized medical assistant. I can only provide information related to health and medications."
        3. ALWAYS include a brief medical disclaimer at the end of every response: "Disclaimer: This is for informational purposes only. Please consult a qualified healthcare professional before making any medical decisions."
        4. Be professional, empathetic, and concise.
        5. If asked about medication dosage, advise the user to check their prescription or consult their pharmacist.
    """.trimIndent()

    override suspend fun getChatResponse(message: String): Resource<String> {
        return try {
            val response = generativeModel.generateContent(
                content {
                    text("$systemPrompt\n\nUser Question: $message")
                }
            )
            Resource.Success(response.text ?: "I'm sorry, I couldn't process that.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to connect to AI assistant. Please check your internet connection or API key.")
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

    override suspend fun parsePrescription(rawText: String): Resource<String> {
        return try {
            val prompt = """
                Extract medication details from the following prescription text. 
                Return ONLY a JSON object in this exact format:
                {
                  "name": "string",
                  "type": "string (Tablet/Capsule/Syrup/Injection/Drops)",
                  "dosage": "string (e.g. 1 Tablet, 5ml)",
                  "timings": ["HH:mm AM/PM"]
                }
                If multiple medicines are found, just return the first one. 
                If a field is not found, use empty string or empty array.
                
                Prescription Text:
                $rawText
            """.trimIndent()
            
            val response = generativeModel.generateContent(prompt)
            val jsonResponse = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            Resource.Success(jsonResponse ?: "{}")
        } catch (e: Exception) {
            Resource.Error("AI parsing failed")
        }
    }
}
