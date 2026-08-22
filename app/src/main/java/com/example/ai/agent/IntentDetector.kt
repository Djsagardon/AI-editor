package com.example.ai.agent

import com.example.ai.model.DetectedIntent
import com.example.ai.model.IntentType
import com.example.ai.model.LanguageMode

class IntentDetector {

    fun detectIntent(userPrompt: String, hasImageAttachment: Boolean = false): DetectedIntent {
        val raw = userPrompt.trim()
        val lower = raw.lowercase()

        val detectedLang = detectLanguage(raw)

        // If user uploaded an image and wrote prompt
        if (hasImageAttachment) {
            if (lower.contains("edit") || lower.contains("remove") || lower.contains("change") ||
                lower.contains("background") || lower.contains("filter") || lower.contains("relight") ||
                lower.contains("bg") || lower.contains("বদলে") || lower.contains("बदल")
            ) {
                return DetectedIntent(
                    type = IntentType.IMAGE_EDIT,
                    confidence = 0.95f,
                    extractedPrompt = raw,
                    language = detectedLang
                )
            }
            return DetectedIntent(
                type = IntentType.IMAGE_ANALYSIS,
                confidence = 0.95f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Image Generation Intent (Multilingual: English, Bengali, Hindi)
        val isBengaliImageGen = lower.contains("ছবি") || lower.contains("ছবি আঁকো") ||
                lower.contains("তৈরি করো") && (lower.contains("ছবি") || lower.contains("photo") || lower.contains("portrait") || lower.contains("ল্যান্ডস্কেপ")) ||
                lower.contains("বানিয়ে দাও") && (lower.contains("ছবি") || lower.contains("photo") || lower.contains("portrait")) ||
                lower.contains("cinematic photo") || lower.contains("cinematic portrait") || lower.contains("wallpaper")

        val isHindiImageGen = lower.contains("फोटो") || lower.contains("तस्वीर") || lower.contains("चित्र") ||
                lower.contains("बनाओ") && (lower.contains("फोटो") || lower.contains("तस्वीर") || lower.contains("portrait") || lower.contains("photo") || lower.contains("landscape")) ||
                lower.contains("चित्र बनाइए")

        val isEnglishImageGen = lower.startsWith("generate") ||
                lower.startsWith("create a photo") || lower.startsWith("create an image") ||
                lower.startsWith("create a cinematic") || lower.startsWith("create a realistic") ||
                lower.startsWith("create a futuristic") || lower.startsWith("create a ") && (lower.contains("photo") || lower.contains("image") || lower.contains("portrait") || lower.contains("landscape") || lower.contains("city") || lower.contains("art") || lower.contains("picture")) ||
                lower.startsWith("draw") || lower.contains("cinematic photo") || lower.contains("generate image") ||
                lower.contains("paint a") || lower.startsWith("make an image") || lower.startsWith("render")

        if (isBengaliImageGen || isHindiImageGen || isEnglishImageGen) {
            // Keep the core visual prompt with full description
            var cleanPrompt = raw
            if (cleanPrompt.startsWith("generate an image of", ignoreCase = true)) {
                cleanPrompt = cleanPrompt.substring(21).trim()
            } else if (cleanPrompt.startsWith("generate a photo of", ignoreCase = true)) {
                cleanPrompt = cleanPrompt.substring(20).trim()
            } else if (cleanPrompt.startsWith("create an image of", ignoreCase = true)) {
                cleanPrompt = cleanPrompt.substring(19).trim()
            } else if (cleanPrompt.startsWith("create a photo of", ignoreCase = true)) {
                cleanPrompt = cleanPrompt.substring(18).trim()
            } else if (cleanPrompt.startsWith("create a", ignoreCase = true) && !cleanPrompt.startsWith("create a cinematic", ignoreCase = true) && !cleanPrompt.startsWith("create a realistic", ignoreCase = true)) {
                cleanPrompt = cleanPrompt.substring(8).trim()
            }

            return DetectedIntent(
                type = IntentType.IMAGE_GENERATION,
                confidence = 0.95f,
                extractedPrompt = if (cleanPrompt.isBlank()) raw else cleanPrompt,
                language = detectedLang
            )
        }

        // Calculator Intent
        if (lower.startsWith("calculate") || (lower.startsWith("what is ") && lower.matches(Regex(".*\\d+\\s*[+\\-*/%^]\\s*\\d+.*")))) {
            return DetectedIntent(
                type = IntentType.CALCULATOR,
                confidence = 0.98f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Date & Time Intent
        if (lower.contains("what time") || lower.contains("current time") || lower.contains("today's date") ||
            lower.contains("আজকের তারিখ") || lower.contains("आज की तारीख") || lower.contains("এখন কয়টা বাজে") ||
            lower.contains("সময় কত")
        ) {
            return DetectedIntent(
                type = IntentType.DATE_TIME,
                confidence = 0.95f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Web Search / Live Info Intent
        if (lower.startsWith("search") || lower.contains("weather in") || lower.contains("today's news") ||
            lower.contains("latest price") || lower.contains("current weather") || lower.contains("আজকের আবহাওয়া") ||
            lower.contains("आज का मौसम")
        ) {
            return DetectedIntent(
                type = IntentType.WEB_SEARCH,
                confidence = 0.90f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Translation Intent
        if (lower.contains("translate to") || lower.contains("অনুবাদ করো") || lower.contains("अनुवाद करो")) {
            return DetectedIntent(
                type = IntentType.TRANSLATION,
                confidence = 0.92f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Knowledge / File Search Intent
        if (lower.contains("in my document") || lower.contains("my notes") || lower.contains("আমার ডকুমেন্টে") || lower.contains("मेरे नोट्स")) {
            return DetectedIntent(
                type = IntentType.KNOWLEDGE_SEARCH,
                confidence = 0.88f,
                extractedPrompt = raw,
                language = detectedLang
            )
        }

        // Default to General Chat
        return DetectedIntent(
            type = IntentType.CHAT,
            confidence = 0.85f,
            extractedPrompt = raw,
            language = detectedLang
        )
    }

    fun detectLanguage(text: String): LanguageMode {
        var bengaliCount = 0
        var devanagariCount = 0
        var latinCount = 0

        for (ch in text) {
            when (ch) {
                in '\u0980'..'\u09FF' -> bengaliCount++
                in '\u0900'..'\u097F' -> devanagariCount++
                in 'a'..'z', in 'A'..'Z' -> latinCount++
            }
        }

        return when {
            bengaliCount > 0 && bengaliCount >= devanagariCount -> LanguageMode.BENGALI
            devanagariCount > 0 && devanagariCount > bengaliCount -> LanguageMode.HINDI
            else -> LanguageMode.ENGLISH
        }
    }
}
