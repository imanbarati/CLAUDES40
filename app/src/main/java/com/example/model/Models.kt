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

enum class S40Theme(val displayName: String, val primaryHex: Long, val bgHex: Long) {
    SAPPHIRE_BLUE("Nokia Sapphire Blue", 0xFF0D47A1, 0xFFE3F2FD),
    RETRO_GREEN("Nokia 3310 LCD Green", 0xFF2E7D32, 0xFFDCEDC8),
    CYBER_SILVER("Nokia 6300 Steel Silver", 0xFF374151, 0xFFF3F4F6),
    MATRIX_DARK("Matrix Hacker Dark", 0xFF00C853, 0xFF121212),
    MONOCHROME_AMBER("Amber Phosphor", 0xFFFF8F00, 0xFF1E1405)
}

enum class S40ViewMode {
    NOKIA_6300_DEVICE,
    FULLSCREEN_S40
}

enum class KeyboardMode {
    T9_KEYPAD,
    SYSTEM_KEYBOARD
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val sources: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val isSaved: Boolean = false
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
            val sdf = SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault())
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
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
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
    val theme: S40Theme = S40Theme.SAPPHIRE_BLUE,
    val viewMode: S40ViewMode = S40ViewMode.NOKIA_6300_DEVICE,
    val keyboardMode: KeyboardMode = KeyboardMode.T9_KEYPAD,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val webSearchEnabled: Boolean = false,
    val readingFontSize: Int = 14,
    val systemNotes: String = ""
)
