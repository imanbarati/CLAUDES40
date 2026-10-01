package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ApiProvider
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.KeyboardMode
import com.example.model.S40Task
import com.example.model.S40Theme
import com.example.model.S40ViewMode
import com.example.model.SavedTextFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ClaudeS40Repository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("claude_s40_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    private val _conversationsFlow = MutableStateFlow(loadConversations())
    val conversationsFlow: StateFlow<List<Conversation>> = _conversationsFlow.asStateFlow()

    private val _savedFilesFlow = MutableStateFlow(loadSavedFiles())
    val savedFilesFlow: StateFlow<List<SavedTextFile>> = _savedFilesFlow.asStateFlow()

    private val _tasksFlow = MutableStateFlow(loadTasks())
    val tasksFlow: StateFlow<List<S40Task>> = _tasksFlow.asStateFlow()

    private fun loadSettings(): AppSettings {
        val apiKey = prefs.getString("api_key", "") ?: ""
        val geminiKey = prefs.getString("gemini_api_key", "") ?: ""
        val providerStr = prefs.getString("api_provider", ApiProvider.BUILTIN_SMART.name) ?: ApiProvider.BUILTIN_SMART.name
        val provider = try { ApiProvider.valueOf(providerStr) } catch (_: Exception) { ApiProvider.BUILTIN_SMART }
        val model = prefs.getString("model_name", "claude-3-5-sonnet-20241022") ?: "claude-3-5-sonnet-20241022"
        val proxy = prefs.getString("proxy_url", "http://10.0.2.2:8080") ?: "http://10.0.2.2:8080"
        val lang = prefs.getString("language", "en") ?: "en"
        val themeStr = prefs.getString("theme", S40Theme.SAPPHIRE_BLUE.name) ?: S40Theme.SAPPHIRE_BLUE.name
        val theme = try { S40Theme.valueOf(themeStr) } catch (_: Exception) { S40Theme.SAPPHIRE_BLUE }
        val viewModeStr = prefs.getString("view_mode", S40ViewMode.NOKIA_6300_DEVICE.name) ?: S40ViewMode.NOKIA_6300_DEVICE.name
        val viewMode = try { S40ViewMode.valueOf(viewModeStr) } catch (_: Exception) { S40ViewMode.NOKIA_6300_DEVICE }
        val kbModeStr = prefs.getString("kb_mode", KeyboardMode.T9_KEYPAD.name) ?: KeyboardMode.T9_KEYPAD.name
        val kbMode = try { KeyboardMode.valueOf(kbModeStr) } catch (_: Exception) { KeyboardMode.T9_KEYPAD }
        val sound = prefs.getBoolean("sound_enabled", true)
        val vibrate = prefs.getBoolean("vibrate_enabled", true)
        val webSearch = prefs.getBoolean("web_search_enabled", false)
        val fontSize = prefs.getInt("reading_font_size", 14)
        val sysNotes = prefs.getString("system_notes", "") ?: ""

        return AppSettings(
            apiKey = apiKey,
            geminiApiKey = geminiKey,
            apiProvider = provider,
            modelName = model,
            proxyUrl = proxy,
            language = lang,
            theme = theme,
            viewMode = viewMode,
            keyboardMode = kbMode,
            soundEnabled = sound,
            vibrateEnabled = vibrate,
            webSearchEnabled = webSearch,
            readingFontSize = fontSize,
            systemNotes = sysNotes
        )
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putString("api_key", settings.apiKey)
            putString("gemini_api_key", settings.geminiApiKey)
            putString("api_provider", settings.apiProvider.name)
            putString("model_name", settings.modelName)
            putString("proxy_url", settings.proxyUrl)
            putString("language", settings.language)
            putString("theme", settings.theme.name)
            putString("view_mode", settings.viewMode.name)
            putString("kb_mode", settings.keyboardMode.name)
            putBoolean("sound_enabled", settings.soundEnabled)
            putBoolean("vibrate_enabled", settings.vibrateEnabled)
            putBoolean("web_search_enabled", settings.webSearchEnabled)
            putInt("reading_font_size", settings.readingFontSize)
            putString("system_notes", settings.systemNotes)
            apply()
        }
        _settingsFlow.value = settings
    }

    fun getActiveConversationId(): String {
        val existing = _conversationsFlow.value
        if (existing.isNotEmpty()) {
            return existing.first().id
        }
        val newConv = Conversation(
            id = UUID.randomUUID().toString(),
            title = "Chat with Claude",
            lastMessage = "Welcome to Claude S40",
            updatedAt = System.currentTimeMillis()
        )
        saveConversation(newConv)
        return newConv.id
    }

    private fun loadConversations(): List<Conversation> {
        val raw = prefs.getString("conversations_json", null) ?: return listOf(
            Conversation(
                id = "default-chat",
                title = "Welcome to Claude S40",
                lastMessage = "Hello! S40 client ready for Galaxy A53.",
                updatedAt = System.currentTimeMillis(),
                isPinned = true,
                messageCount = 2
            )
        )
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<Conversation>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    Conversation(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        lastMessage = obj.optString("lastMessage", ""),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis()),
                        isPinned = obj.optBoolean("isPinned", false),
                        messageCount = obj.optInt("messageCount", 0)
                    )
                )
            }
            list.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.updatedAt })
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveConversation(conv: Conversation) {
        val current = _conversationsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == conv.id }
        if (index >= 0) {
            current[index] = conv
        } else {
            current.add(0, conv)
        }
        val sorted = current.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.updatedAt })
        persistConversations(sorted)
    }

    fun togglePinConversation(id: String) {
        val current = _conversationsFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val conv = current[index]
            current[index] = conv.copy(isPinned = !conv.isPinned)
            val sorted = current.sortedWith(compareByDescending<Conversation> { it.isPinned }.thenByDescending { it.updatedAt })
            persistConversations(sorted)
        }
    }

    fun deleteConversation(id: String) {
        val current = _conversationsFlow.value.filter { it.id != id }
        persistConversations(current)
        // Also clear its messages
        prefs.edit().remove("messages_$id").apply()
    }

    private fun persistConversations(list: List<Conversation>) {
        val arr = JSONArray()
        for (c in list) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("title", c.title)
                put("lastMessage", c.lastMessage)
                put("updatedAt", c.updatedAt)
                put("isPinned", c.isPinned)
                put("messageCount", c.messageCount)
            }
            arr.put(obj)
        }
        prefs.edit().putString("conversations_json", arr.toString()).apply()
        _conversationsFlow.value = list
    }

    fun getMessages(conversationId: String): List<ChatMessage> {
        val raw = prefs.getString("messages_$conversationId", null)
        if (raw == null && conversationId == "default-chat") {
            // Seed welcome message
            val welcomeList = listOf(
                ChatMessage(
                    id = "msg-1",
                    conversationId = "default-chat",
                    role = "assistant",
                    content = "Welcome to Claude S40 for Samsung Galaxy A53! 📱✨\n\nInspired by Emir Karşıyakalı's iconic Nokia 6300 client, this port brings the complete retro Series 40 experience with tactile keypad feedback, paginated reading mode, message actions, and direct Claude AI connectivity.\n\nUse Left Softkey for Options, or start typing below!",
                    timestamp = System.currentTimeMillis() - 120000
                )
            )
            persistMessages(conversationId, welcomeList)
            return welcomeList
        }
        if (raw == null) return emptyList()

        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val sourcesArr = obj.optJSONArray("sources")
                val sources = mutableListOf<String>()
                if (sourcesArr != null) {
                    for (s in 0 until sourcesArr.length()) {
                        sources.add(sourcesArr.getString(s))
                    }
                }
                list.add(
                    ChatMessage(
                        id = obj.getString("id"),
                        conversationId = obj.getString("conversationId"),
                        role = obj.getString("role"),
                        content = obj.getString("content"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        sources = sources,
                        isPinned = obj.optBoolean("isPinned", false),
                        isSaved = obj.optBoolean("isSaved", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveMessage(message: ChatMessage): List<ChatMessage> {
        val existing = getMessages(message.conversationId).toMutableList()
        existing.add(message)
        persistMessages(message.conversationId, existing)

        // Also update conversation snippet
        val convs = _conversationsFlow.value.toMutableList()
        val cIndex = convs.indexOfFirst { it.id == message.conversationId }
        val snippet = if (message.content.length > 40) message.content.take(37) + "..." else message.content
        if (cIndex >= 0) {
            convs[cIndex] = convs[cIndex].copy(
                lastMessage = snippet,
                updatedAt = System.currentTimeMillis(),
                messageCount = existing.size
            )
            persistConversations(convs)
        }
        return existing
    }

    fun updateMessage(message: ChatMessage): List<ChatMessage> {
        val existing = getMessages(message.conversationId).toMutableList()
        val index = existing.indexOfFirst { it.id == message.id }
        if (index >= 0) {
            existing[index] = message
            persistMessages(message.conversationId, existing)
        }
        return existing
    }

    fun clearMessages(conversationId: String) {
        persistMessages(conversationId, emptyList())
    }

    private fun persistMessages(conversationId: String, list: List<ChatMessage>) {
        val arr = JSONArray()
        for (m in list) {
            val obj = JSONObject().apply {
                put("id", m.id)
                put("conversationId", m.conversationId)
                put("role", m.role)
                put("content", m.content)
                put("timestamp", m.timestamp)
                put("isPinned", m.isPinned)
                put("isSaved", m.isSaved)
                val sourcesArr = JSONArray()
                m.sources.forEach { sourcesArr.put(it) }
                put("sources", sourcesArr)
            }
            arr.put(obj)
        }
        prefs.edit().putString("messages_$conversationId", arr.toString()).apply()
    }

    // Saved Text Files (.TXT)
    private fun loadSavedFiles(): List<SavedTextFile> {
        val raw = prefs.getString("saved_files_json", null) ?: return listOf(
            SavedTextFile(
                id = "doc-1",
                title = "About Claude S40",
                content = "Claude S40 was created by Emir Karşıyakalı for Nokia Series 40 phones.\n\nNow running seamlessly on Samsung Galaxy A53 with authentic Nokia soundscapes and full feature set.",
                fileName = "CLAUDE_INFO.TXT",
                timestamp = System.currentTimeMillis() - 86400000
            )
        )
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SavedTextFile>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SavedTextFile(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        content = obj.getString("content"),
                        fileName = obj.getString("fileName"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedByDescending { it.timestamp }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTextFile(title: String, content: String): SavedTextFile {
        val cleanTitle = title.take(12).uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9_]"), "_")
        val fileName = "${cleanTitle.ifBlank { "NOTE" }}_${System.currentTimeMillis() % 10000}.TXT"
        val file = SavedTextFile(
            title = title,
            content = content,
            fileName = fileName,
            timestamp = System.currentTimeMillis()
        )
        val current = _savedFilesFlow.value.toMutableList()
        current.add(0, file)
        persistSavedFiles(current)
        return file
    }

    fun deleteSavedFile(id: String) {
        val current = _savedFilesFlow.value.filter { it.id != id }
        persistSavedFiles(current)
    }

    private fun persistSavedFiles(list: List<SavedTextFile>) {
        val arr = JSONArray()
        for (f in list) {
            val obj = JSONObject().apply {
                put("id", f.id)
                put("title", f.title)
                put("content", f.content)
                put("fileName", f.fileName)
                put("timestamp", f.timestamp)
            }
            arr.put(obj)
        }
        prefs.edit().putString("saved_files_json", arr.toString()).apply()
        _savedFilesFlow.value = list
    }

    // Tasks & Calendar
    private fun loadTasks(): List<S40Task> {
        val raw = prefs.getString("tasks_json", null) ?: return listOf(
            S40Task(
                id = "task-1",
                title = "Try Nokia T9 typing on A53",
                details = "Test the alphanumeric keypad with multitap",
                dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
                isCompleted = false
            ),
            S40Task(
                id = "task-2",
                title = "Configure Claude API key",
                details = "Enter Anthropic API key in Settings (optional)",
                dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()),
                isCompleted = false
            )
        )
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<S40Task>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    S40Task(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        details = obj.optString("details", ""),
                        dateStr = obj.optString("dateStr", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list.sortedBy { it.isCompleted }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun addTask(title: String, details: String = "", dateStr: String = "") {
        val effectiveDate = if (dateStr.isNotBlank()) dateStr else SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val task = S40Task(
            title = title,
            details = details,
            dateStr = effectiveDate
        )
        val current = _tasksFlow.value.toMutableList()
        current.add(0, task)
        persistTasks(current)
    }

    fun toggleTask(id: String) {
        val current = _tasksFlow.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(isCompleted = !item.isCompleted)
            persistTasks(current)
        }
    }

    fun deleteTask(id: String) {
        val current = _tasksFlow.value.filter { it.id != id }
        persistTasks(current)
    }

    private fun persistTasks(list: List<S40Task>) {
        val arr = JSONArray()
        for (t in list) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                put("details", t.details)
                put("dateStr", t.dateStr)
                put("isCompleted", t.isCompleted)
                put("timestamp", t.timestamp)
            }
            arr.put(obj)
        }
        prefs.edit().putString("tasks_json", arr.toString()).apply()
        _tasksFlow.value = list
    }
}
