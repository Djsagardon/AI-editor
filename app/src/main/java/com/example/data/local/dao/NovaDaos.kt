package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.CreativeProjectEntity
import com.example.data.local.entity.ImageVersionEntity
import com.example.data.local.entity.KnowledgeChunkEntity
import com.example.data.local.entity.KnowledgeDocEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.ToolExecutionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("UPDATE conversations SET title = :newTitle, updatedAt = :timestamp WHERE id = :id")
    suspend fun renameConversation(id: String, newTitle: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun togglePin(id: String, isPinned: Boolean)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesList(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("UPDATE messages SET content = :content WHERE id = :messageId")
    suspend fun updateMessageContent(messageId: String, content: String)
}

@Dao
interface CreativeProjectDao {
    @Query("SELECT * FROM creative_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<CreativeProjectEntity>>

    @Query("SELECT * FROM creative_projects WHERE id = :id")
    suspend fun getProjectById(id: String): CreativeProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: CreativeProjectEntity)

    @Query("DELETE FROM creative_projects WHERE id = :id")
    suspend fun deleteProject(id: String)

    @Query("UPDATE creative_projects SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)
}

@Dao
interface ImageVersionDao {
    @Query("SELECT * FROM image_versions WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getVersionsForProject(projectId: String): Flow<List<ImageVersionEntity>>

    @Query("SELECT * FROM image_versions WHERE projectId = :projectId ORDER BY createdAt DESC")
    suspend fun getVersionsList(projectId: String): List<ImageVersionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ImageVersionEntity)

    @Query("DELETE FROM image_versions WHERE id = :versionId")
    suspend fun deleteVersion(versionId: String)
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates WHERE isActive = 1 ORDER BY usageCount DESC, createdAt DESC")
    fun getAllActiveTemplates(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    fun getAllTemplatesAdmin(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE category = :category AND isActive = 1")
    fun getTemplatesByCategory(category: String): Flow<List<TemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)

    @Update
    suspend fun updateTemplate(template: TemplateEntity)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun deleteTemplate(id: String)

    @Query("UPDATE templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: String)

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun getCount(): Int
}

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY updatedAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isEnabled = 1")
    suspend fun getActiveMemories(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: String)

    @Query("DELETE FROM memories")
    suspend fun clearAllMemories()

    @Query("UPDATE memories SET isEnabled = :enabled WHERE id = :id")
    suspend fun toggleMemory(id: String, enabled: Boolean)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<KnowledgeDocEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: KnowledgeDocEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<KnowledgeChunkEntity>)

    @Query("SELECT * FROM knowledge_chunks WHERE text LIKE '%' || :query || '%' OR keywords LIKE '%' || :query || '%' LIMIT 10")
    suspend fun searchChunks(query: String): List<KnowledgeChunkEntity>

    @Query("DELETE FROM knowledge_documents WHERE id = :id")
    suspend fun deleteDocument(id: String)

    @Query("DELETE FROM knowledge_chunks WHERE docId = :docId")
    suspend fun deleteChunksForDoc(docId: String)
}

@Dao
interface ToolExecutionDao {
    @Query("SELECT * FROM tool_executions ORDER BY timestamp DESC LIMIT 50")
    fun getRecentExecutions(): Flow<List<ToolExecutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExecution(execution: ToolExecutionEntity)

    @Query("SELECT COUNT(*) FROM tool_executions")
    suspend fun getTotalExecutionsCount(): Int
}
