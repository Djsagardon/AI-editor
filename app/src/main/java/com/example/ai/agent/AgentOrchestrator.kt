package com.example.ai.agent

import android.graphics.Bitmap
import com.example.ai.model.ChatMessage
import com.example.ai.model.DetectedIntent
import com.example.ai.model.GeneratedImageResult
import com.example.ai.model.IntentType
import com.example.ai.model.LanguageMode
import com.example.ai.provider.AIProvider
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.ToolExecutionEntity
import com.example.data.repository.NovaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

sealed interface AgentResponse {
    data class Text(val content: String, val language: LanguageMode = LanguageMode.ENGLISH) : AgentResponse
    data class ImageCreated(val imageResult: GeneratedImageResult, val description: String) : AgentResponse
    data class ToolExecuted(val toolName: String, val toolOutput: String, val followUpText: String) : AgentResponse
    data class ConfirmationRequired(val toolName: String, val prompt: String, val pendingAction: () -> Unit) : AgentResponse
}

class AgentOrchestrator(
    private val repository: NovaRepository,
    private val intentDetector: IntentDetector = IntentDetector(),
    private val toolRegistry: ToolRegistry = ToolRegistry()
) {

    suspend fun processUserRequest(
        conversationId: String,
        userText: String,
        attachedBitmap: Bitmap? = null,
        provider: AIProvider,
        preferredLanguage: LanguageMode = LanguageMode.AUTO,
        onChunkStream: ((String) -> Unit)? = null
    ): AgentResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // 1. Detect Intent & Language
        val intent = intentDetector.detectIntent(userText, attachedBitmap != null)
        val targetLang = if (preferredLanguage != LanguageMode.AUTO) preferredLanguage else intent.language

        // 2. Fetch Active Memories to build context
        val activeMemories = repository.getActiveMemories()
        val memoryContext = if (activeMemories.isNotEmpty()) {
            "User Long-Term Preferences:\n" + activeMemories.joinToString("\n") { "- ${it.key}: ${it.value}" }
        } else ""

        // 3. Handle Intent Types
        when (intent.type) {
            IntentType.IMAGE_GENERATION -> {
                val enhancedPromptResult = provider.enhancePrompt(intent.extractedPrompt)
                val finalPrompt = enhancedPromptResult.getOrDefault(intent.extractedPrompt)
                val imgResult = provider.generateImage(finalPrompt)

                val generated = imgResult.getOrNull()
                if (imgResult.isSuccess && generated != null && generated.imageUriOrBase64.isNotBlank()) {
                    // Create creative project in database
                    val project = repository.createProject(
                        title = intent.extractedPrompt.take(30),
                        prompt = finalPrompt
                    )
                    repository.addImageVersion(
                        projectId = project.id,
                        versionName = "Original V1",
                        promptOrInstruction = finalPrompt,
                        operationType = "generate",
                        imageUri = generated.imageUriOrBase64
                    )

                    val desc = when (targetLang) {
                        LanguageMode.BENGALI -> "✨ আমি আপনার নির্দেশ মতো ছবিটি সফলভাবে তৈরি করেছি!"
                        LanguageMode.HINDI -> "✨ मैंने आपके निर्देशानुसार तस्वीर सफलतापूर्वक बना दी है!"
                        else -> "✨ Here is your generated image based on your prompt."
                    }
                    return@withContext AgentResponse.ImageCreated(generated, desc)
                } else {
                    val errorReason = imgResult.exceptionOrNull()?.message ?: "Unknown error"
                    val errorMsg = when (targetLang) {
                        LanguageMode.BENGALI -> "⚠️ ছবিটি তৈরি করা যায়নি কারণ ইমেজ জেনারেশন সার্ভিস বর্তমানে কনফিগার করা নেই বা অনুপলব্ধ।\n\n*কারণ: $errorReason*\n\nঅনুগ্রহ করে **Settings**-এ গিয়ে আপনার Google Gemini API Key প্রবেশ করান।"
                        LanguageMode.HINDI -> "⚠️ तस्वीर नहीं बनाई जा सकी क्योंकि इमेज जेनरेशन सर्विस अभी उपलब्ध या कॉन्फ़िगर नहीं है।\n\n*कारण: $errorReason*\n\nकृपया **Settings** में जाकर अपनी Google Gemini API Key दर्ज करें।"
                        else -> "⚠️ I couldn't generate the image because the image-generation service is unavailable or not configured.\n\n*Reason: $errorReason*\n\nPlease configure your Google Gemini API Key in the **Settings** tab."
                    }
                    return@withContext AgentResponse.Text(errorMsg, targetLang)
                }
            }

            IntentType.IMAGE_ANALYSIS -> {
                if (attachedBitmap != null) {
                    val analysis = provider.analyzeImage(attachedBitmap, userText).getOrDefault("Image analyzed.")
                    return@withContext AgentResponse.Text(analysis, targetLang)
                }
            }

            IntentType.IMAGE_EDIT -> {
                if (attachedBitmap != null) {
                    val editResult = provider.editImage(attachedBitmap, userText)
                    val edited = editResult.getOrNull()
                    if (editResult.isSuccess && edited != null && edited.imageUriOrBase64.isNotBlank()) {
                        val desc = "🎨 Image edited according to instructions: \"$userText\""
                        return@withContext AgentResponse.ImageCreated(edited, desc)
                    } else {
                        val errorReason = editResult.exceptionOrNull()?.message ?: "Unknown error"
                        val errorMsg = "⚠️ Could not edit the image: $errorReason. Please check your Gemini API key in Settings."
                        return@withContext AgentResponse.Text(errorMsg, targetLang)
                    }
                }
            }

            IntentType.CALCULATOR -> {
                val tool = toolRegistry.getTool("calculator")
                val res = tool?.execute(userText, repository)
                if (res is ToolResult.Success) {
                    recordExecution("calculator", userText, res.output, System.currentTimeMillis() - startTime)
                    return@withContext AgentResponse.ToolExecuted("calculator", res.output, "Calculated using NOVA Math Engine.")
                }
            }

            IntentType.DATE_TIME -> {
                val tool = toolRegistry.getTool("date_time")
                val res = tool?.execute(userText, repository)
                if (res is ToolResult.Success) {
                    recordExecution("date_time", userText, res.output, System.currentTimeMillis() - startTime)
                    return@withContext AgentResponse.ToolExecuted("date_time", res.output, "")
                }
            }

            IntentType.WEB_SEARCH -> {
                val tool = toolRegistry.getTool("web_search")
                val res = tool?.execute(userText, repository)
                if (res is ToolResult.Success) {
                    recordExecution("web_search", userText, res.output, System.currentTimeMillis() - startTime)
                    return@withContext AgentResponse.ToolExecuted("web_search", res.output, "")
                }
            }

            IntentType.KNOWLEDGE_SEARCH -> {
                val tool = toolRegistry.getTool("knowledge_search")
                val res = tool?.execute(userText, repository)
                if (res is ToolResult.Success) {
                    recordExecution("knowledge_search", userText, res.output, System.currentTimeMillis() - startTime)
                    return@withContext AgentResponse.ToolExecuted("knowledge_search", res.output, "")
                }
            }

            IntentType.TRANSLATION -> {
                val tool = toolRegistry.getTool("translation")
                val res = tool?.execute(userText, repository)
                if (res is ToolResult.Success) {
                    return@withContext AgentResponse.ToolExecuted("translation", res.output, "")
                }
            }

            else -> {
                // Regular multi-turn chat
            }
        }

        // 4. Default Multi-Turn Conversation
        val pastEntities = repository.getMessagesList(conversationId)
        val chatMessages = pastEntities.takeLast(10).map {
            ChatMessage(role = it.role, text = it.content)
        }.toMutableList()

        chatMessages.add(ChatMessage(role = "user", text = userText))

        val systemPrompt = """
            You are NOVA AI, a premier personal AI agent & creative studio.
            You are natively fluent in Bengali (বাংলা), Hindi (हिन्दी), and English.
            Respond in the user's detected language (${targetLang.displayName}) with natural fluency.
            If the user used mixed language, respond seamlessly.
            Use crisp Markdown formatting.
            $memoryContext
        """.trimIndent()

        val reply = if (onChunkStream != null) {
            provider.streamChat(chatMessages, systemPrompt, onChunkStream).getOrDefault("NOVA AI ready.")
        } else {
            provider.chat(chatMessages, systemPrompt).getOrDefault("NOVA AI ready.")
        }

        AgentResponse.Text(reply, targetLang)
    }

    private suspend fun recordExecution(name: String, input: String, output: String, duration: Long) {
        repository.recordToolExecution(
            ToolExecutionEntity(
                id = UUID.randomUUID().toString(),
                toolName = name,
                inputJson = input,
                outputJson = output,
                status = "success",
                durationMs = duration
            )
        )
    }
}
