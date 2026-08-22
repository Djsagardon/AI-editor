package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.ai.model.LanguageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    MUTED,
    ERROR
}

class VoiceManager(private val context: Context) {

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _audioRmsLevel = MutableStateFlow(0f)
    val audioRmsLevel: StateFlow<Float> = _audioRmsLevel.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isDictating = MutableStateFlow(false)
    val isDictating: StateFlow<Boolean> = _isDictating.asStateFlow()

    private var dictationRecognizer: SpeechRecognizer? = null
    var onSpeechRecognized: ((String) -> Unit)? = null
    var onDictationChunk: ((String, Boolean) -> Unit)? = null // text, isFinal

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.ENGLISH
                textToSpeech?.setSpeechRate(1.0f)
                textToSpeech?.setPitch(1.0f)
                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                    }

                    override fun onError(utteranceId: String?) {
                        _voiceState.value = VoiceState.IDLE
                    }
                })
            }
        }
    }

    fun startDictation(
        language: LanguageMode = LanguageMode.AUTO,
        onChunk: (String, Boolean) -> Unit
    ) {
        stopSpeaking()
        stopListening()
        this.onDictationChunk = onChunk

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _isDictating.value = false
            return
        }

        dictationRecognizer?.destroy()
        dictationRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isDictating.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isDictating.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _audioRmsLevel.value = (rmsdB.coerceIn(-2f, 10f) + 2f) / 12f
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isDictating.value = false
                }

                override fun onError(error: Int) {
                    _isDictating.value = false
                    _audioRmsLevel.value = 0f
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _isDictating.value = false
                    _audioRmsLevel.value = 0f
                    if (text.isNotBlank()) {
                        onDictationChunk?.invoke(text, true)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        onDictationChunk?.invoke(text, false)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            when (language) {
                LanguageMode.BENGALI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-IN")
                }
                LanguageMode.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                }
                LanguageMode.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                }
                LanguageMode.AUTO -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                }
            }
        }

        try {
            dictationRecognizer?.startListening(intent)
            _isDictating.value = true
        } catch (e: Exception) {
            _isDictating.value = false
        }
    }

    fun stopDictation() {
        try {
            dictationRecognizer?.stopListening()
        } catch (e: Exception) {}
        _isDictating.value = false
        _audioRmsLevel.value = 0f
    }

    fun startListening(language: LanguageMode = LanguageMode.AUTO) {
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _voiceState.value = VoiceState.ERROR
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _voiceState.value = VoiceState.LISTENING
                }

                override fun onBeginningOfSpeech() {
                    _voiceState.value = VoiceState.LISTENING
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _audioRmsLevel.value = (rmsdB.coerceIn(-2f, 10f) + 2f) / 12f
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _voiceState.value = VoiceState.PROCESSING
                }

                override fun onError(error: Int) {
                    _voiceState.value = VoiceState.IDLE
                    _audioRmsLevel.value = 0f
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    _spokenText.value = text
                    _voiceState.value = VoiceState.IDLE
                    _audioRmsLevel.value = 0f
                    if (text.isNotBlank()) {
                        onSpeechRecognized?.invoke(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { _spokenText.value = it }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            when (language) {
                LanguageMode.BENGALI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-IN")
                }
                LanguageMode.HINDI -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                }
                LanguageMode.ENGLISH -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                }
                LanguageMode.AUTO -> {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                }
            }
        }

        try {
            speechRecognizer?.startListening(intent)
            _voiceState.value = VoiceState.LISTENING
        } catch (e: Exception) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _voiceState.value = VoiceState.IDLE
        _audioRmsLevel.value = 0f
    }

    fun speakText(text: String, language: LanguageMode = LanguageMode.AUTO) {
        if (!isTtsReady || text.isBlank()) return

        stopSpeaking()

        // Configure TTS language
        when (language) {
            LanguageMode.BENGALI -> {
                val bnLocale = Locale("bn", "IN")
                textToSpeech?.language = if (textToSpeech?.isLanguageAvailable(bnLocale) == TextToSpeech.LANG_AVAILABLE) bnLocale else Locale.ENGLISH
            }
            LanguageMode.HINDI -> {
                val hiLocale = Locale("hi", "IN")
                textToSpeech?.language = if (textToSpeech?.isLanguageAvailable(hiLocale) == TextToSpeech.LANG_AVAILABLE) hiLocale else Locale.ENGLISH
            }
            else -> {
                // Auto detect by characters in string
                if (text.any { it in '\u0980'..'\u09FF' }) {
                    textToSpeech?.language = Locale("bn", "IN")
                } else if (text.any { it in '\u0900'..'\u097F' }) {
                    textToSpeech?.language = Locale("hi", "IN")
                } else {
                    textToSpeech?.language = Locale.US
                }
            }
        }

        // Clean markdown characters before speech
        val cleanText = text.replace(Regex("[*#_`>]"), "").replace("\n", " ").trim()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "NOVA_SPEECH_ID")
        }

        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "NOVA_SPEECH_ID")
        _voiceState.value = VoiceState.SPEAKING
    }

    fun stopSpeaking() {
        if (textToSpeech?.isSpeaking == true) {
            textToSpeech?.stop()
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun destroy() {
        dictationRecognizer?.destroy()
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}
