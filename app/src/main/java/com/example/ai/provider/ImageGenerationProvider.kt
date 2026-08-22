package com.example.ai.provider

import android.graphics.Bitmap
import com.example.ai.model.GeneratedImageResult

data class ImageGenerationOptions(
    val aspectRatio: String = "1:1",
    val stylePreset: String = "Cinematic",
    val quality: String = "HD",
    val negativePrompt: String? = null
)

data class ImageEditOptions(
    val operationType: String = "edit",
    val quality: String = "HD"
)

data class ImageVariationOptions(
    val count: Int = 1,
    val quality: String = "HD"
)

/**
 * Clean abstraction for AI Image Generation and Editing.
 * Decouples conversational text models (e.g. gemini-3.5-flash)
 * from dedicated image generation models (e.g. gemini-2.5-flash-image).
 */
interface ImageGenerationProvider {
    val providerName: String
    val modelName: String
    val isConfigured: Boolean

    suspend fun generateImage(
        prompt: String,
        options: ImageGenerationOptions = ImageGenerationOptions()
    ): Result<GeneratedImageResult>

    suspend fun editImage(
        image: Bitmap,
        prompt: String,
        options: ImageEditOptions = ImageEditOptions()
    ): Result<GeneratedImageResult>

    suspend fun generateVariations(
        image: Bitmap,
        options: ImageVariationOptions = ImageVariationOptions()
    ): Result<GeneratedImageResult>
}
