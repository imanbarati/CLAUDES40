package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ClaudeS40ViewModel
import com.example.model.GalaxyNavTab
import com.example.ui.components.OneUiActionSheet
import com.example.ui.components.OneUiAgentSwitcherBottomSheet
import com.example.ui.components.OneUiBottomNav
import com.example.ui.components.OneUiHeader
import com.example.ui.components.shareChatMessage
import com.example.ui.screens.GalaxyAgentsScreen
import com.example.ui.screens.GalaxyChatScreen
import com.example.ui.screens.GalaxyConversationsScreen
import com.example.ui.screens.GalaxyNotesTasksScreen
import com.example.ui.screens.GalaxySettingsScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalaxyMainContainer(
    viewModel: ClaudeS40ViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val conversations by viewModel.conversationsFlow.collectAsState()
    val savedFiles by viewModel.savedFilesFlow.collectAsState()
    val tasks by viewModel.tasksFlow.collectAsState()

    // Handle Back button: close sheets or return to CHAT tab
    BackHandler {
        if (uiState.isActionSheetOpen) {
            viewModel.closeActionSheet()
        } else if (uiState.isAgentBottomSheetOpen) {
            viewModel.closeAgentBottomSheet()
        } else if (uiState.currentTab != GalaxyNavTab.CHAT) {
            viewModel.switchTab(GalaxyNavTab.CHAT)
        }
    }

    val screenBg = Color(settings.theme.bgHex)
    val isImeVisible = WindowInsets.isImeVisible

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = screenBg,
        bottomBar = {
            if (!isImeVisible || uiState.currentTab != GalaxyNavTab.CHAT) {
                OneUiBottomNav(
                    currentTab = uiState.currentTab,
                    theme = settings.theme,
                    onTabSelected = { tab -> viewModel.switchTab(tab) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // One UI Large Viewing Area Header
            OneUiHeader(
                title = when (uiState.currentTab) {
                    GalaxyNavTab.CHAT -> "Claude Galaxy"
                    GalaxyNavTab.CONVERSATIONS -> "Conversations"
                    GalaxyNavTab.AGENTS -> "AI Agents Hub"
                    GalaxyNavTab.NOTES_TASKS -> "Notes & Tasks"
                    GalaxyNavTab.SETTINGS -> "Settings"
                },
                subtitle = when (uiState.currentTab) {
                    GalaxyNavTab.CHAT -> "${uiState.activeAgent} • ${uiState.activeConversationTitle}"
                    GalaxyNavTab.CONVERSATIONS -> "${conversations.size} active sessions"
                    GalaxyNavTab.AGENTS -> "6 specialized AI models"
                    GalaxyNavTab.NOTES_TASKS -> "${savedFiles.size} notes • ${tasks.count { !it.isCompleted }} tasks"
                    GalaxyNavTab.SETTINGS -> settings.theme.displayName
                },
                theme = settings.theme,
                webSearchActive = settings.webSearchEnabled || uiState.selectedAgentId == "web_search",
                onNewChatClick = if (uiState.currentTab == GalaxyNavTab.CHAT || uiState.currentTab == GalaxyNavTab.CONVERSATIONS) {
                    { viewModel.createNewChat() }
                } else null,
                onToggleWebSearch = if (uiState.currentTab == GalaxyNavTab.CHAT) {
                    { viewModel.toggleWebSearch() }
                } else null
            )

            // Main Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (uiState.currentTab) {
                    GalaxyNavTab.CHAT -> {
                        GalaxyChatScreen(
                            messages = uiState.messages,
                            inputText = uiState.inputText,
                            isLoading = uiState.isLoading,
                            isListening = uiState.isListening,
                            speechTranscriptionNotice = uiState.speechTranscriptionNotice,
                            smartReplies = uiState.smartReplies,
                            webSearchEnabled = settings.webSearchEnabled,
                            selectedAgentId = uiState.selectedAgentId,
                            activeAgentName = uiState.activeAgent,
                            theme = settings.theme,
                            statusNotice = uiState.statusNotice,
                            onInputTextChange = { viewModel.onInputTextChange(it) },
                            onSendMessage = { prompt -> viewModel.sendMessage(prompt) },
                            onSmartReplySelected = { reply -> viewModel.onSmartReplyTapped(reply) },
                            onMessageClick = { msg -> viewModel.openMessageActionSheet(msg) },
                            onToggleWebSearch = { viewModel.toggleWebSearch() },
                            onSelectAgent = { agentId -> viewModel.selectAgent(agentId) },
                            onOpenAgentSheet = { viewModel.openAgentBottomSheet() },
                            onToggleMic = { viewModel.toggleSpeechRecognition() },
                            onDismissNotice = { viewModel.dismissNotice() }
                        )
                    }
                    GalaxyNavTab.CONVERSATIONS -> {
                        GalaxyConversationsScreen(
                            conversations = conversations,
                            activeId = uiState.activeConversationId,
                            theme = settings.theme,
                            onSelectConversation = { convId -> viewModel.switchConversation(convId) },
                            onTogglePin = { convId -> viewModel.repository.togglePinConversation(convId) },
                            onDeleteConversation = { convId -> viewModel.deleteConversation(convId) },
                            onNewChat = { viewModel.createNewChat() }
                        )
                    }
                    GalaxyNavTab.AGENTS -> {
                        GalaxyAgentsScreen(
                            agents = viewModel.availableAgents,
                            theme = settings.theme,
                            onSelectAgent = { agent -> viewModel.selectAgentPrompt(agent) }
                        )
                    }
                    GalaxyNavTab.NOTES_TASKS -> {
                        GalaxyNotesTasksScreen(
                            savedFiles = savedFiles,
                            tasks = tasks,
                            theme = settings.theme,
                            onDeleteFile = { id -> viewModel.repository.deleteSavedFile(id) },
                            onToggleTask = { id -> viewModel.repository.toggleTask(id) },
                            onDeleteTask = { id -> viewModel.repository.deleteTask(id) },
                            onAddTask = { title, details -> viewModel.repository.addTask(title, details) }
                        )
                    }
                    GalaxyNavTab.SETTINGS -> {
                        GalaxySettingsScreen(
                            settings = settings,
                            onSaveSettings = { updated -> viewModel.updateSettings(updated) }
                        )
                    }
                }
            }
        }

        // Specialized AI Agents Switcher Bottom Sheet
        if (uiState.isAgentBottomSheetOpen) {
            OneUiAgentSwitcherBottomSheet(
                agents = viewModel.availableAgents,
                selectedAgentId = uiState.selectedAgentId,
                theme = settings.theme,
                onDismiss = { viewModel.closeAgentBottomSheet() },
                onSelectAgent = { agentId -> viewModel.selectAgent(agentId) },
                onUsePrompt = { prompt -> viewModel.sendMessage(prompt) }
            )
        }

        // Message Actions Modal Bottom Sheet
        if (uiState.isActionSheetOpen && uiState.selectedMessageForAction != null) {
            val msg = uiState.selectedMessageForAction!!
            val context = LocalContext.current
            OneUiActionSheet(
                message = msg,
                theme = settings.theme,
                onDismiss = { viewModel.closeActionSheet() },
                onAction = { actionType ->
                    if (actionType == "SHARE") {
                        shareChatMessage(context, msg)
                        viewModel.closeActionSheet()
                    } else {
                        viewModel.applyMessageAction(actionType, msg)
                    }
                }
            )
        }
    }
}
