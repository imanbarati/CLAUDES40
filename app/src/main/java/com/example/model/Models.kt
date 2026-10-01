package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class ApiProvider {
    ANTHROPIC,
    GEMINI,
    CUSTOM_PROXY,
    BUILTIN_SMART
}

enum class GalaxyTheme(val displayName: String, val primaryHex: Long, val bgHex: Long, val isDark: Boolean) {
    AMOLED_BLACK("Galaxy Phantom Black (AMOLED)", 0xFF3B82F6, 0xFF000000, true),
    PHANTOM_BLUE("Galaxy Electric Blue", 0xFF2563EB, 0xFFF8FAFC, false),
    GALAXY_LAVENDER("Galaxy Violet Lavender", 0xFF7C3AED, 0xFFFAF5FF, false),
    MINIMAL_WHITE("Samsung One UI Porcelain", 0xFF0F172A, 0xFFFFFFFF, false),
    GALAXY_EMERALD("Galaxy Forest Emerald", 0xFF059669, 0xFFF0FDF4, false)
}

enum class GalaxyNavTab {
    CHAT,
    CONVERSATIONS,
    AGENTS,
    NOTES_TASKS,
    SETTINGS
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "default-chat",
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isSaved: Boolean = false,
    val agentName: String = "Claude 3.5 Sonnet"
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val messageCount: Int = 0
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            return sdf.format(Date(updatedAt))
        }
}

data class S40Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val details: String = "",
    val dateStr: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class SavedTextFile(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val fileName: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}

data class AppSettings(
    val apiKey: String = "", // Anthropic API Key
    val geminiApiKey: String = "",
    val apiProvider: ApiProvider = ApiProvider.BUILTIN_SMART,
    val modelName: String = "claude-3-5-sonnet-20241022",
    val proxyUrl: String = "http://10.0.2.2:8080",
    val language: String = "en", // "en" or "tr"
    val theme: GalaxyTheme = GalaxyTheme.AMOLED_BLACK,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val webSearchEnabled: Boolean = false,
    val systemNotes: String = ""
)

data class AgentDefinition(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val iconName: String,
    val defaultPrompt: String,
    val category: String
)
