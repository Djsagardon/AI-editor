package com.example.ai.provider

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ai.model.AIProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "nova_ai_preferences")

class AIProviderFactory(private val context: Context) {

    private val KEY_PROVIDER_TYPE = stringPreferencesKey("ai_provider_type")
    private val KEY_CUSTOM_API_KEY = stringPreferencesKey("custom_gemini_api_key")
    private val KEY_LOCAL_SERVER_URL = stringPreferencesKey("local_server_url")
    private val KEY_LANGUAGE_MODE = stringPreferencesKey("language_mode")

    val selectedProviderType: Flow<AIProviderType> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_PROVIDER_TYPE] ?: AIProviderType.GEMINI_DIRECT.name
        try {
            AIProviderType.valueOf(raw)
        } catch (e: Exception) {
            AIProviderType.GEMINI_DIRECT
        }
    }

    val customApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_API_KEY] ?: ""
    }

    val localServerUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LOCAL_SERVER_URL] ?: "http://localhost:8080/v1"
    }

    val languageMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LANGUAGE_MODE] ?: "AUTO"
    }

    suspend fun setProviderType(type: AIProviderType) {
        context.dataStore.edit { it[KEY_PROVIDER_TYPE] = type.name }
    }

    suspend fun setCustomApiKey(key: String) {
        context.dataStore.edit { it[KEY_CUSTOM_API_KEY] = key }
    }

    suspend fun setLocalServerUrl(url: String) {
        context.dataStore.edit { it[KEY_LOCAL_SERVER_URL] = url }
    }

    suspend fun setLanguageMode(mode: String) {
        context.dataStore.edit { it[KEY_LANGUAGE_MODE] = mode }
    }

    fun getProvider(
        type: AIProviderType,
        customKey: String = "",
        localUrl: String = "http://localhost:8080/v1"
    ): AIProvider {
        return when (type) {
            AIProviderType.GEMINI_DIRECT -> GeminiAIProvider(customKey.ifBlank { null })
            AIProviderType.LOCAL_SERVER -> LocalServerAIProvider(localUrl)
            AIProviderType.CUSTOM_ENDPOINT -> LocalServerAIProvider(localUrl)
        }
    }
}
