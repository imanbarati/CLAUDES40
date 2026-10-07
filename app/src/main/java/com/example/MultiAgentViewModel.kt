package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.AgenticTaskRequest
import com.example.api.AgenticTaskType
import com.example.api.ClaudeApiService
import com.example.api.NvidiaHardwareNodeService
import com.example.audio.AudioCaptureHelper
import com.example.audio.AudioCaptureState
import com.example.audio.AudioRecordHelper
import com.example.audio.NokiaSoundPlayer
import com.example.llm.LocalGgufModelLoader
import com.example.model.AgentDefinition
import com.example.model.ApiProvider
import com.example.model.AppSettings
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.GalaxyNavTab
import com.example.model.GalaxyTheme
import com.example.model.S40Task
import com.example.model.SavedTextFile
import com.example.repository.ClaudeS40Repository
import com.example.repository.MessageRepository
import com.example.speech.OneUiSpeechRecognizer
import com.example.speech.SpeechState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AgentInteractionState(
    val id: String,
    val name: String,
    val tag: String,
    val description: String,
    val placeholder: String,
    val accentColorHex: Long,
    val suggestionChips: List<String>,
    val defaultPrompt: String
)

data class MultiAgentUiState(
    val currentTab: GalaxyNavTab = GalaxyNavTab.CHAT,
    val activeConversationId: String = "default-chat",
    val activeConversationTitle: String = "Chat with Claude",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isListening: Boolean = false,
    val audioAmplitude: Float = 0f,
    val isVoiceDetected: Boolean = false,
    val speechTranscriptionNotice: String? = null,
    val selectedMessageForAction: ChatMessage? = null,
    val isActionSheetOpen: Boolean = false,
    val isAgentBottomSheetOpen: Boolean = false,
    val statusNotice: String? = null,
    val selectedAgentId: String = "claude_core",
    val activeAgent: String = "Claude 3.5 Sonnet",
    val smartReplies: List<String> = emptyList(),
    val currentAgentState: AgentInteractionState = AgentInteractionState(
        id = "claude_core",
        name = "Claude 3.5 Sonnet",
        tag = "Core AI",
        description = "Anthropic's flagship intelligence model for in-depth reasoning, coding, and writing.",
        placeholder = "Ask Claude anything...",
        accentColorHex = 0xFF7B61FF,
        suggestionChips = listOf("Explain Quantum Computing", "Review Kotlin Coroutines", "Draft an Email"),
        defaultPrompt = "Help me analyze and plan a modern Kotlin Android project structure."
    )
)

