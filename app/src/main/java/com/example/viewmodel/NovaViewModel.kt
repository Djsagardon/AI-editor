package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.agent.AgentOrchestrator
import com.example.ai.agent.AgentResponse
import com.example.ai.agent.IntentDetector
import com.example.ai.agent.ToolRegistry
import com.example.ai.model.AIProviderType
import com.example.ai.model.GeneratedImageResult
import com.example.ai.model.LanguageMode
import com.example.ai.provider.AIProvider
import com.example.ai.provider.AIProviderFactory
import com.example.data.local.NovaDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.CreativeProjectEntity
import com.example.data.local.entity.ImageVersionEntity
import com.example.data.local.entity.KnowledgeDocEntity
import com.example.data.local.entity.MemoryEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.TemplateEntity
import com.example.data.local.entity.ToolExecutionEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import com.example.data.repository.AuthUser
import com.example.data.repository.NovaRepository
import com.example.voice.VoiceManager
import com.example.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class CreativeStudioUiState(
    val prompt: String = "",
    val negativePrompt: String = "",
    val selectedAspectRatio: String = "1:1",
    val selectedStylePreset: String = "Cinematic",
    val selectedQuality: String = "HD",
    val isGenerating: Boolean = false,
    val generationProgressMessage: String = "",
    val latestGeneratedImage: GeneratedImageResult? = null,
    val generationError: String? = null,
    val selectedProjectId: String? = null,
    val uploadedEditBitmap: Bitmap? = null,
    val editInstruction: String = "",
    val selectedOperationType: String = "remove_bg",
    val isEditing: Boolean = false,
    val editError: String? = null,
    val selectedVersionIndex: Int = 0
)

class NovaViewModel(application: Application) : AndroidViewModel(application) {

    private val database = NovaDatabase.getDatabase(application)
    val repository = NovaRepository(database)
    val authRepository = AuthRepository(application)
    val providerFactory = AIProviderFactory(application)
    val orchestrator = AgentOrchestrator(repository, IntentDetector(), ToolRegistry())
    val voiceManager = VoiceManager(application)

    // Auth State
    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authDialogContext = MutableStateFlow<String?>(null)
    val authDialogContext: StateFlow<String?> = _authDialogContext.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    // Data streams
    val conversations = repository.allConversations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageEntity>>(emptyList())
    val messages: StateFlow<List<MessageEntity>> = _messages.asStateFlow()

    private val _isAgentThinking = MutableStateFlow(false)
    val isAgentThinking: StateFlow<Boolean> = _isAgentThinking.asStateFlow()

    private val _streamingMessageText = MutableStateFlow<String?>(null)
    val streamingMessageText: StateFlow<String?> = _streamingMessageText.asStateFlow()

