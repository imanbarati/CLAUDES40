package com.example.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ClaudeS40ViewModel
import com.example.model.GalaxyNavTab
import com.example.ui.components.OneUiActionSheet
import com.example.ui.components.OneUiAgentSwitcherBottomSheet
import com.example.ui.components.OneUiBottomNav
import com.example.ui.components.OneUiHeader
import com.example.ui.components.shareChatMessage
import com.example.ui.navigation.GalaxyScreen
import com.example.ui.screens.GalaxyAgentsScreen
import com.example.ui.screens.GalaxyChatScreen
import com.example.ui.screens.GalaxyConversationsScreen
import com.example.ui.screens.GalaxyNotesTasksScreen
import com.example.ui.screens.GalaxySettingsScreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalaxyMainContainer(
    viewModel: ClaudeS40ViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val conversations by viewModel.conversationsFlow.collectAsState()
    val savedFiles by viewModel.savedFilesFlow.collectAsState()
    val tasks by viewModel.tasksFlow.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: GalaxyScreen.Chat.route
    val currentScreen = GalaxyScreen.fromRoute(currentRoute)

    // Sync ViewModel currentTab when NavController route changes
    LaunchedEffect(currentScreen) {
        if (uiState.currentTab != currentScreen.tab) {
            viewModel.switchTab(currentScreen.tab)
        }
    }

    // Sync NavController when ViewModel changes tab programmatically (e.g., from prompt select or back button)
    LaunchedEffect(uiState.currentTab) {
        val targetScreen = GalaxyScreen.fromTab(uiState.currentTab)
        if (currentRoute != targetScreen.route) {
            navController.navigate(targetScreen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    // Handle Back button: close sheets or navigate back to CHAT destination
    BackHandler {
        if (uiState.isActionSheetOpen) {
            viewModel.closeActionSheet()
        } else if (uiState.isAgentBottomSheetOpen) {
            viewModel.closeAgentBottomSheet()
        } else if (currentScreen != GalaxyScreen.Chat) {
            navController.navigate(GalaxyScreen.Chat.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
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
            if (!isImeVisible || currentScreen != GalaxyScreen.Chat) {
                OneUiBottomNav(
                    currentTab = currentScreen.tab,
                    theme = settings.theme,
                    onTabSelected = { tab ->
                        val target = GalaxyScreen.fromTab(tab)
                        viewModel.switchTab(tab)
                        navController.navigate(target.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
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
                title = when (currentScreen) {
                    GalaxyScreen.Chat -> "Claude Galaxy"
                    GalaxyScreen.Conversations -> "Conversations"
                    GalaxyScreen.Agents -> "AI Agents Hub"
                    GalaxyScreen.NotesTasks -> "Notes & Tasks"
                    GalaxyScreen.Settings -> "Settings"
                },
                subtitle = when (currentScreen) {
                    GalaxyScreen.Chat -> "${uiState.activeAgent} • ${uiState.activeConversationTitle}"
                    GalaxyScreen.Conversations -> "${conversations.size} active sessions"
                    GalaxyScreen.Agents -> "6 specialized AI models"
                    GalaxyScreen.NotesTasks -> "${savedFiles.size} notes • ${tasks.count { !it.isCompleted }} tasks"
                    GalaxyScreen.Settings -> settings.theme.displayName
                },
                theme = settings.theme,
                webSearchActive = settings.webSearchEnabled || uiState.selectedAgentId == "web_search",
                onNewChatClick = if (currentScreen == GalaxyScreen.Chat || currentScreen == GalaxyScreen.Conversations) {
                    {
                        viewModel.createNewChat()
                        if (currentScreen != GalaxyScreen.Chat) {
                            navController.navigate(GalaxyScreen.Chat.route)
                        }
                    }
                } else null,
                onToggleWebSearch = if (currentScreen == GalaxyScreen.Chat) {
                    { viewModel.toggleWebSearch() }
                } else null
            )

            // Jetpack Navigation NavHost Container
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = GalaxyScreen.Chat.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(GalaxyScreen.Chat.route) {
                        GalaxyChatScreen(
                            messages = uiState.messages,
                            inputText = uiState.inputText,
                            isLoading = uiState.isLoading,
                            isListening = uiState.isListening,
                            audioAmplitude = uiState.audioAmplitude,
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

                    composable(GalaxyScreen.Conversations.route) {
                        GalaxyConversationsScreen(
                            conversations = conversations,
                            activeId = uiState.activeConversationId,
                            theme = settings.theme,
                            onSelectConversation = { convId ->
                                viewModel.switchConversation(convId)
                                navController.navigate(GalaxyScreen.Chat.route)
                            },
                            onTogglePin = { convId -> viewModel.togglePinConversation(convId) },
                            onDeleteConversation = { convId -> viewModel.deleteConversation(convId) },
                            onNewChat = {
                                viewModel.createNewChat()
                                navController.navigate(GalaxyScreen.Chat.route)
                            }
                        )
                    }

                    composable(GalaxyScreen.Agents.route) {
                        GalaxyAgentsScreen(
                            agents = viewModel.availableAgents,
                            theme = settings.theme,
                            onSelectAgent = { agent ->
                                viewModel.selectAgentPrompt(agent)
                                navController.navigate(GalaxyScreen.Chat.route)
                            }
                        )
                    }

                    composable(GalaxyScreen.NotesTasks.route) {
                        GalaxyNotesTasksScreen(
                            savedFiles = savedFiles,
                            tasks = tasks,
                            theme = settings.theme,
                            onDeleteFile = { id -> viewModel.repository.deleteSavedFile(id) },
                            onToggleTask = { id -> viewModel.toggleTask(id) },
                            onDeleteTask = { id -> viewModel.deleteTask(id) },
                            onAddTask = { title, details -> viewModel.addTask(title, details) }
                        )
                    }

                    composable(GalaxyScreen.Settings.route) {
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
                onUsePrompt = { prompt ->
                    viewModel.sendMessage(prompt)
                    navController.navigate(GalaxyScreen.Chat.route)
                }
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
