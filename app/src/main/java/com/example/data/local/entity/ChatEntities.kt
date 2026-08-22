package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val language: String = "AUTO", // AUTO, BN, HI, EN
    val systemPrompt: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "system", "tool"
    val content: String,
    val imageUrl: String? = null,
    val fileUrl: String? = null,
    val audioUrl: String? = null,
    val toolName: String? = null,
    val toolStatus: String? = null, // "running", "success", "error"
    val toolResult: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "EN",
    val tokensUsed: Int = 0
)