    // Language & AI Settings
    val selectedLanguageMode = providerFactory.languageMode.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "AUTO"
    )

    val selectedProviderType = providerFactory.selectedProviderType.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), AIProviderType.GEMINI_DIRECT
    )

    val customApiKey = providerFactory.customApiKey.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), ""
    )

    val localServerUrl = providerFactory.localServerUrl.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), "http://localhost:8080/v1"
    )

    // Creative Studio State
    private val _studioState = MutableStateFlow(CreativeStudioUiState())
    val studioState: StateFlow<CreativeStudioUiState> = _studioState.asStateFlow()

    val projects = repository.allProjects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _currentProjectVersions = MutableStateFlow<List<ImageVersionEntity>>(emptyList())
    val currentProjectVersions: StateFlow<List<ImageVersionEntity>> = _currentProjectVersions.asStateFlow()

    // Templates State
    val templates = repository.activeTemplates.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTemplatesAdmin = repository.allTemplatesAdmin.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _selectedTemplateCategory = MutableStateFlow("All")
    val selectedTemplateCategory: StateFlow<String> = _selectedTemplateCategory.asStateFlow()

    // Memories & Knowledge
    val memories = repository.allMemories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val knowledgeDocs = repository.allKnowledgeDocs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val toolExecutions = repository.recentToolExecutions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // User Profile / Status
    private val _userProfileName = MutableStateFlow("Cosmic Explorer")
    val userProfileName: StateFlow<String> = _userProfileName.asStateFlow()

    private val _isUserSignedIn = MutableStateFlow(true)
    val isUserSignedIn: StateFlow<Boolean> = _isUserSignedIn.asStateFlow()

    init {
        // Observe conversation updates to maintain messages
        viewModelScope.launch {
            conversations.collect { list ->
                if (_activeConversationId.value == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                }
            }
        }

        voiceManager.onSpeechRecognized = { spoken ->
            sendMessage(spoken)
        }
    }

    private fun getCurrentProvider(): AIProvider {
        return providerFactory.getProvider(
            type = selectedProviderType.value,
            customKey = customApiKey.value,
            localUrl = localServerUrl.value
        )
    }

    fun selectConversation(conversationId: String) {
        _activeConversationId.value = conversationId
        viewModelScope.launch {
            repository.getMessagesForConversation(conversationId).collect { msgList ->
                _messages.value = msgList
            }
        }
    }

    fun createNewConversation(title: String = "New AI Conversation") {
        viewModelScope.launch {
            val conv = repository.createConversation(
                title = title,
                language = selectedLanguageMode.value
            )
            selectConversation(conv.id)
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            repository.deleteConversation(id)
            if (_activeConversationId.value == id) {
                _activeConversationId.value = null
                _messages.value = emptyList()
            }
        }
    }

    fun renameConversation(id: String, newTitle: String) {
        viewModelScope.launch {
            repository.renameConversation(id, newTitle)
        }
    }

    fun togglePinConversation(id: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinConversation(id, isPinned)
        }
    }

    fun sendMessage(
        text: String,
        attachedBitmap: Bitmap? = null
    ) {
        val convId = _activeConversationId.value ?: return
        if (text.isBlank() && attachedBitmap == null) return

        viewModelScope.launch {
            val userMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                role = "user",
                content = text,
                imageUrl = if (attachedBitmap != null) "attached_image_in_memory" else null
            )
            repository.insertMessage(userMsg)

            _isAgentThinking.value = true
            _streamingMessageText.value = ""

            val langMode = LanguageMode.fromCode(selectedLanguageMode.value)
            val provider = getCurrentProvider()

            val response = orchestrator.processUserRequest(
                conversationId = convId,
                userText = text,
                attachedBitmap = attachedBitmap,
                provider = provider,
                preferredLanguage = langMode,
                onChunkStream = { chunk ->
                    _streamingMessageText.value = chunk
                }
            )

            _isAgentThinking.value = false
            _streamingMessageText.value = null

            when (response) {
                is AgentResponse.Text -> {
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = response.content,
                        language = response.language.code
                    )
                    repository.insertMessage(aiMsg)

                    // Speak response if in voice conversation mode
                    if (voiceManager.voiceState.value == VoiceState.PROCESSING) {
                        voiceManager.speakText(response.content, response.language)
                    }
                }

                is AgentResponse.ImageCreated -> {
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = "${response.description}\n\n**Generated Image:**",
                        imageUrl = response.imageResult.imageUriOrBase64
                    )
                    repository.insertMessage(aiMsg)
                }

                is AgentResponse.ToolExecuted -> {
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = "${response.toolOutput}\n\n${response.followUpText}".trim(),
                        toolName = response.toolName,
                        toolStatus = "success"
                    )
                    repository.insertMessage(aiMsg)
                }

                is AgentResponse.ConfirmationRequired -> {
                    val aiMsg = MessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = "⚠️ **Confirmation Required:** ${response.prompt}\n\nDo you want me to continue?"
                    )
                    repository.insertMessage(aiMsg)
                }
            }
        }
    }

    // Creative Studio Actions
    fun updatePrompt(prompt: String) {
        _studioState.value = _studioState.value.copy(prompt = prompt)
    }

    fun updateNegativePrompt(neg: String) {
        _studioState.value = _studioState.value.copy(negativePrompt = neg)
    }

    fun setAspectRatio(ratio: String) {
        _studioState.value = _studioState.value.copy(selectedAspectRatio = ratio)
    }

    fun setStylePreset(style: String) {
        _studioState.value = _studioState.value.copy(selectedStylePreset = style)
    }

    fun setQuality(quality: String) {
        _studioState.value = _studioState.value.copy(selectedQuality = quality)
    }

    fun setUploadedEditBitmap(bitmap: Bitmap?) {
        _studioState.value = _studioState.value.copy(uploadedEditBitmap = bitmap)
    }

    fun setEditInstruction(instruction: String) {
        _studioState.value = _studioState.value.copy(editInstruction = instruction)
    }

    fun setOperationType(op: String) {
        _studioState.value = _studioState.value.copy(selectedOperationType = op)
    }

    fun enhancePrompt() {
        val raw = _studioState.value.prompt
        if (raw.isBlank()) return
        viewModelScope.launch {
            val enhanced = getCurrentProvider().enhancePrompt(raw).getOrDefault(raw)
            _studioState.value = _studioState.value.copy(prompt = enhanced)
        }
    }

    fun generateStudioImage() {
        val state = _studioState.value
        if (state.prompt.isBlank()) return

        viewModelScope.launch {
            _studioState.value = _studioState.value.copy(
                isGenerating = true,
                generationProgressMessage = "Synthesizing visual concepts...",
                generationError = null
            )

            val provider = getCurrentProvider()
            val result = provider.generateImage(
                prompt = state.prompt,
                aspectRatio = state.selectedAspectRatio,
                stylePreset = state.selectedStylePreset,
                quality = state.selectedQuality
            )

            _studioState.value = _studioState.value.copy(
                isGenerating = false,
                generationProgressMessage = ""
            )

            if (result.isSuccess) {
                val generated = result.getOrNull()
                if (generated != null && generated.imageUriOrBase64.isNotBlank()) {
                    _studioState.value = _studioState.value.copy(
                        latestGeneratedImage = generated,
                        generationError = null
                    )

                    // Save to repository projects
                    val project = repository.createProject(
                        title = state.prompt.take(30),
                        prompt = state.prompt,
                        negativePrompt = state.negativePrompt,
                        aspectRatio = state.selectedAspectRatio,
                        quality = state.selectedQuality,
                        stylePreset = state.selectedStylePreset
                    )
                    repository.addImageVersion(
                        projectId = project.id,
                        versionName = "Original V1",
                        promptOrInstruction = state.prompt,
                        operationType = "generate",
                        imageUri = generated.imageUriOrBase64
                    )
                    loadProjectVersions(project.id)
                } else {
                    _studioState.value = _studioState.value.copy(
                        latestGeneratedImage = null,
                        generationError = "No valid image data was returned by the provider."
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to generate image"
                _studioState.value = _studioState.value.copy(
                    latestGeneratedImage = null,
                    generationError = err
                )
            }
        }
    }

    fun executeImageEdit() {
        val state = _studioState.value
        val bitmap = state.uploadedEditBitmap ?: return
        val instruction = state.editInstruction.ifBlank { "Apply ${state.selectedOperationType}" }

        viewModelScope.launch {
            _studioState.value = _studioState.value.copy(
                isEditing = true,
                generationProgressMessage = "Executing non-destructive layer modifications...",
                editError = null
            )

            val provider = getCurrentProvider()
            val result = provider.editImage(
                originalBitmap = bitmap,
                instruction = instruction,
                operationType = state.selectedOperationType
            )

            _studioState.value = _studioState.value.copy(
                isEditing = false,
                generationProgressMessage = ""
            )

            if (result.isSuccess) {
                val edited = result.getOrNull()
                if (edited != null && edited.imageUriOrBase64.isNotBlank()) {
                    _studioState.value = _studioState.value.copy(
                        latestGeneratedImage = edited,
                        editError = null
                    )

                    // Add to project version branch
                    state.selectedProjectId?.let { pId ->
                        repository.addImageVersion(
                            projectId = pId,
                            versionName = "Version ${currentProjectVersions.value.size + 1} (${state.selectedOperationType})",
                            promptOrInstruction = instruction,
                            operationType = state.selectedOperationType,
                            imageUri = edited.imageUriOrBase64
                        )
                        loadProjectVersions(pId)
                    }
                } else {
                    _studioState.value = _studioState.value.copy(
                        editError = "No edited image returned."
                    )
                }
            } else {
                val err = result.exceptionOrNull()?.message ?: "Failed to edit image"
                _studioState.value = _studioState.value.copy(
                    editError = err
                )
            }
        }
    }

    fun loadProjectVersions(projectId: String) {
        _studioState.value = _studioState.value.copy(selectedProjectId = projectId)
        viewModelScope.launch {
            repository.getVersionsForProject(projectId).collect { versions ->
                _currentProjectVersions.value = versions
            }
        }
    }

    // Templates Actions
    fun setTemplateCategory(category: String) {
        _selectedTemplateCategory.value = category
    }

    fun applyTemplate(template: TemplateEntity, variableValues: Map<String, String>) {
        var finalPrompt = template.promptTemplate
        for ((key, value) in variableValues) {
            finalPrompt = finalPrompt.replace("{$key}", value)
        }

        // Set prompt and aspect ratio in studio
        _studioState.value = _studioState.value.copy(
            prompt = finalPrompt,
            selectedAspectRatio = template.aspectRatio
        )

        viewModelScope.launch {
            repository.incrementTemplateUsage(template.id)
        }
    }

    fun createTemplateAdmin(
        name: String,
        description: String,
        category: String,
        promptTemplate: String,
        aspectRatio: String,
        isPremium: Boolean
    ) {
        viewModelScope.launch {
            repository.insertTemplate(
                TemplateEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    category = category,
                    promptTemplate = promptTemplate,
                    aspectRatio = aspectRatio,
                    isPremium = isPremium
                )
            )
        }
    }

    fun deleteTemplateAdmin(id: String) {
        viewModelScope.launch {
            repository.deleteTemplate(id)
        }
    }

    // Memory Actions
    fun addMemory(key: String, value: String, category: String = "preference") {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryEntity(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    key = key,
                    value = value,
                    source = "user"
                )
            )
        }
    }

    fun toggleMemory(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleMemory(id, enabled)
        }
    }

    fun deleteMemory(id: String) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAllMemories()
        }
    }

    // Knowledge Base Actions
    fun addKnowledgeDocument(title: String, fileType: String, content: String) {
        viewModelScope.launch {
            repository.addKnowledgeDocument(title, fileType, content)
        }
    }

    fun deleteKnowledgeDoc(docId: String) {
        viewModelScope.launch {
            repository.deleteKnowledgeDocument(docId)
        }
    }

    // Settings Actions
    fun setLanguagePreference(languageMode: LanguageMode) {
        viewModelScope.launch {
            providerFactory.setLanguageMode(languageMode.code)
        }
    }

    fun setAIProvider(providerType: AIProviderType) {
        viewModelScope.launch {
            providerFactory.setProviderType(providerType)
        }
    }

    fun setCustomApiKey(key: String) {
        viewModelScope.launch {
            providerFactory.setCustomApiKey(key)
        }
    }

    fun setLocalServerUrl(url: String) {
        viewModelScope.launch {
            providerFactory.setLocalServerUrl(url)
        }
    }

    // Authentication Actions
    fun openAuthDialog(contextReason: String? = null) {
        _authDialogContext.value = contextReason
        _authErrorMessage.value = null
        _showAuthDialog.value = true
    }

    fun dismissAuthDialog() {
        _showAuthDialog.value = false
        _authDialogContext.value = null
        _authErrorMessage.value = null
    }

    fun login(email: String, pass: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authRepository.login(email, pass)
            _isAuthLoading.value = false
            result.onSuccess {
                dismissAuthDialog()
                onSuccess?.invoke()
            }.onFailure { e ->
                _authErrorMessage.value = e.localizedMessage ?: "Login failed"
            }
        }
    }

    fun register(name: String, email: String, pass: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authErrorMessage.value = null
            val result = authRepository.register(name, email, pass)
            _isAuthLoading.value = false
            result.onSuccess {
                dismissAuthDialog()
                onSuccess?.invoke()
            }.onFailure { e ->
                _authErrorMessage.value = e.localizedMessage ?: "Registration failed"
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            authRepository.resetPassword(email)
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            authRepository.continueAsGuest()
            dismissAuthDialog()
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun executeWithAuthGuard(actionTitle: String, action: () -> Unit) {
        val user = currentUser.value
        if (user == null || user.isGuest) {
            openAuthDialog(actionTitle)
        } else {
            action()
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
