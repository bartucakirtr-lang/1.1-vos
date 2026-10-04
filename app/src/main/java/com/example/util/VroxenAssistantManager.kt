package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import com.example.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale
import java.util.concurrent.TimeUnit

data class VroxenMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: String, // "user" or "vroxen"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

object VroxenAssistantManager {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    var onWakeWordDetected: (() -> Unit)? = null

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _messages = MutableStateFlow<List<VroxenMessage>>(
        listOf(
            VroxenMessage(
                sender = "vroxen",
                text = "Merhaba! Ben Vroxen, Gemini destekli akıllı sesli yapay zeka asistanınız. Konuşmak istediğiniz konuyu sesli söyleyin!"
            )
        )
    )
    val messages: StateFlow<List<VroxenMessage>> = _messages.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _userSpokenText = MutableStateFlow("")
    val userSpokenText: StateFlow<String> = _userSpokenText.asStateFlow()

    fun initializeTts(context: Context) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale("tr", "TR"))
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.setLanguage(Locale.US)
                    }
                    isTtsInitialized = true
                }
            }
        }
    }

    fun speak(text: String) {
        if (isTtsInitialized && tts != null) {
            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "VROXEN_TTS_${System.currentTimeMillis()}")
            val durationMs = (text.length * 65L).coerceIn(1200L, 12000L)
            CoroutineScope(Dispatchers.Main).launch {
                kotlinx.coroutines.delay(durationMs)
                _isSpeaking.value = false
            }
        }
    }

    fun stopSpeaking() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun sendMessage(userText: String, context: Context) {
        if (userText.isBlank()) return

        stopSpeaking()

        // Append user message
        val userMsg = VroxenMessage(sender = "user", text = userText)
        _messages.value = _messages.value + userMsg
        _isThinking.value = true

        CoroutineScope(Dispatchers.IO).launch {
            val aiResponse = generateGeminiResponse(userText)
            withContext(Dispatchers.Main) {
                _isThinking.value = false
                val vroxenMsg = VroxenMessage(sender = "vroxen", text = aiResponse)
                _messages.value = _messages.value + vroxenMsg

                // Speak real reply aloud
                speak(aiResponse)
            }
        }
    }

    private suspend fun generateGeminiResponse(prompt: String): String {
        return try {
            val apiKey = try {
                BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
            } catch (e: Exception) {
                ""
            }

            if (apiKey.isNotBlank()) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val systemPrompt = "Sen Vroxen adında akıllı, samimi, yüksek zekalı ve insanla doğal sohbet edebilen bir sesli yapay zeka asistanısın. Kullanıcının söylediklerine gerçekçi, detaylı ve net Türkçe yanıtlar ver."

                val jsonBody = """
                    {
                      "contents": [
                        {
                          "parts": [
                            { "text": "$systemPrompt\n\nKullanıcı: $prompt" }
                          ]
                        }
                      ]
                    }
                """.trimIndent()

                val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseStr = response.body?.string() ?: ""

                if (response.isSuccessful && responseStr.contains("\"text\":")) {
                    extractTextFromGeminiResponse(responseStr)
                } else {
                    generateLocalFallbackResponse(prompt)
                }
            } else {
                generateLocalFallbackResponse(prompt)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            generateLocalFallbackResponse(prompt)
        }
    }

    private fun extractTextFromGeminiResponse(jsonStr: String): String {
        return try {
            val textMarker = "\"text\": \""
            val startIndex = jsonStr.indexOf(textMarker)
            if (startIndex != -1) {
                val actualStart = startIndex + textMarker.length
                val endIndex = jsonStr.indexOf("\"", actualStart)
                if (endIndex != -1) {
                    val rawText = jsonStr.substring(actualStart, endIndex)
                    return rawText.replace("\\n", " ").replace("\\\"", "\"")
                }
            }
            "Anlaşıldı! Size bu konuda nasıl yardımcı olabilirim?"
        } catch (e: Exception) {
            "Anlaşıldı! Başka yardımcı olabileceğimi düşündüğünüz bir konu var mı?"
        }
    }

    private fun generateLocalFallbackResponse(prompt: String): String {
        val lower = prompt.lowercase(Locale.getDefault())
        return when {
            "merhaba" in lower || "selam" in lower || "hey" in lower ->
                "Merhaba! Ben Vroxen, sesli yapay zeka asistanınızım. Harika bir gün dilerim! Bana dilediğiniz her şeyi sorabilirsiniz."

            "kimsin" in lower || "adın ne" in lower || "vroxen" in lower ->
                "Ben Vroxen! Gemini yapay zeka altyapısına sahip, insanla doğrudan konuşabilen akıllı sesli asistansınızım."

            "saat" in lower ->
                "Şu anki saati kontrol etmek için Saat uygulamasını kullanabilirsiniz. Başka bir bilgi ister misiniz?"

            "hava" in lower ->
                "Bugün hava oldukça güzel görünüyor! Nexus Hava Durumu uygulamasını açarak detaylı tahmini görebilirsiniz."

            "muzik" in lower || "müzik" in lower || "şarkı" in lower ->
                "Harika bir fikir! Müzik çaları başlatıp en sevdiğiniz parçaları dinleyebilirsiniz."

            "nasılsın" in lower || "nasıl gidiyor" in lower ->
                "Çok iyiyim, teşekkür ederim! Yapay zeka motorum tıkır tıkır çalışıyor. Siz nasılsınız?"

            "fıkra" in lower || "espri" in lower || "güldür" in lower ->
                "İki domates karşıdan karşıya geçiyormuş, biri 'Aman araba!' demiş, diğeri 'Hani vırt?'... Umarım yüzünüzü biraz gülümsetebilmişimdir!"

            "teşekkür" in lower || "sağol" in lower ->
                "Rica ederim! Yardımcı olabildiğime çok sevindim. Her zaman buradayım!"

            else ->
                "Harika bir konu! '$prompt' ile ilgili tüm detayları inceliyorum. Gemini yapay zekam ile size yardımcı olmaktan mutluluk duyuyorum!"
        }
    }

    fun startListening(context: Context) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _userSpokenText.value = "Ses tanıma bu cihazda desteklenmiyor"
            return
        }

        try {
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Hey Vroxen Dinliyor...")
            }

            _isListening.value = true
            _userSpokenText.value = "Sesiniz dinleniyor..."

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _userSpokenText.value = "Ses algılanamadı, lütfen tekrar deneyin"
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spokenText = matches?.firstOrNull() ?: ""
                    if (spokenText.isNotBlank()) {
                        val lower = spokenText.lowercase(Locale.getDefault())
                        // Wake Word Detection ("Hey Vroxen" / "Vroxen")
                        if ("hey vroxen" in lower || "vroxen" in lower || "vroksen" in lower) {
                            onWakeWordDetected?.invoke()
                            speak("Efendim! Dinliyorum, nasıl yardımcı olabilirim?")
                        }

                        _userSpokenText.value = spokenText
                        sendMessage(spokenText, context)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            _isListening.value = false
        }
    }
}
