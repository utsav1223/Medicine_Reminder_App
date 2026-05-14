package com.example.medicinereminder.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.example.medicinereminder.utils.Resource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

interface OCRRepository {
    suspend fun recognizeText(imageUri: Uri, context: Context): Resource<String>
    suspend fun recognizeText(bitmap: Bitmap): Resource<String>
}

class OCRRepositoryImpl : OCRRepository {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun recognizeText(imageUri: Uri, context: Context): Resource<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            Resource.Success(result.text)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to recognize text")
        }
    }

    override suspend fun recognizeText(bitmap: Bitmap): Resource<String> {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            Resource.Success(result.text)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to recognize text")
        }
    }
}
