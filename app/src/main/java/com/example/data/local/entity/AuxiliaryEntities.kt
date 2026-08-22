package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // "Portrait", "Cinematic", "Wedding", "Festival", "Business", etc.
    val previewGradientColors: String = "#00E5FF,#9D4EDD", // fallback gradient hex colors
    val promptTemplate: String,
    val aspectRatio: String = "1:1",
    val requiredInputsJson: String = "[]", // list of parameter names e.g. ["person", "location", "lighting"]
    val isPremium: Boolean = false,
    val isActive: Boolean = true,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val category: String, // "preference", "language", "style", "fact", "custom"
    val key: String,
    val value: String,
    val source: String = "conversation",
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_documents")
data class KnowledgeDocEntity(
    @PrimaryKey val id: String,
    val title: String,
    val fileType: String = "TEXT", // "PDF", "TXT", "DOCX", "NOTE"
    val rawContent: String,
    val summary: String = "",
    val chunkCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_chunks")
data class KnowledgeChunkEntity(
    @PrimaryKey val id: String,
    val docId: String,
    val chunkIndex: Int,
    val text: String,
    val keywords: String = ""
)

@Entity(tableName = "tool_executions")
data class ToolExecutionEntity(
    @PrimaryKey val id: String,
    val toolName: String,
    val inputJson: String,
    val outputJson: String,
    val status: String, // "success", "error", "pending"
    val durationMs: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
