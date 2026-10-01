package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.ClaudeApiService
import com.example.audio.NokiaSoundPlayer
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.KeyboardMode
import com.example.model.S40Task
import com.example.model.S40Theme
import com.example.model.S40ViewMode
import com.example.model.SavedTextFile
import com.example.repository.ClaudeS40Repository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class S40Screen {
    CHAT,
    HISTORY,
    SAVED_FILES,
    CALENDAR,
    SETTINGS,
    ABOUT,
    READING_MODE
}

data class UiState(
    val currentScreen: S40Screen = S40Screen.CHAT,
    val activeConversationId: String = "default-chat",
    val activeConversationTitle: String = "Welcome to Claude S40",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isOptionsMenuOpen: Boolean = false,
    val isMessageActionsOpen: Boolean = false,
    val selectedMessageForAction: ChatMessage? = null,
    val readingMessage: ChatMessage? = null,
    val readingPage: Int = 0,
    val totalReadingPages: Int = 1,
    val statusNotice: String? = null,
    val t9ActiveKey: Char? = null,
    val t9CycleIndex: Int = 0,
    val isCaps: Boolean = false,
    val isNumberMode: Boolean = false
)

class ClaudeS40ViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ClaudeS40Repository(application)
    val soundPlayer = NokiaSoundPlayer(application)
    private val apiService = ClaudeApiService()

    val settingsFlow: StateFlow<AppSettings> = repository.settingsFlow
    val conversationsFlow: StateFlow<List<Conversation>> = repository.conversationsFlow
    val savedFilesFlow: StateFlow<List<SavedTextFile>> = repository.savedFilesFlow
    val tasksFlow: StateFlow<List<S40Task>> = repository.tasksFlow

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var t9CommitJob: Job? = null

    // Nokia Multitap Mapping
    private val keyMap = mapOf(
        '1' to listOf('.', ',', '!', '?', '\'', '"', '1', '-', '(', ')', '@', '/', ':'),
        '2' to listOf('a', 'b', 'c', '2'),
        '3' to listOf('d', 'e', 'f', '3'),
        '4' to listOf('g', 'h', 'i', '4'),
        '5' to listOf('j', 'k', 'l', '5'),
        '6' to listOf('m', 'n', 'o', '6'),
        '7' to listOf('p', 'q', 'r', 's', '7'),
        '8' to listOf('t', 'u', 'v', '8'),
        '9' to listOf('w', 'x', 'y', 'z', '9'),
        '0' to listOf(' ', '0')
    )

    init {
        val convId = repository.getActiveConversationId()
        val msgs = repository.getMessages(convId)
        val conv = conversationsFlow.value.find { it.id == convId }
        _uiState.value = _uiState.value.copy(
            activeConversationId = convId,
            activeConversationTitle = conv?.title ?: "Chat with Claude",
            messages = msgs
        )
    }

    fun onLeftSoftKey() {
        val settings = settingsFlow.value
        soundPlayer.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)

        when (_uiState.value.currentScreen) {
            S40Screen.CHAT -> {
                // Toggle Options Menu
                _uiState.value = _uiState.value.copy(
                    isOptionsMenuOpen = !_uiState.value.isOptionsMenuOpen,
                    isMessageActionsOpen = false
                )
            }
            S40Screen.READING_MODE -> {
                // Reading mode: Previous page or Options
                prevReadingPage()
            }
            S40Screen.HISTORY, S40Screen.SAVED_FILES, S40Screen.CALENDAR, S40Screen.SETTINGS, S40Screen.ABOUT -> {
                // Select / Primary action in sub-screens
            }
        }
    }

    fun onRightSoftKey() {
        val settings = settingsFlow.value
        soundPlayer.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)

        if (_uiState.value.isOptionsMenuOpen) {
            _uiState.value = _uiState.value.copy(isOptionsMenuOpen = false)
            return
        }
        if (_uiState.value.isMessageActionsOpen) {
            _uiState.value = _uiState.value.copy(isMessageActionsOpen = false, selectedMessageForAction = null)
            return
        }

        when (_uiState.value.currentScreen) {
            S40Screen.CHAT -> {
                // In chat: if there is input, clear one char, else exit options
                if (_uiState.value.inputText.isNotEmpty()) {
                    onKeypadBackspace()
                }
            }
            S40Screen.READING_MODE -> {
                _uiState.value = _uiState.value.copy(
                    currentScreen = S40Screen.CHAT,
                    readingMessage = null
                )
            }
            else -> {
                // Navigate back to Chat screen
                _uiState.value = _uiState.value.copy(currentScreen = S40Screen.CHAT)
            }
        }
    }

    fun onCenterNaviKey() {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)

        if (_uiState.value.isOptionsMenuOpen) {
            return
        }

        when (_uiState.value.currentScreen) {
            S40Screen.CHAT -> {
                // Send current message
                if (_uiState.value.inputText.isNotBlank()) {
                    commitPendingT9()
                    sendCurrentMessage()
                }
            }
            S40Screen.READING_MODE -> {
                nextReadingPage()
            }
            else -> {}
        }
    }

    // T9 Multitap Input Handling
    fun onT9KeyPress(digit: Char) {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)

        if (_uiState.value.isNumberMode) {
            _uiState.value = _uiState.value.copy(inputText = _uiState.value.inputText + digit)
            return
        }

        val chars = keyMap[digit] ?: return
        val currentActiveKey = _uiState.value.t9ActiveKey
        val currentIndex = _uiState.value.t9CycleIndex

        if (currentActiveKey == digit) {
            // Cycle through letters of the same key
            val nextIndex = (currentIndex + 1) % chars.size
            var chosenChar = chars[nextIndex]
            if (_uiState.value.isCaps && chosenChar.isLetter()) {
                chosenChar = chosenChar.uppercaseChar()
            }
            val base = if (_uiState.value.inputText.isNotEmpty()) _uiState.value.inputText.dropLast(1) else ""
            _uiState.value = _uiState.value.copy(
                inputText = base + chosenChar,
                t9CycleIndex = nextIndex
            )
            resetT9Timer()
        } else {
            // Commit previous character and start fresh cycle on new key
            commitPendingT9()
            var chosenChar = chars[0]
            if (_uiState.value.isCaps && chosenChar.isLetter()) {
                chosenChar = chosenChar.uppercaseChar()
            }
            _uiState.value = _uiState.value.copy(
                inputText = _uiState.value.inputText + chosenChar,
                t9ActiveKey = digit,
                t9CycleIndex = 0
            )
            resetT9Timer()
        }
    }

    private fun resetT9Timer() {
        t9CommitJob?.cancel()
        t9CommitJob = viewModelScope.launch {
            delay(850)
            commitPendingT9()
        }
    }

    fun commitPendingT9() {
        t9CommitJob?.cancel()
        _uiState.value = _uiState.value.copy(t9ActiveKey = null, t9CycleIndex = 0)
    }

    fun onKeypadBackspace() {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        commitPendingT9()
        if (_uiState.value.inputText.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(inputText = _uiState.value.inputText.dropLast(1))
        }
    }

    fun onKeypadSpace() {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        commitPendingT9()
        _uiState.value = _uiState.value.copy(inputText = _uiState.value.inputText + " ")
    }

    fun onToggleCaps() {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        commitPendingT9()
        _uiState.value = _uiState.value.copy(isCaps = !_uiState.value.isCaps)
    }

    fun onToggleNumberMode() {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        commitPendingT9()
        _uiState.value = _uiState.value.copy(isNumberMode = !_uiState.value.isNumberMode)
    }

    fun onInputTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun sendCurrentMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        val settings = settingsFlow.value
        soundPlayer.playSendTone(settings.soundEnabled, settings.vibrateEnabled)

        val convId = _uiState.value.activeConversationId
        val userMsg = ChatMessage(
            conversationId = convId,
            role = "user",
            content = text
        )

        val updated = repository.saveMessage(userMsg)
        _uiState.value = _uiState.value.copy(
            inputText = "",
            messages = updated,
            isLoading = true
        )

        viewModelScope.launch {
            val result = apiService.sendMessage(
                history = updated,
                userPrompt = text,
                settings = settings
            )

            val assistantMsg = ChatMessage(
                conversationId = convId,
                role = "assistant",
                content = result.replyText,
                sources = result.sources
            )

            val finalList = repository.saveMessage(assistantMsg)
            _uiState.value = _uiState.value.copy(
                messages = finalList,
                isLoading = false
            )

            soundPlayer.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
        }
    }

    fun applyMessageAction(actionType: String, message: ChatMessage) {
        val settings = settingsFlow.value
        soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(isMessageActionsOpen = false, selectedMessageForAction = null)

        when (actionType) {
            "READING_MODE" -> {
                openReadingMode(message)
            }
            "SAVE_TXT" -> {
                val saved = repository.saveTextFile(
                    title = "CLAUDE_${System.currentTimeMillis() % 1000}",
                    content = message.content
                )
                repository.updateMessage(message.copy(isSaved = true))
                val msgs = repository.getMessages(_uiState.value.activeConversationId)
                _uiState.value = _uiState.value.copy(
                    messages = msgs,
                    statusNotice = "Saved to ${saved.fileName} on Memory Card!"
                )
                soundPlayer.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
            }
            "ADD_TODO" -> {
                repository.addTask(
                    title = message.content.take(30) + "...",
                    details = message.content
                )
                _uiState.value = _uiState.value.copy(statusNotice = "Added to S40 To-Do List!")
                soundPlayer.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
            }
            "PIN" -> {
                val updated = message.copy(isPinned = !message.isPinned)
                repository.updateMessage(updated)
                val msgs = repository.getMessages(_uiState.value.activeConversationId)
                _uiState.value = _uiState.value.copy(messages = msgs)
            }
            "SHORTEN", "SIMPLIFY", "TRANSLATE_TR", "TRANSLATE_EN" -> {
                // Request Claude to transform the message
                _uiState.value = _uiState.value.copy(isLoading = true)
                viewModelScope.launch {
                    val result = apiService.sendMessage(
                        history = _uiState.value.messages,
                        userPrompt = message.content,
                        settings = settings,
                        actionType = actionType
                    )
                    val assistantMsg = ChatMessage(
                        conversationId = _uiState.value.activeConversationId,
                        role = "assistant",
                        content = result.replyText,
                        sources = result.sources
                    )
                    val finalList = repository.saveMessage(assistantMsg)
                    _uiState.value = _uiState.value.copy(
                        messages = finalList,
                        isLoading = false
                    )
                    soundPlayer.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
                }
            }
        }
    }

    fun openReadingMode(message: ChatMessage) {
        // Chunk into 400 character pages for authentic 240x320 S40 pagination
        val pageSize = 400
        val totalPages = maxOf(1, (message.content.length + pageSize - 1) / pageSize)
        _uiState.value = _uiState.value.copy(
            currentScreen = S40Screen.READING_MODE,
            readingMessage = message,
            readingPage = 0,
            totalReadingPages = totalPages,
            isOptionsMenuOpen = false,
            isMessageActionsOpen = false
        )
    }

    fun nextReadingPage() {
        val current = _uiState.value.readingPage
        val maxPage = _uiState.value.totalReadingPages - 1
        if (current < maxPage) {
            val settings = settingsFlow.value
            soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
            _uiState.value = _uiState.value.copy(readingPage = current + 1)
        }
    }

    fun prevReadingPage() {
        val current = _uiState.value.readingPage
        if (current > 0) {
            val settings = settingsFlow.value
            soundPlayer.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
            _uiState.value = _uiState.value.copy(readingPage = current - 1)
        } else {
            // Exit reading mode back to chat
            _uiState.value = _uiState.value.copy(currentScreen = S40Screen.CHAT, readingMessage = null)
        }
    }

    fun selectMessageForActions(msg: ChatMessage) {
        val settings = settingsFlow.value
        soundPlayer.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            selectedMessageForAction = msg,
            isMessageActionsOpen = true,
            isOptionsMenuOpen = false
        )
    }

    fun navigateTo(screen: S40Screen) {
        val settings = settingsFlow.value
        soundPlayer.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            currentScreen = screen,
            isOptionsMenuOpen = false,
            isMessageActionsOpen = false
        )
    }

    fun createNewChat() {
        val newConv = Conversation(
            id = UUID.randomUUID().toString(),
            title = "Chat #${conversationsFlow.value.size + 1}",
            lastMessage = "Started new chat",
            updatedAt = System.currentTimeMillis()
        )
        repository.saveConversation(newConv)
        switchConversation(newConv.id)
    }

    fun switchConversation(id: String) {
        val conv = conversationsFlow.value.find { it.id == id }
        val msgs = repository.getMessages(id)
        _uiState.value = _uiState.value.copy(
            currentScreen = S40Screen.CHAT,
            activeConversationId = id,
            activeConversationTitle = conv?.title ?: "Chat",
            messages = msgs,
            isOptionsMenuOpen = false
        )
    }

    fun deleteConversation(id: String) {
        repository.deleteConversation(id)
        if (_uiState.value.activeConversationId == id) {
            val nextConvId = repository.getActiveConversationId()
            switchConversation(nextConvId)
        }
    }

    fun toggleWebSearch() {
        val current = settingsFlow.value
        val updated = current.copy(webSearchEnabled = !current.webSearchEnabled)
        repository.saveSettings(updated)
        soundPlayer.playKeyClick(updated.soundEnabled, updated.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            statusNotice = if (updated.webSearchEnabled) "Web Search: ON [Sources enabled]" else "Web Search: OFF"
        )
    }

    fun toggleViewMode() {
        val current = settingsFlow.value
        val newMode = if (current.viewMode == S40ViewMode.NOKIA_6300_DEVICE) S40ViewMode.FULLSCREEN_S40 else S40ViewMode.NOKIA_6300_DEVICE
        val updated = current.copy(viewMode = newMode)
        repository.saveSettings(updated)
        soundPlayer.playSoftKeyBeep(updated.soundEnabled, updated.vibrateEnabled)
    }

    fun toggleKeyboardMode() {
        val current = settingsFlow.value
        val newMode = if (current.keyboardMode == KeyboardMode.T9_KEYPAD) KeyboardMode.SYSTEM_KEYBOARD else KeyboardMode.T9_KEYPAD
        val updated = current.copy(keyboardMode = newMode)
        repository.saveSettings(updated)
        soundPlayer.playSoftKeyBeep(updated.soundEnabled, updated.vibrateEnabled)
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.saveSettings(newSettings)
    }

    fun dismissNotice() {
        _uiState.value = _uiState.value.copy(statusNotice = null)
    }

    fun closeDialogs() {
        _uiState.value = _uiState.value.copy(
            isOptionsMenuOpen = false,
            isMessageActionsOpen = false,
            selectedMessageForAction = null
        )
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }
}
