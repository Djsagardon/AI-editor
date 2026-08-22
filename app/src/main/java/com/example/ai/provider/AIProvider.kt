package com.example.ai.provider

import android.graphics.Bitmap
import com.example.ai.model.ChatMessage
import com.example.ai.model.GeneratedImageResult
import com.example.ai.model.LanguageMode

interface AIProvider {
    val providerName: String

    suspend fun chat(
        messages: List<ChatMessage>,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): Result<String>

    suspend fun streamChat(
        messages: List<ChatMessage>,
        systemInstruction: String? = null,
        onChunk: (String) -> Unit
    ): Result<String>

    suspend fun analyzeImage(
        bitmap: Bitmap,
        prompt: String
    ): Result<String>

    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        stylePreset: String = "Cinematic",
        quality: String = "HD"
    ): Result<GeneratedImageResult>

    suspend fun editImage(
        originalBitmap: Bitmap,
        instruction: String,
        operationType: String = "custom_edit"
    ): Result<GeneratedImageResult>

    suspend fun enhancePrompt(
        rawPrompt: String,
        targetType: String = "image_generation"
    ): Result<String>
}
