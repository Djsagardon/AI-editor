package com.example.ai.provider

import android.graphics.Bitmap
import com.example.ai.model.ChatMessage
import com.example.ai.model.GeneratedImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class LocalServerAIProvider(
    private val serverUrl: String = "http://localhost:8080/v1"
) : AIProvider {

    override val providerName: String = "Self-Hosted / Local Model Server"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun chat(
        messages: List<ChatMessage>,
        systemInstruction: String?,
        temperature: Float
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messagesArray = JSONArray()
            systemInstruction?.let {
                messagesArray.put(JSONObject().put("role", "system").put("content", it))
            }
            for (msg in messages) {
                messagesArray.put(JSONObject().put("role", msg.role).put("content", msg.text))
            }

            val requestJson = JSONObject()
                .put("model", "local-model")
                .put("messages", messagesArray)
                .put("temperature", temperature.toDouble())

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url("$serverUrl/chat/completions")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Local server returned ${response.code}: $body"))
            }

            val root = JSONObject(body)
            val reply = root.optJSONArray("choices")?.optJSONObject(0)
                ?.optJSONObject("message")?.optString("content") ?: ""

            Result.success(reply)
        } catch (e: Exception) {
            Result.failure(Exception("Could not reach local model server at $serverUrl: ${e.localizedMessage}"))
        }
    }

    override suspend fun streamChat(
        messages: List<ChatMessage>,
        systemInstruction: String?,
        onChunk: (String) -> Unit
    ): Result<String> {
        val result = chat(messages, systemInstruction)
        result.getOrNull()?.let { onChunk(it) }
        return result
    }

    override suspend fun analyzeImage(bitmap: Bitmap, prompt: String): Result<String> {
        return GeminiAIProvider().analyzeImage(bitmap, prompt)
    }

    override suspend fun generateImage(
        prompt: String,
        aspectRatio: String,
        stylePreset: String,
        quality: String
    ): Result<GeneratedImageResult> {
        return GeminiAIProvider().generateImage(prompt, aspectRatio, stylePreset, quality)
    }

    override suspend fun editImage(
        originalBitmap: Bitmap,
        instruction: String,
        operationType: String
    ): Result<GeneratedImageResult> {
        return GeminiAIProvider().editImage(originalBitmap, instruction, operationType)
    }

    override suspend fun enhancePrompt(rawPrompt: String, targetType: String): Result<String> {
        return Result.success("Optimized prompt: $rawPrompt, high quality, sharp focus, 8k resolution")
    }
}
