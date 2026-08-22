package com.example.ai.model

enum class LanguageMode(val code: String, val displayName: String, val nativeName: String) {
    AUTO("AUTO", "Auto Detect", "স্বয়ংক্রিয় / ऑटो"),
    BENGALI("BN", "Bengali", "বাংলা"),
    HINDI("HI", "Hindi", "हिन्दी"),
    ENGLISH("EN", "English", "English");

    companion object {
        fun fromCode(code: String): LanguageMode =
            values().find { it.code.equals(code, ignoreCase = true) } ?: AUTO
    }
}

enum class AIProviderType(val displayName: String) {
    GEMINI_DIRECT("Google Gemini (Cloud AI)"),
    LOCAL_SERVER("Local / Self-Hosted Server"),
    CUSTOM_ENDPOINT("Custom OpenAI Compatible API")
}

data class ChatMessage(
    val role: String, // "user", "assistant", "system"
    val text: String,
    val imageBase64: String? = null
)

data class GeneratedImageResult(
    val imageUriOrBase64: String,
    val prompt: String,
    val width: Int = 1024,
    val height: Int = 1024,
    val providerUsed: String = "Gemini AI"
)

enum class IntentType {
    CHAT,
    IMAGE_GENERATION,
    IMAGE_EDIT,
    IMAGE_ANALYSIS,
    WEB_SEARCH,
    KNOWLEDGE_SEARCH,
    CALCULATOR,
    DATE_TIME,
    TRANSLATION,
    MEMORY_UPDATE,
    TEMPLATE_MATCH
}

data class DetectedIntent(
    val type: IntentType,
    val confidence: Float,
    val extractedPrompt: String,
    val language: LanguageMode,
    val parameters: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String? = null
)