open class MultiAgentViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences & System Settings Repository
    val repository = ClaudeS40Repository(application)

    // Room Database Repository for Chat History & Conversations Persistence
    val messageRepository = MessageRepository(application)

    // Samsung One UI Feedback & Audio Manager
    val feedbackManager = NokiaSoundPlayer(application)

    // AudioCaptureHelper using Android AudioRecord API for low-level audio capture & amplitude analysis
    val audioCaptureHelper = AudioCaptureHelper(application)
    val audioRecordHelper = audioCaptureHelper

    // On-device private GGUF model loader using llama.cpp Android port (via JNI)
    val localGgufModelLoader = LocalGgufModelLoader(application)

    // Remote NVIDIA hardware node service for offloading heavy agentic tasks via REST API
    val nvidiaNodeService = NvidiaHardwareNodeService()

    // SpeechRecognizer for Dictation
    val speechRecognizer = OneUiSpeechRecognizer(application)
    val speechState: StateFlow<SpeechState> = speechRecognizer.speechState

    private val apiService = ClaudeApiService()

    val settingsFlow: StateFlow<AppSettings> = repository.settingsFlow
    val savedFilesFlow: StateFlow<List<SavedTextFile>> = repository.savedFilesFlow
    val tasksFlow: StateFlow<List<S40Task>> = repository.tasksFlow

    // Room-backed reactive conversation stream
    val conversationsFlow: StateFlow<List<Conversation>> = messageRepository.conversationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val agentCatalog: List<AgentInteractionState> = listOf(
        AgentInteractionState(
            id = "claude_core",
            name = "Claude 3.5 Sonnet",
            tag = "Core AI",
            description = "Anthropic's flagship intelligence model for in-depth reasoning, coding, and writing.",
            placeholder = "Ask Claude anything...",
            accentColorHex = 0xFF7B61FF,
            suggestionChips = listOf("Explain Quantum Computing", "Write a Kotlin Compose UI", "Architecture Advice"),
            defaultPrompt = "Help me analyze and plan a modern Kotlin Android project structure."
        ),
        AgentInteractionState(
            id = "web_search",
            name = "Web Searcher",
            tag = "Real-time",
            description = "Searches the live web for breaking news, live documentation, and cited sources.",
            placeholder = "Search live web with Web Searcher...",
            accentColorHex = 0xFF10B981,
            suggestionChips = listOf("Latest Android 15 features", "Galaxy A53 Specs", "Tech headlines today"),
            defaultPrompt = "What are the latest updates in Android Jetpack Compose and AI models?"
        ),
        AgentInteractionState(
            id = "summarizer",
            name = "Executive Summarizer",
            tag = "Productivity",
            description = "Condenses long articles, research papers, and documents into executive bullet points.",
            placeholder = "Enter or paste text to summarize...",
            accentColorHex = 0xFFF59E0B,
            suggestionChips = listOf("Summarize clean architecture", "3-bullet takeaway", "Meeting notes summary"),
            defaultPrompt = "Summarize the key architectural benefits of Clean Architecture with MVVM."
        ),
        AgentInteractionState(
            id = "eli5",
            name = "Simplifier & ELI5",
            tag = "Explainer",
            description = "Explains complex algorithms, financial concepts, and science in plain intuitive terms.",
            placeholder = "Ask for an ELI5 simplified explanation...",
            accentColorHex = 0xFF06B6D4,
            suggestionChips = listOf("Explain APIs like I'm 5", "How do neural nets work?", "What is an AMOLED screen?"),
            defaultPrompt = "Explain how transformer neural networks and attention mechanisms work like I'm 5."
        ),
        AgentInteractionState(
            id = "translator",
            name = "Polyglot Translator",
            tag = "Languages",
            description = "Flawless multi-language translation preserving tone, idiom, and technical terms.",
            placeholder = "Enter message to translate...",
            accentColorHex = 0xFFF43F5E,
            suggestionChips = listOf("Translate to Turkish", "Translate to English", "Translate to Spanish"),
            defaultPrompt = "Translate the following software update notice into fluent Turkish and Spanish."
        ),
        AgentInteractionState(
            id = "task_planner",
            name = "Task Manager",
            tag = "Organization",
            description = "Extracts deadlines, todo items, and scheduling memos directly into your task list.",
            placeholder = "Describe tasks or events to plan...",
            accentColorHex = 0xFF6366F1,
            suggestionChips = listOf("Plan weekly workout", "Study schedule", "Project release checklist"),
            defaultPrompt = "Create a structured daily checklist for mastering Jetpack Compose and Coroutines."
        ),
        AgentInteractionState(
            id = "local_gguf_host",
            name = "Local GGUF / Device AI",
            tag = "Open Weights",
            description = "Connects to your local LAN GPU (Ollama, llama.cpp, NVIDIA NIM, GhostBrain, PirateFace open models) without cloud fees or data tracking.",
            placeholder = "Ask your local open-source model...",
            accentColorHex = 0xFF10B981,
            suggestionChips = listOf("Run Llama 3.2 3B Test", "DeepSeek-R1 Local Reasoning", "GhostBrain Server Ping"),
            defaultPrompt = "Analyze this system architecture using local GPU inference power."
        )
    )

    val availableAgents: List<AgentDefinition> = agentCatalog.map {
        AgentDefinition(
            id = it.id,
            name = it.name,
            tag = it.tag,
            description = it.description,
            iconName = when (it.id) {
                "web_search" -> "Search"
                "summarizer" -> "Compress"
                "eli5" -> "Lightbulb"
                "translator" -> "Language"
                "task_planner" -> "DateRange"
                "local_gguf_host" -> "Memory"
                else -> "AutoAwesome"
            },
            defaultPrompt = it.defaultPrompt,
            category = it.tag
        )
    }

    private val _uiState = MutableStateFlow(MultiAgentUiState())
    val uiState: StateFlow<MultiAgentUiState> = _uiState.asStateFlow()

    private var messagesObservationJob: Job? = null

    init {
        val initialConvId = repository.getActiveConversationId()
        observeConversationMessages(initialConvId)

        // Observe real-time audio capture state & voice activity from AudioRecord API
        viewModelScope.launch {
            audioCaptureHelper.captureState.collect { state ->
                if (_uiState.value.isListening && state is AudioCaptureState.Capturing) {
                    _uiState.value = _uiState.value.copy(
                        audioAmplitude = state.amplitude,
                        isVoiceDetected = state.isVoiceDetected
                    )
                }
            }
        }

        // Observe speech recognition state for UI feedback
        viewModelScope.launch {
            speechState.collect { state ->
                when (state) {
                    is SpeechState.Listening -> {
                        _uiState.value = _uiState.value.copy(
                            isListening = true,
                            speechTranscriptionNotice = "Listening for ${_uiState.value.activeAgent}..."
                        )
                    }
                    is SpeechState.PartialResult -> {
                        _uiState.value = _uiState.value.copy(
                            speechTranscriptionNotice = "Recognized: \"${state.text}\""
                        )
                    }
                    is SpeechState.Result -> {
                        appendSpeechText(state.text)
                        audioCaptureHelper.stopRecording()
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            audioAmplitude = 0f,
                            speechTranscriptionNotice = null
                        )
                    }
                    is SpeechState.Error -> {
                        audioCaptureHelper.stopRecording()
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            audioAmplitude = 0f,
                            speechTranscriptionNotice = null,
                            statusNotice = state.message
                        )
                    }
                    is SpeechState.Idle -> {
                        if (!_uiState.value.isListening) {
                            audioCaptureHelper.stopRecording()
                            _uiState.value = _uiState.value.copy(
                                audioAmplitude = 0f,
                                speechTranscriptionNotice = null
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeConversationMessages(convId: String) {
        messagesObservationJob?.cancel()
        messagesObservationJob = viewModelScope.launch {
            // First load sync to prevent flash of empty content
            val current = messageRepository.getMessagesSync(convId)
            val initialReplies = com.example.util.SmartReplyEngine.generateSuggestions(
                current.lastOrNull { it.role == "assistant" },
                settingsFlow.value.language
            )
            _uiState.value = _uiState.value.copy(
                activeConversationId = convId,
                messages = current,
                smartReplies = initialReplies
            )

            // Then observe Flow updates reactively from Room
            messageRepository.getMessagesFlow(convId).collect { msgList ->
                val replies = com.example.util.SmartReplyEngine.generateSuggestions(
                    msgList.lastOrNull { it.role == "assistant" },
                    settingsFlow.value.language
                )
                _uiState.value = _uiState.value.copy(
                    messages = msgList,
                    smartReplies = replies
                )
            }
        }
    }

    fun onSmartReplyTapped(suggestion: String) {
        val settings = settingsFlow.value
        feedbackManager.vibrateAgentToggle(settings.vibrateEnabled)
        sendMessage(suggestion)
    }

    fun switchTab(tab: GalaxyNavTab) {
        val settings = settingsFlow.value
        feedbackManager.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            currentTab = tab,
            isActionSheetOpen = false,
            isAgentBottomSheetOpen = false
        )
    }

    fun selectAgent(agentId: String) {
        val agent = agentCatalog.find { it.id == agentId } ?: agentCatalog.first()
        val settings = settingsFlow.value
        feedbackManager.vibrateAgentToggle(settings.vibrateEnabled)
        feedbackManager.playKeyClick(settings.soundEnabled, false)

        if (agentId == "web_search" && !settings.webSearchEnabled) {
            repository.saveSettings(settings.copy(webSearchEnabled = true))
        }

        _uiState.value = _uiState.value.copy(
            selectedAgentId = agentId,
            activeAgent = agent.name,
            currentAgentState = agent,
            statusNotice = "Active Agent: ${agent.name}"
        )
    }

    fun openAgentBottomSheet() {
        val settings = settingsFlow.value
        feedbackManager.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(isAgentBottomSheetOpen = true)
    }

    fun closeAgentBottomSheet() {
        _uiState.value = _uiState.value.copy(isAgentBottomSheetOpen = false)
    }

    fun onInputTextChange(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun selectAgentPrompt(agent: AgentDefinition) {
        selectAgent(agent.id)
        _uiState.value = _uiState.value.copy(
            currentTab = GalaxyNavTab.CHAT,
            inputText = agent.defaultPrompt
        )
    }

    fun toggleWebSearch() {
        val current = settingsFlow.value
        val updated = current.copy(webSearchEnabled = !current.webSearchEnabled)
        repository.saveSettings(updated)
        feedbackManager.playKeyClick(updated.soundEnabled, updated.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            statusNotice = if (updated.webSearchEnabled) "Web Search: ON [Sources will be cited]" else "Web Search: OFF"
        )
    }

    /**
     * Speech Recognition Control
     */
    fun toggleSpeechRecognition() {
        val settings = settingsFlow.value
        val isStarting = !_uiState.value.isListening
        feedbackManager.vibrateSpeechToggle(isStarting, settings.vibrateEnabled)
        feedbackManager.playKeyClick(settings.soundEnabled, false)

        if (_uiState.value.isListening) {
            audioCaptureHelper.stop()
            speechRecognizer.stopListening()
            _uiState.value = _uiState.value.copy(
                isListening = false,
                audioAmplitude = 0f,
                isVoiceDetected = false,
                speechTranscriptionNotice = null
            )
        } else {
            // Start audio capture using AudioCaptureHelper & AudioRecord API
            audioCaptureHelper.start()
            // Start speech recognizer for voice-to-text transcription
            speechRecognizer.startListening(languageCode = settings.language) { text ->
                appendSpeechText(text)
            }
            _uiState.value = _uiState.value.copy(
                isListening = true,
                speechTranscriptionNotice = "Listening with AudioRecord for ${_uiState.value.activeAgent}..."
            )
        }
    }

    private fun appendSpeechText(spokenText: String) {
        val current = _uiState.value.inputText.trim()
        val combined = if (current.isBlank()) spokenText else "$current $spokenText"
        _uiState.value = _uiState.value.copy(inputText = combined)
    }

    /**
     * Dispatches message with agent-specific prompt transformation and Room persistence
     */
    fun sendMessage(customPrompt: String? = null) {
        val text = (customPrompt ?: _uiState.value.inputText).trim()
        if (text.isBlank() || _uiState.value.isLoading) return

        val settings = settingsFlow.value
        feedbackManager.playSendTone(settings.soundEnabled, settings.vibrateEnabled)

        val convId = _uiState.value.activeConversationId
        val userMsg = ChatMessage(
            conversationId = convId,
            role = "user",
            content = text
        )

        _uiState.value = _uiState.value.copy(
            inputText = "",
            isLoading = true
        )

        viewModelScope.launch {
            // Persist user message to Room DB
            val historyAfterUser = messageRepository.saveMessage(userMsg)

            // Agent-specific action logic
            val currentAgentId = _uiState.value.selectedAgentId
            val actionType = when (currentAgentId) {
                "summarizer" -> "SHORTEN"
                "eli5" -> "SIMPLIFY"
                "translator" -> if (settings.language == "tr") "TRANSLATE_EN" else "TRANSLATE_TR"
                "task_planner" -> "TODO"
                else -> null
            }

            val replyText: String
            val sources: List<String>
            val agentName: String

            if (currentAgentId == "local_gguf_host" && localGgufModelLoader.isLoaded.value) {
                replyText = localGgufModelLoader.generateSync(text)
                sources = listOf("On-Device GGUF • llama.cpp JNI Private Inference")
                agentName = localGgufModelLoader.activeModelName.value ?: "Local GGUF Model"
            } else {
                val effectiveSettings = when (currentAgentId) {
                    "web_search" -> settings.copy(webSearchEnabled = true)
                    "local_gguf_host" -> settings.copy(apiProvider = ApiProvider.LOCAL_NETWORK, enableLocalGgufPower = true)
                    else -> settings
                }

                val result = apiService.sendMessage(
                    history = historyAfterUser,
                    userPrompt = text,
                    settings = effectiveSettings,
                    actionType = actionType
                )
                replyText = result.replyText
                sources = result.sources
                agentName = _uiState.value.activeAgent
            }

            val assistantMsg = ChatMessage(
                conversationId = convId,
                role = "assistant",
                content = replyText,
                sources = sources,
                agentName = agentName
            )

            // Persist assistant message to Room DB
            messageRepository.saveMessage(assistantMsg)

            _uiState.value = _uiState.value.copy(isLoading = false)
            feedbackManager.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
        }
    }

    fun offloadAgenticTaskToNvidia(taskType: AgenticTaskType, prompt: String) {
        val settings = settingsFlow.value
        val convId = _uiState.value.activeConversationId
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            statusNotice = "Dispatching to Remote NVIDIA Node (${taskType.displayName})..."
        )

        viewModelScope.launch {
            val userMsg = ChatMessage(
                conversationId = convId,
                role = "user",
                content = "⚡ [NVIDIA Node Offload]: ${taskType.displayName}\nPrompt: $prompt"
            )
            messageRepository.saveMessage(userMsg)

            val request = AgenticTaskRequest(
                taskType = taskType,
                prompt = prompt,
                targetModel = settings.localModelName.ifBlank { NvidiaHardwareNodeService.DEFAULT_MODEL },
                customSystemNotes = settings.systemNotes
            )

            val result = nvidiaNodeService.offloadAgenticTask(
                request = request,
                nodeUrl = settings.localEndpointUrl
            )

            val assistantMsg = ChatMessage(
                conversationId = convId,
                role = "assistant",
                content = result.finalOutput,
                sources = result.intermediateSteps,
                agentName = result.gpuHardwareNode
            )
            messageRepository.saveMessage(assistantMsg)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                statusNotice = if (result.isOffloadedSuccessfully) "Completed on NVIDIA Tensor Cores" else "Fallback processed"
            )
            feedbackManager.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
        }
    }

    fun applyMessageAction(actionType: String, message: ChatMessage) {
        val settings = settingsFlow.value
        feedbackManager.playKeyClick(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(isActionSheetOpen = false, selectedMessageForAction = null)

        when (actionType) {
            "NVIDIA_DEEP_RESEARCH" -> {
                offloadAgenticTaskToNvidia(AgenticTaskType.DEEP_RESEARCH, message.content)
            }
            "NVIDIA_CODE_AUDIT" -> {
                offloadAgenticTaskToNvidia(AgenticTaskType.CODE_ANALYSIS, message.content)
            }
            "NVIDIA_CONSENSUS" -> {
                offloadAgenticTaskToNvidia(AgenticTaskType.MULTI_AGENT_CONSENSUS, message.content)
            }
            "SAVE_NOTE" -> {
                val saved = repository.saveTextFile(
                    title = "CLAUDE_${System.currentTimeMillis() % 1000}",
                    content = message.content
                )
                viewModelScope.launch {
                    messageRepository.updateMessage(message.copy(isSaved = true))
                }
                _uiState.value = _uiState.value.copy(
                    statusNotice = "Saved to Notes (${saved.fileName})"
                )
                feedbackManager.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
            }
            "ADD_TASK" -> {
                repository.addTask(
                    title = message.content.take(35) + "...",
                    details = message.content
                )
                _uiState.value = _uiState.value.copy(statusNotice = "Added to Galaxy Tasks & Calendar!")
                feedbackManager.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
            }
            "PIN" -> {
                viewModelScope.launch {
                    messageRepository.updateMessage(message.copy(isPinned = !message.isPinned))
                }
            }
            "SHORTEN", "SIMPLIFY", "TRANSLATE_TR", "TRANSLATE_EN" -> {
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
                        sources = result.sources,
                        agentName = when (actionType) {
                            "SHORTEN" -> "Executive Summarizer"
                            "SIMPLIFY" -> "Simplifier & ELI5"
                            "TRANSLATE_TR", "TRANSLATE_EN" -> "Polyglot Translator"
                            else -> "Claude 3.5 Sonnet"
                        }
                    )
                    messageRepository.saveMessage(assistantMsg)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    feedbackManager.playMessageReceivedSms(settings.soundEnabled, settings.vibrateEnabled)
                }
            }
        }
    }

    fun openMessageActionSheet(message: ChatMessage) {
        val settings = settingsFlow.value
        feedbackManager.playSoftKeyBeep(settings.soundEnabled, settings.vibrateEnabled)
        _uiState.value = _uiState.value.copy(
            selectedMessageForAction = message,
            isActionSheetOpen = true
        )
    }

    fun closeActionSheet() {
        _uiState.value = _uiState.value.copy(
            isActionSheetOpen = false,
            selectedMessageForAction = null
        )
    }

    fun createNewChat() {
        val newConv = Conversation(
            id = UUID.randomUUID().toString(),
            title = "Chat #${conversationsFlow.value.size + 1}",
            lastMessage = "Started new chat",
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            messageRepository.saveConversation(newConv)
            switchConversation(newConv.id)
        }
    }

    fun switchConversation(id: String) {
        repository.saveActiveConversationId(id)
        val conv = conversationsFlow.value.find { it.id == id }
        _uiState.value = _uiState.value.copy(
            currentTab = GalaxyNavTab.CHAT,
            activeConversationId = id,
            activeConversationTitle = conv?.title ?: "Chat with Claude",
            isActionSheetOpen = false,
            isAgentBottomSheetOpen = false
        )
        observeConversationMessages(id)
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            messageRepository.deleteConversation(id)
            if (_uiState.value.activeConversationId == id) {
                val nextList = messageRepository.getConversationsSync()
                val nextConvId = nextList.firstOrNull()?.id ?: "default-chat"
                switchConversation(nextConvId)
            }
        }
    }

    fun togglePinConversation(id: String) {
        viewModelScope.launch {
            messageRepository.togglePinConversation(id)
        }
    }

    fun updateSettings(newSettings: AppSettings) {
        repository.saveSettings(newSettings)
    }

    fun dismissNotice() {
        _uiState.value = _uiState.value.copy(statusNotice = null)
    }

    override fun onCleared() {
        super.onCleared()
        localGgufModelLoader.unloadModel()
        audioCaptureHelper.release()
        speechRecognizer.destroy()
        feedbackManager.release()
    }
}
