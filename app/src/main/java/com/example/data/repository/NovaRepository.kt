package com.example.data.repository

import com.example.data.local.NovaDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.CreativeProjectEntity
import com.example.data.local.entity.ImageVersionEntity
import com.example.data.local.entity.KnowledgeChunkEntity
import com.example.data.local.entity.KnowledgeDocEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.ToolExecutionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class NovaRepository(private val database: NovaDatabase) {

    // Conversations
    val allConversations: Flow<List<ConversationEntity>> = database.conversationDao().getAllConversations()

    suspend fun getConversation(id: String): ConversationEntity? = withContext(Dispatchers.IO) {
        database.conversationDao().getConversationById(id)
    }

    suspend fun createConversation(title: String, language: String = "AUTO"): ConversationEntity = withContext(Dispatchers.IO) {
        val conv = ConversationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            language = language
        )
        database.conversationDao().insertOrUpdate(conv)
        conv
    }

    suspend fun updateConversation(conversation: ConversationEntity) = withContext(Dispatchers.IO) {
        database.conversationDao().insertOrUpdate(conversation)
    }

    suspend fun renameConversation(id: String, newTitle: String) = withContext(Dispatchers.IO) {
        database.conversationDao().renameConversation(id, newTitle)
    }

    suspend fun togglePinConversation(id: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        database.conversationDao().togglePin(id, isPinned)
    }

    suspend fun deleteConversation(id: String) = withContext(Dispatchers.IO) {
        database.messageDao().deleteMessagesForConversation(id)
        database.conversationDao().deleteConversation(id)
    }

    suspend fun clearAllConversations() = withContext(Dispatchers.IO) {
        database.conversationDao().clearAll()
    }

    // Messages
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForConversation(conversationId)

    suspend fun getMessagesList(conversationId: String): List<MessageEntity> = withContext(Dispatchers.IO) {
        database.messageDao().getMessagesList(conversationId)
    }

    suspend fun insertMessage(message: MessageEntity) = withContext(Dispatchers.IO) {
        database.messageDao().insertMessage(message)
        // Update conversation timestamp
        val conv = database.conversationDao().getConversationById(message.conversationId)
        if (conv != null) {
            database.conversationDao().insertOrUpdate(conv.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateMessageContent(messageId: String, content: String) = withContext(Dispatchers.IO) {
        database.messageDao().updateMessageContent(messageId, content)
    }

    suspend fun deleteMessage(messageId: String) = withContext(Dispatchers.IO) {
        database.messageDao().deleteMessage(messageId)
    }

    // Creative Projects & Versions
    val allProjects: Flow<List<CreativeProjectEntity>> = database.creativeProjectDao().getAllProjects()

    fun getVersionsForProject(projectId: String): Flow<List<ImageVersionEntity>> =
        database.imageVersionDao().getVersionsForProject(projectId)

    suspend fun createProject(
        title: String,
        prompt: String,
        negativePrompt: String = "",
        aspectRatio: String = "1:1",
        quality: String = "HD",
        stylePreset: String = "Cinematic"
    ): CreativeProjectEntity = withContext(Dispatchers.IO) {
        val project = CreativeProjectEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            prompt = prompt,
            negativePrompt = negativePrompt,
            aspectRatio = aspectRatio,
            quality = quality,
            stylePreset = stylePreset
        )
        database.creativeProjectDao().insertProject(project)
        project
    }

    suspend fun addImageVersion(
        projectId: String,
        versionName: String,
        promptOrInstruction: String,
        operationType: String,
        imageUri: String,
        parentVersionId: String? = null,
        width: Int = 1024,
        height: Int = 1024
    ): ImageVersionEntity = withContext(Dispatchers.IO) {
        val version = ImageVersionEntity(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            parentVersionId = parentVersionId,
            versionName = versionName,
            promptOrInstruction = promptOrInstruction,
            operationType = operationType,
            imageUri = imageUri,
            width = width,
            height = height
        )
        database.imageVersionDao().insertVersion(version)
        // Update project thumbnail
        val project = database.creativeProjectDao().getProjectById(projectId)
        if (project != null) {
            database.creativeProjectDao().insertProject(project.copy(thumbnailUri = imageUri))
        }
        version
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        database.creativeProjectDao().deleteProject(id)
    }

    suspend fun toggleFavoriteProject(id: String, isFav: Boolean) = withContext(Dispatchers.IO) {
        database.creativeProjectDao().toggleFavorite(id, isFav)
    }

    // Templates
    val activeTemplates: Flow<List<TemplateEntity>> = database.templateDao().getAllActiveTemplates()
    val allTemplatesAdmin: Flow<List<TemplateEntity>> = database.templateDao().getAllTemplatesAdmin()

    fun getTemplatesByCategory(category: String): Flow<List<TemplateEntity>> =
        database.templateDao().getTemplatesByCategory(category)

    suspend fun insertTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        database.templateDao().insertTemplate(template)
    }

    suspend fun updateTemplate(template: TemplateEntity) = withContext(Dispatchers.IO) {
        database.templateDao().updateTemplate(template)
    }

    suspend fun deleteTemplate(id: String) = withContext(Dispatchers.IO) {
        database.templateDao().deleteTemplate(id)
    }

    suspend fun incrementTemplateUsage(id: String) = withContext(Dispatchers.IO) {
        database.templateDao().incrementUsage(id)
    }

    // Memories
    val allMemories: Flow<List<MemoryEntity>> = database.memoryDao().getAllMemories()

    suspend fun getActiveMemories(): List<MemoryEntity> = withContext(Dispatchers.IO) {
        database.memoryDao().getActiveMemories()
    }

    suspend fun insertMemory(memory: MemoryEntity) = withContext(Dispatchers.IO) {
        database.memoryDao().insertMemory(memory)
    }

    suspend fun toggleMemory(id: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        database.memoryDao().toggleMemory(id, enabled)
    }

    suspend fun deleteMemory(id: String) = withContext(Dispatchers.IO) {
        database.memoryDao().deleteMemory(id)
    }

    suspend fun clearAllMemories() = withContext(Dispatchers.IO) {
        database.memoryDao().clearAllMemories()
    }

    // Knowledge
    val allKnowledgeDocs: Flow<List<KnowledgeDocEntity>> = database.knowledgeDao().getAllDocuments()

    suspend fun addKnowledgeDocument(title: String, fileType: String, content: String, summary: String = ""): KnowledgeDocEntity = withContext(Dispatchers.IO) {
        val docId = UUID.randomUUID().toString()
        // Chunk content into ~300 character chunks
        val chunks = content.chunked(300).mapIndexed { index, chunkText ->
            KnowledgeChunkEntity(
                id = UUID.randomUUID().toString(),
                docId = docId,
                chunkIndex = index,
                text = chunkText,
                keywords = chunkText.split(" ").filter { it.length > 4 }.take(8).joinToString(",")
            )
        }
        val doc = KnowledgeDocEntity(
            id = docId,
            title = title,
            fileType = fileType,
            rawContent = content,
            summary = summary.ifBlank { content.take(120) + "..." },
            chunkCount = chunks.size
        )
        database.knowledgeDao().insertDocument(doc)
        database.knowledgeDao().insertChunks(chunks)
        doc
    }

    suspend fun searchKnowledge(query: String): List<KnowledgeChunkEntity> = withContext(Dispatchers.IO) {
        database.knowledgeDao().searchChunks(query)
    }

    suspend fun deleteKnowledgeDocument(docId: String) = withContext(Dispatchers.IO) {
        database.knowledgeDao().deleteChunksForDoc(docId)
        database.knowledgeDao().deleteDocument(docId)
    }

    // Tool Executions
    val recentToolExecutions: Flow<List<ToolExecutionEntity>> = database.toolExecutionDao().getRecentExecutions()

    suspend fun recordToolExecution(execution: ToolExecutionEntity) = withContext(Dispatchers.IO) {
        database.toolExecutionDao().insertExecution(execution)
    }

    suspend fun getTotalToolExecutionsCount(): Int = withContext(Dispatchers.IO) {
        database.toolExecutionDao().getTotalExecutionsCount()
    }
}
