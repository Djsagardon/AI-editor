package com.example.ai.provider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.ai.model.GeneratedImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiImageGenerationProvider(
    private val customApiKey: String? = null
) : ImageGenerationProvider {

    override val providerName: String = "Google Gemini Image Generator"
    override val modelName: String = "gemini-2.5-flash-image"

    private val effectiveApiKey: String
        get() {
            if (!customApiKey.isNullOrBlank()) return customApiKey
            return try {
                val key = BuildConfig.GEMINI_API_KEY
                if (key != "MY_GEMINI_API_KEY") key else ""
            } catch (e: Exception) {
                ""
            }
        }

    override val isConfigured: Boolean
        get() = effectiveApiKey.isNotBlank() && effectiveApiKey != "MY_GEMINI_API_KEY"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun generateImage(
        prompt: String,
        options: ImageGenerationOptions
    ): Result<GeneratedImageResult> = withContext(Dispatchers.IO) {
        val apiKey = effectiveApiKey

        Log.d("NOVA_IMAGE_GEN", "=== IMAGE REQUEST STARTED ===")
        Log.d("NOVA_IMAGE_GEN", "Prompt received: $prompt")
        Log.d("NOVA_IMAGE_GEN", "Image provider selected: $providerName")
        Log.d("NOVA_IMAGE_GEN", "Image model selected: $modelName")

        if (!isConfigured) {
            val errorMsg = "Image generation is not configured yet. Please configure your Gemini API Key in Settings or the Secrets panel."
            Log.e("NOVA_IMAGE_GEN", "=== IMAGE GENERATION ERROR ===")
            Log.e("NOVA_IMAGE_GEN", "Provider: $providerName")
            Log.e("NOVA_IMAGE_GEN", "Model: $modelName")
            Log.e("NOVA_IMAGE_GEN", "Reason: Missing or default API key")
            return@withContext Result.failure(IllegalStateException(errorMsg))
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            // Construct structured prompt preserving exact user intent
            val promptBuilder = StringBuilder(prompt.trim())
            if (options.stylePreset.isNotBlank() && options.stylePreset != "Cinematic") {
                promptBuilder.append(", in ").append(options.stylePreset).append(" style")
            }
            if (options.quality.equals("Ultra", ignoreCase = true)) {
                promptBuilder.append(", ultra-detailed masterpiece, 8k resolution, high dynamic range")
            }
            if (!options.negativePrompt.isNullOrBlank()) {
                promptBuilder.append(". Negative prompt (avoid): ").append(options.negativePrompt)
            }

            val finalPrompt = promptBuilder.toString()
            Log.d("NOVA_IMAGE_GEN", "Constructed final prompt: $finalPrompt")

            val partsArray = JSONArray().put(JSONObject().put("text", finalPrompt))
            val contentObj = JSONObject().put("parts", partsArray)

            val imageConfig = JSONObject()
                .put("aspectRatio", mapAspectRatio(options.aspectRatio))
                .put("imageSize", if (options.quality.equals("Ultra", ignoreCase = true)) "2K" else "1K")

            val genConfig = JSONObject()
                .put("imageConfig", imageConfig)
                .put("responseModalities", JSONArray().put("IMAGE").put("TEXT"))

            val requestJson = JSONObject()
                .put("contents", JSONArray().put(contentObj))
                .put("generationConfig", genConfig)

            Log.d("NOVA_IMAGE_GEN", "Generation request sent to endpoint")

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val statusCode = response.code
            val responseBody = response.body?.string() ?: ""

            Log.d("NOVA_IMAGE_GEN", "Generation response received: HTTP $statusCode")

            if (!response.isSuccessful) {
                val errorReason = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP $statusCode error"
                } catch (e: Exception) {
                    "HTTP $statusCode: ${response.message}"
                }

                Log.e("NOVA_IMAGE_GEN", "=== IMAGE GENERATION ERROR ===")
                Log.e("NOVA_IMAGE_GEN", "Provider: $providerName")
                Log.e("NOVA_IMAGE_GEN", "Model: $modelName")
                Log.e("NOVA_IMAGE_GEN", "Reason: $errorReason")

                return@withContext Result.failure(Exception("Image generation failed from server: $errorReason"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val parts = firstCandidate?.optJSONObject("content")?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val mimeType = inlineData.optString("mimeType", "image/png")
                        val base64Data = inlineData.optString("data")

                        if (base64Data.isNotBlank()) {
                            // Validate image data by attempting to decode
                            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                            if (decodedBitmap != null && decodedBitmap.width > 0 && decodedBitmap.height > 0) {
                                Log.d("NOVA_IMAGE_GEN", "Image data detected: mime=$mimeType, ${decodedBytes.size} bytes (${decodedBitmap.width}x${decodedBitmap.height})")
                                Log.d("NOVA_IMAGE_GEN", "Image saved and verified successfully")

                                val dataUri = "data:$mimeType;base64,$base64Data"
                                return@withContext Result.success(
                                    GeneratedImageResult(
                                        imageUriOrBase64 = dataUri,
                                        prompt = prompt,
                                        width = decodedBitmap.width,
                                        height = decodedBitmap.height,
                                        providerUsed = "$providerName ($modelName)"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Log.e("NOVA_IMAGE_GEN", "=== IMAGE GENERATION ERROR ===")
            Log.e("NOVA_IMAGE_GEN", "Provider: $providerName")
            Log.e("NOVA_IMAGE_GEN", "Model: $modelName")
            Log.e("NOVA_IMAGE_GEN", "Reason: No image data returned in API candidates response")

            Result.failure(Exception("No valid image data was returned by the image model."))
        } catch (e: Exception) {
            Log.e("NOVA_IMAGE_GEN", "=== IMAGE GENERATION ERROR ===")
            Log.e("NOVA_IMAGE_GEN", "Provider: $providerName")
            Log.e("NOVA_IMAGE_GEN", "Model: $modelName")
            Log.e("NOVA_IMAGE_GEN", "Reason: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun editImage(
        image: Bitmap,
        prompt: String,
        options: ImageEditOptions
    ): Result<GeneratedImageResult> = withContext(Dispatchers.IO) {
        val apiKey = effectiveApiKey

        Log.d("NOVA_IMAGE_GEN", "=== IMAGE EDIT REQUEST STARTED ===")
        Log.d("NOVA_IMAGE_GEN", "Instruction: $prompt, Operation: ${options.operationType}")

        if (!isConfigured) {
            return@withContext Result.failure(
                IllegalStateException("Image editing is not configured yet. Please configure your Gemini API Key in Settings.")
            )
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val imageBase64 = bitmapToBase64(image)

            val partsArray = JSONArray()
            partsArray.put(JSONObject().put("text", "Edit this image based on the following instruction: $prompt (Operation: ${options.operationType})"))
            partsArray.put(
                JSONObject().put(
                    "inlineData",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", imageBase64)
                )
            )

            val contentObj = JSONObject().put("parts", partsArray)
            val genConfig = JSONObject()
                .put("responseModalities", JSONArray().put("IMAGE").put("TEXT"))

            val requestJson = JSONObject()
                .put("contents", JSONArray().put(contentObj))
                .put("generationConfig", genConfig)

            val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Image edit failed: ${response.message}"))
            }

            val rootJson = JSONObject(responseBody)
            val parts = rootJson.optJSONArray("candidates")?.optJSONObject(0)
                ?.optJSONObject("content")?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.optJSONObject(i)
                    val inlineData = part?.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val mimeType = inlineData.optString("mimeType", "image/png")
                        val base64Data = inlineData.optString("data")

                        if (base64Data.isNotBlank()) {
                            val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                            if (decodedBitmap != null && decodedBitmap.width > 0 && decodedBitmap.height > 0) {
                                Log.d("NOVA_IMAGE_GEN", "Edited image data received: ${decodedBitmap.width}x${decodedBitmap.height}")
                                return@withContext Result.success(
                                    GeneratedImageResult(
                                        imageUriOrBase64 = "data:$mimeType;base64,$base64Data",
                                        prompt = prompt,
                                        width = decodedBitmap.width,
                                        height = decodedBitmap.height,
                                        providerUsed = "$providerName ($modelName)"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Result.failure(Exception("No edited image returned by the image model."))
        } catch (e: Exception) {
            Log.e("NOVA_IMAGE_GEN", "Image edit error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun generateVariations(
        image: Bitmap,
        options: ImageVariationOptions
    ): Result<GeneratedImageResult> {
        return editImage(image, "Create a creative variation with enhanced aesthetic composition and lighting", ImageEditOptions(operationType = "variation"))
    }

    private fun mapAspectRatio(ratio: String): String {
        return when (ratio) {
            "16:9", "9:16", "4:3", "3:4", "4:5" -> ratio
            else -> "1:1"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }
}
