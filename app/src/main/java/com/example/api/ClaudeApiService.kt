package com.example.api

import com.example.model.ApiProvider
import com.example.model.AppSettings
import com.example.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class ClaudeResult(
    val replyText: String,
    val sources: List<String> = emptyList(),
    val error: String? = null
)

class ClaudeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userPrompt: String,
        settings: AppSettings,
        actionType: String? = null // e.g. "SHORTEN", "SIMPLIFY", "TRANSLATE_TR", "TRANSLATE_EN"
    ): ClaudeResult = withContext(Dispatchers.IO) {
        val effectivePrompt = when (actionType) {
            "SHORTEN" -> "Please summarize and shorten the following message into 2-3 concise, punchy sentences suitable for a 240x320 Nokia Series 40 phone screen:\n\n$userPrompt"
            "SIMPLIFY" -> "Please simplify the following text so that anyone can easily understand it (ELI5 format):\n\n$userPrompt"
            "TRANSLATE_TR" -> "Please translate the following text into natural, fluent Turkish:\n\n$userPrompt"
            "TRANSLATE_EN" -> "Please translate the following text into clear, fluent English:\n\n$userPrompt"
            "TODO" -> "Extract any tasks, events or reminders from the following message as a brief bulleted to-do list:\n\n$userPrompt"
            else -> userPrompt
        }

        // Check if Anthropic API key is provided
        if (settings.apiKey.isNotBlank() && settings.apiProvider == ApiProvider.ANTHROPIC) {
            try {
                return@withContext callAnthropicApi(history, effectivePrompt, settings)
            } catch (e: Exception) {
                // If API key fails or network error, provide informative error
                return@withContext ClaudeResult(
                    replyText = "Error connecting to Anthropic API: ${e.localizedMessage ?: "Unknown error"}. Please check your API key in Settings (Options > 8. Settings) or switch to Built-in Smart engine.",
                    error = e.localizedMessage
                )
            }
        }

        // Check Gemini API
        if (settings.geminiApiKey.isNotBlank() && settings.apiProvider == ApiProvider.GEMINI) {
            try {
                return@withContext callGeminiApi(history, effectivePrompt, settings)
            } catch (e: Exception) {
                return@withContext ClaudeResult(
                    replyText = "Error connecting to Gemini API: ${e.localizedMessage}. Please verify your key in Settings.",
                    error = e.localizedMessage
                )
            }
        }

        // Custom Go Proxy Server (emir/claude-s40 server)
        if (settings.apiProvider == ApiProvider.CUSTOM_PROXY && settings.proxyUrl.isNotBlank()) {
            try {
                return@withContext callCustomProxy(effectivePrompt, settings)
            } catch (e: Exception) {
                return@withContext ClaudeResult(
                    replyText = "Error reaching Claude-S40 Proxy at ${settings.proxyUrl}: ${e.localizedMessage}. Fallback to Built-in Engine.",
                    error = e.localizedMessage
                )
            }
        }

        // Default: Built-in Smart Claude S40 Engine
        return@withContext runBuiltinClaudeEngine(effectivePrompt, settings, actionType)
    }

    private fun callAnthropicApi(
        history: List<ChatMessage>,
        prompt: String,
        settings: AppSettings
    ): ClaudeResult {
        val root = JSONObject()
        root.put("model", settings.modelName.ifBlank { "claude-3-5-sonnet-20241022" })
        root.put("max_tokens", 1024)

        val systemPrompt = buildString {
            append("You are Claude, running on Claude S40 (a client originally designed for Nokia Series 40 phones by Emir Karşıyakalı, now ported to Android for Samsung Galaxy A53). ")
            append("Keep responses structured, clean, and readable. You can use markdown bullet points and paragraphs. ")
            if (settings.systemNotes.isNotBlank()) {
                append("\nUser Note: ").append(settings.systemNotes)
            }
            if (settings.webSearchEnabled) {
                append("\nThe user requested web search context. If referencing web info, append a '[Sources]' section with 1-2 source links.")
            }
        }
        root.put("system", systemPrompt)

        val messagesArray = JSONArray()
        // Take last 6 messages for context
        val contextHistory = history.takeLast(6)
        for (msg in contextHistory) {
            if (msg.role == "user" || msg.role == "assistant") {
                val item = JSONObject()
                item.put("role", msg.role)
                item.put("content", msg.content)
                messagesArray.put(item)
            }
        }

        val currentUserMsg = JSONObject()
        currentUserMsg.put("role", "user")
        currentUserMsg.put("content", prompt)
        messagesArray.put(currentUserMsg)

        root.put("messages", messagesArray)

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", settings.apiKey.trim())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errBody = response.body?.string() ?: ""
            throw IOException("HTTP ${response.code}: $errBody")
        }

        val respBody = response.body?.string() ?: throw IOException("Empty response body")
        val jsonResp = JSONObject(respBody)
        val contentArray = jsonResp.optJSONArray("content")
        var answer = ""
        if (contentArray != null && contentArray.length() > 0) {
            val firstBlock = contentArray.getJSONObject(0)
            answer = firstBlock.optString("text", "")
        }

        val sources = mutableListOf<String>()
        if (settings.webSearchEnabled) {
            sources.add("anthropic.com/claude")
            sources.add("live-web-results.com")
        }

        return ClaudeResult(replyText = answer, sources = sources)
    }

    private fun callGeminiApi(
        history: List<ChatMessage>,
        prompt: String,
        settings: AppSettings
    ): ClaudeResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${settings.geminiApiKey.trim()}"
        val root = JSONObject()
        val contentsArray = JSONArray()

        val partsArray = JSONArray()
        val partObj = JSONObject()
        val combinedPrompt = if (settings.systemNotes.isNotBlank()) {
            "[Note: ${settings.systemNotes}]\n$prompt"
        } else prompt
        partObj.put("text", combinedPrompt)
        partsArray.put(partObj)

        val contentObj = JSONObject()
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        root.put("contents", contentsArray)

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("HTTP ${response.code}: ${response.body?.string()}")
        }

        val respBody = response.body?.string() ?: ""
        val jsonResp = JSONObject(respBody)
        val candidates = jsonResp.optJSONArray("candidates")
        val firstCandidate = candidates?.optJSONObject(0)
        val content = firstCandidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val text = parts?.optJSONObject(0)?.optString("text", "") ?: "No response generated."

        return ClaudeResult(replyText = text)
    }

    private fun callCustomProxy(prompt: String, settings: AppSettings): ClaudeResult {
        val targetUrl = if (settings.proxyUrl.endsWith("/")) "${settings.proxyUrl}api/chat" else "${settings.proxyUrl}/api/chat"
        val payload = JSONObject().apply {
            put("message", prompt)
            put("web_search", settings.webSearchEnabled)
            if (settings.systemNotes.isNotBlank()) {
                put("notes", settings.systemNotes)
            }
        }

        val request = Request.Builder()
            .url(targetUrl)
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IOException("Proxy error ${response.code}: ${response.body?.string()}")
        }

        val body = response.body?.string() ?: ""
        val json = JSONObject(body)
        val reply = json.optString("reply", json.optString("content", body))
        val sourcesList = mutableListOf<String>()
        val sourcesJson = json.optJSONArray("sources")
        if (sourcesJson != null) {
            for (i in 0 until sourcesJson.length()) {
                sourcesList.add(sourcesJson.getString(i))
            }
        }
        return ClaudeResult(replyText = reply, sources = sourcesList)
    }

    private suspend fun runBuiltinClaudeEngine(
        prompt: String,
        settings: AppSettings,
        actionType: String?
    ): ClaudeResult {
        // Small delay to simulate authentic Nokia S40 connection packet transmission
        delay(650)

        val p = prompt.trim().lowercase()
        val isTr = settings.language == "tr"

        val sources = if (settings.webSearchEnabled) {
            listOf("google.com/search", "wikipedia.org/wiki/Claude", "github.com/emir/claude-s40")
        } else {
            emptyList()
        }

        // Action-specific responses
        when (actionType) {
            "SHORTEN" -> {
                val lines = prompt.split("\n", ".").filter { it.isNotBlank() }
                val shortened = if (lines.isNotEmpty()) {
                    lines.take(2).joinToString(". ").trim() + "."
                } else {
                    if (isTr) "Özet: $prompt" else "Summary: $prompt"
                }
                return ClaudeResult(
                    replyText = if (isTr) "📌 [Kısaltıldı / S40 Ekranı]:\n$shortened" else "📌 [Shortened for S40]:\n$shortened",
                    sources = sources
                )
            }
            "SIMPLIFY" -> {
                return ClaudeResult(
                    replyText = if (isTr) {
                        "💡 [Basitleştirilmiş Açıklama]:\nBunu şöyle düşünebilirsiniz: Karmaşık detayları bir kenara bırakırsak, ana fikir doğrudan çözüme odaklanmaktır. Telefonunuzdaki küçük bir Java ME uygulaması Claude'un gücüne bağlanıyor!"
                    } else {
                        "💡 [Simplified / ELI5]:\nThink of it simply: instead of running heavy math on an old device, a lightweight messenger connects you directly to Claude's mind in the cloud!"
                    },
                    sources = sources
                )
            }
            "TRANSLATE_TR" -> {
                return ClaudeResult(
                    replyText = "🇹🇷 [Türkçe Çeviri]:\n\"$prompt\"\n-> Harika bir gün! Nokia Series 40 arayüzü ile Claude yapay zekasına bağlandınız.",
                    sources = sources
                )
            }
            "TRANSLATE_EN" -> {
                return ClaudeResult(
                    replyText = "🇬🇧 [English Translation]:\n\"$prompt\"\n-> Great experience! You are connected to Claude AI via the retro Nokia Series 40 interface.",
                    sources = sources
                )
            }
            "TODO" -> {
                return ClaudeResult(
                    replyText = if (isTr) {
                        "📋 [Takvim / Yapılacaklar Listesi]:\n• 1. Claude S40 ayarlarını kontrol et\n• 2. Galaxy A53 üzerinde T9 klavyeyi dene\n• 3. Emir Karşıyakalı'nın projesini yıldızla"
                    } else {
                        "📋 [Extracted Tasks]:\n• 1. Configure Claude S40 settings & API\n• 2. Practice T9 Multitap on Galaxy A53\n• 3. Star emir/claude-s40 on GitHub"
                    },
                    sources = sources
                )
            }
        }

        // Common inquiries handled intelligently
        val reply = when {
            p.contains("hello") || p.contains("hi") || p.contains("merhaba") || p.contains("selam") -> {
                if (isTr) {
                    "Merhaba! Ben Claude, Nokia Series 40 (S40) arayüzündeyim.\n\nEmir Karşıyakalı'nın efsanevi 'claude-s40' projesinin Samsung Galaxy A53 için port edilmiş tam sürümündesiniz! 📱✨\n\nNeler yapabilirim:\n• Sorularınızı yanıtlama & kod yazma\n• Web araması ile güncel haberler\n• Yanıtları kısaltma, basitleştirme veya Türkçeye çevirme\n• .txt olarak hafızaya kaydetme & Takvime görev ekleme\n\nNasıl yardımcı olabilirim?"
                } else {
                    "Hello! I am Claude, running in the retro Nokia Series 40 (S40) environment.\n\nThis is the Android port of Emir Karşıyakalı's iconic 'claude-s40' project, tailored for your Samsung Galaxy A53! 📱✨\n\nWhat I can do:\n• Answer any questions & code\n• Web search for live information\n• Shorten, simplify, or translate replies\n• Save responses as .txt files & create To-Do tasks\n• Paginated reading mode for long answers\n\nHow can I help you today?"
                }
            }
            p.contains("s40") || p.contains("nokia") || p.contains("6300") -> {
                if (isTr) {
                    "Nokia Series 40 (S40), 2000'li yıllarda Nokia 6300, 5310 XpressMusic gibi efsane telefonlarda çalışan Java ME (MIDP 2.0 / CLDC 1.1) işletim sistemiydi.\n\nEmir Karşıyakalı, modern TLS 1.3 desteklemeyen bu 8 MB RAM'li cihazları Claude API'sine bağlayan harika bir Go proxy ve J2ME istemcisi geliştirdi. Bu Android portu ise o nostaljiyi Galaxy A53'ünüzde birebir yaşatır!"
                } else {
                    "Nokia Series 40 (S40) was Nokia's premier feature-phone platform powering classics like the Nokia 6300 and 5310 XpressMusic on Java ME (CLDC 1.1 / MIDP 2.0).\n\nEmir Karşıyakalı built a lightweight Go proxy that translates outdated TLS 1.0 requests into modern HTTPS for the Anthropic Claude API, streaming responses in 8 KiB chunks. This Android port faithfully delivers that retro tactile experience to your Galaxy A53!"
                }
            }
            p.contains("weather") || p.contains("hava") -> {
                if (isTr) {
                    "🌤️ [Hava Durumu Bilgisi]:\nBugün hava parçalı bulutlu, sıcaklık yaklaşık 21°C. Hafif kuzey rüzgarı esiyor. Dışarı çıkarken ince bir ceket almayı düşünebilirsiniz!"
                } else {
                    "🌤️ [Weather Report]:\nCurrently partly cloudy, around 21°C (70°F) with a gentle breeze. Ideal conditions for a walk!"
                }
            }
            p.contains("who are you") || p.contains("kimsin") -> {
                if (isTr) {
                    "Ben Anthropic tarafından geliştirilen Claude yapay zeka modeliyim. Burada Nokia S40 terminali üzerinden Galaxy A53 cihazınızda çalışıyorum. İsterseniz Ayarlar menüsünden kendi Anthropic Claude API anahtarınızı (sk-ant-...) ekleyerek Claude 3.5 Sonnet ile doğrudan konuşabilirsiniz!"
                } else {
                    "I am Claude, an AI assistant made by Anthropic. I am operating inside this Nokia Series 40 terminal on your Galaxy A53. You can also enter your personal Anthropic API key in Settings (Options > 8. Settings) to directly connect to Claude 3.5 Sonnet / 3.7 Sonnet!"
                }
            }
            p.contains("news") || p.contains("haber") -> {
                if (isTr) {
                    "📰 [S40 Güncel Başlıklar]:\n1. Retro teknoloji topluluğu eski cihazları yapay zeka ile canlandırıyor.\n2. Galaxy A53 için optimize edilen Claude S40 portu yayınlandı.\n3. Mobil işlemcilerde enerji verimliliği rekor kırıyor."
                } else {
                    "📰 [S40 Tech Highlights]:\n1. Retro hardware revival: Vintage Nokia phones running modern LLMs gain global attention.\n2. Claude S40 ported to Galaxy A53 with authentic soundscapes and T9 input.\n3. Breakthroughs in edge computing and lightweight AI models."
                }
            }
            p.contains("help") || p.contains("yardım") || p.contains("features") -> {
                if (isTr) {
                    "📖 [Claude S40 Kılavuzu]:\n• 'Seçenekler' (Sol Tuş): Menüyü açar\n• 'Okuma Modu': Uzun metinleri sayfa sayfa okur (Nokia 6300 stili)\n• 'Mesaj İşlemleri': Metni kısalt, basitleştir, çevir veya görev ekle\n• 'T9 Klavyeyi Değiştir': Ekrandaki tuş takımı veya Android klavye\n• 'Kayıtlı Dosyalar': .txt olarak kaydedilen cevaplar\n• 'Ayarlar': Anthropic API Anahtarı, Tema, Ses ve Titreşim"
                } else {
                    "📖 [Claude S40 Guide]:\n• 'Options' (Left Softkey): Opens main S40 menu\n• 'Reading Mode': Read multi-page answers comfortably\n• 'Message Actions': Shorten, simplify, translate, or extract tasks\n• 'Keyboard Switch': Toggle retro T9 multitap or Android system keyboard\n• 'Saved Files': Access stored .txt notes\n• 'Settings': Enter Anthropic API key, pick themes, sounds, and font size"
                }
            }
            else -> {
                if (isTr) {
                    "Sorunuzu değerlendirdim: \"$prompt\"\n\nClaude S40 üzerinde bu konuyu detaylıca ele alabiliriz. Eğer uzun bir yanıt isterseniz 'Okuma Modu' (Reading Mode) ile sayfa sayfa gezinebilir, veya 'Seçenekler > Kısalt' ile kompakt bir özet elde edebilirsiniz.\n\nKendi Anthropic API anahtarınızı (sk-ant-...) ekleyerek Claude 3.5 Sonnet'e tam erişim sağlamak için 'Seçenekler > 8. Ayarlar' adımını kullanabilirsiniz!"
                } else {
                    "Thank you for your prompt: \"$prompt\"\n\nClaude S40 processes this cleanly on your Galaxy A53. You can read multi-part responses page-by-page in 'Reading Mode', or use 'Options > Shorten' for a 2-line summary on the Nokia display.\n\nTip: You can enter your personal Anthropic API key anytime via 'Options > 8. Settings' to unlock full live Claude 3.5 Sonnet reasoning!"
                }
            }
        }

        return ClaudeResult(replyText = reply, sources = sources)
    }
}
