package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "creative_projects")
data class CreativeProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val prompt: String,
    val negativePrompt: String = "",
    val aspectRatio: String = "1:1",
    val quality: String = "HD",
    val numOutputs: Int = 1,
    val stylePreset: String = "Cinematic",
    val thumbnailUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: String = ""
)

@Entity(tableName = "image_versions")
data class ImageVersionEntity(
    @PrimaryKey val id: String,
    val projectId: String,
    val parentVersionId: String? = null,
    val versionName: String,
    val promptOrInstruction: String,
    val operationType: String, // "generate", "remove_bg", "replace_bg", "change_color", "enhance", "upscale", "custom_edit"
    val imageUri: String,
    val width: Int = 1024,
    val height: Int = 1024,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "completed"
)
