package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ClaudeS40ViewModel
import com.example.S40Screen
import com.example.model.KeyboardMode
import com.example.model.S40ViewMode
import com.example.ui.components.NokiaKeypad
import com.example.ui.components.NokiaMessageActionsDialog
import com.example.ui.components.NokiaOptionsMenuDialog
import com.example.ui.components.NokiaSoftkeys
import com.example.ui.components.NokiaStatusBar
import com.example.ui.components.ReadingModeView
import com.example.ui.screens.S40AboutScreen
import com.example.ui.screens.S40CalendarScreen
import com.example.ui.screens.S40ChatScreen
import com.example.ui.screens.S40HistoryScreen
import com.example.ui.screens.S40SavedFilesScreen
import com.example.ui.screens.S40SettingsScreen

@Composable
fun S40MainContainer(
    viewModel: ClaudeS40ViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settingsFlow.collectAsState()
    val conversations by viewModel.conversationsFlow.collectAsState()
    val savedFiles by viewModel.savedFilesFlow.collectAsState()
    val tasks by viewModel.tasksFlow.collectAsState()

    // Android Hardware Back button handling
    BackHandler {
        viewModel.onRightSoftKey()
    }

    val primaryColor = Color(settings.theme.primaryHex)

    // Softkey labels based on current screen
    val leftSoftkeyText = when (uiState.currentScreen) {
        S40Screen.CHAT -> "Options"
        S40Screen.READING_MODE -> if (uiState.readingPage > 0) "Prev" else "Exit"
        else -> "Select"
    }

    val centerSoftkeyText = when (uiState.currentScreen) {
        S40Screen.CHAT -> if (uiState.inputText.isNotBlank()) "Send" else ""
        S40Screen.READING_MODE -> if (uiState.readingPage < uiState.totalReadingPages - 1) "Next" else "Done"
        else -> ""
    }

    val rightSoftkeyText = when (uiState.currentScreen) {
        S40Screen.CHAT -> if (uiState.inputText.isNotEmpty()) "Clear" else "Exit"
        S40Screen.READING_MODE -> "Back"
        else -> "Back"
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        containerColor = if (settings.viewMode == S40ViewMode.NOKIA_6300_DEVICE) Color(0xFF0F172A) else Color.White
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (settings.viewMode == S40ViewMode.NOKIA_6300_DEVICE) {
                // NOKIA 6300 RETRO DEVICE FRAME LAYOUT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Phone Bezel: Earpiece & Classic NOKIA Silver Logo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Speaker Slit
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Nokia branding
                        Text(
                            text = "NOKIA",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            fontFamily = FontFamily.SansSerif
                        )
                    }

                    // S40 240x320 RETRO SCREEN DISPLAY
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top S40 Status Bar
                            NokiaStatusBar(
                                theme = settings.theme,
                                title = when (uiState.currentScreen) {
                                    S40Screen.CHAT -> uiState.activeConversationTitle
                                    S40Screen.READING_MODE -> "Reading Mode"
                                    S40Screen.HISTORY -> "Chat History"
                                    S40Screen.SAVED_FILES -> "Saved Files"
                                    S40Screen.CALENDAR -> "S40 Calendar"
                                    S40Screen.SETTINGS -> "Settings"
                                    S40Screen.ABOUT -> "About S40"
                                }
                            )

                            // Screen Body
                            Box(modifier = Modifier.weight(1f)) {
                                ActiveScreenContent(
                                    currentScreen = uiState.currentScreen,
                                    uiState = uiState,
                                    settings = settings,
                                    conversations = conversations,
                                    savedFiles = savedFiles,
                                    tasks = tasks,
                                    viewModel = viewModel
                                )
                            }

                            // Bottom Softkey Bar
                            NokiaSoftkeys(
                                leftText = leftSoftkeyText,
                                centerText = centerSoftkeyText,
                                rightText = rightSoftkeyText,
                                theme = settings.theme,
                                onLeftClick = { viewModel.onLeftSoftKey() },
                                onCenterClick = { viewModel.onCenterNaviKey() },
                                onRightClick = { viewModel.onRightSoftKey() }
                            )
                        }
                    }

                    // BOTTOM HALF: TACTILE NOKIA 6300 KEYPAD & D-PAD
                    NokiaKeypad(
                        isCaps = uiState.isCaps,
                        isNumberMode = uiState.isNumberMode,
                        onDigitPress = { digit -> viewModel.onT9KeyPress(digit) },
                        onBackspace = { viewModel.onKeypadBackspace() },
                        onSpace = { viewModel.onKeypadSpace() },
                        onToggleCaps = { viewModel.onToggleCaps() },
                        onToggleNumberMode = { viewModel.onToggleNumberMode() },
                        onLeftSoftKey = { viewModel.onLeftSoftKey() },
                        onRightSoftKey = { viewModel.onRightSoftKey() },
                        onCenterNaviKey = { viewModel.onCenterNaviKey() },
                        onSwitchToSystemKeyboard = { viewModel.toggleKeyboardMode() }
                    )
                }
            } else {
                // FULLSCREEN S40 OS MODE (Uses full screen on Galaxy A53)
                Column(modifier = Modifier.fillMaxSize()) {
                    NokiaStatusBar(
                        theme = settings.theme,
                        title = when (uiState.currentScreen) {
                            S40Screen.CHAT -> uiState.activeConversationTitle
                            S40Screen.READING_MODE -> "Reading Mode"
                            S40Screen.HISTORY -> "Chat History"
                            S40Screen.SAVED_FILES -> "Saved Files"
                            S40Screen.CALENDAR -> "S40 Calendar"
                            S40Screen.SETTINGS -> "Settings"
                            S40Screen.ABOUT -> "About S40"
                        }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        ActiveScreenContent(
                            currentScreen = uiState.currentScreen,
                            uiState = uiState,
                            settings = settings,
                            conversations = conversations,
                            savedFiles = savedFiles,
                            tasks = tasks,
                            viewModel = viewModel
                        )
                    }

                    NokiaSoftkeys(
                        leftText = leftSoftkeyText,
                        centerText = centerSoftkeyText,
                        rightText = rightSoftkeyText,
                        theme = settings.theme,
                        onLeftClick = { viewModel.onLeftSoftKey() },
                        onCenterClick = { viewModel.onCenterNaviKey() },
                        onRightClick = { viewModel.onRightSoftKey() }
                    )

                    // If keyboard mode is T9 in fullscreen, show bottom compact T9 keypad
                    if (settings.keyboardMode == KeyboardMode.T9_KEYPAD && uiState.currentScreen == S40Screen.CHAT) {
                        NokiaKeypad(
                            isCaps = uiState.isCaps,
                            isNumberMode = uiState.isNumberMode,
                            onDigitPress = { digit -> viewModel.onT9KeyPress(digit) },
                            onBackspace = { viewModel.onKeypadBackspace() },
                            onSpace = { viewModel.onKeypadSpace() },
                            onToggleCaps = { viewModel.onToggleCaps() },
                            onToggleNumberMode = { viewModel.onToggleNumberMode() },
                            onLeftSoftKey = { viewModel.onLeftSoftKey() },
                            onRightSoftKey = { viewModel.onRightSoftKey() },
                            onCenterNaviKey = { viewModel.onCenterNaviKey() },
                            onSwitchToSystemKeyboard = { viewModel.toggleKeyboardMode() }
                        )
                    }
                }
            }

            // MODAL DIALOGS
            if (uiState.isOptionsMenuOpen) {
                NokiaOptionsMenuDialog(
                    theme = settings.theme,
                    webSearchEnabled = settings.webSearchEnabled,
                    onDismiss = { viewModel.closeDialogs() },
                    onSelectScreen = { screen -> viewModel.navigateTo(screen) },
                    onNewChat = { viewModel.createNewChat() },
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    onToggleViewMode = { viewModel.toggleViewMode() }
                )
            }

            if (uiState.isMessageActionsOpen && uiState.selectedMessageForAction != null) {
                NokiaMessageActionsDialog(
                    message = uiState.selectedMessageForAction!!,
                    theme = settings.theme,
                    onDismiss = { viewModel.closeDialogs() },
                    onAction = { actionId ->
                        viewModel.applyMessageAction(actionId, uiState.selectedMessageForAction!!)
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveScreenContent(
    currentScreen: S40Screen,
    uiState: com.example.UiState,
    settings: com.example.model.AppSettings,
    conversations: List<com.example.model.Conversation>,
    savedFiles: List<com.example.model.SavedTextFile>,
    tasks: List<com.example.model.S40Task>,
    viewModel: ClaudeS40ViewModel
) {
    when (currentScreen) {
        S40Screen.CHAT -> {
            S40ChatScreen(
                messages = uiState.messages,
                inputText = uiState.inputText,
                isLoading = uiState.isLoading,
                keyboardMode = settings.keyboardMode,
                theme = settings.theme,
                statusNotice = uiState.statusNotice,
                onInputTextChange = { viewModel.onInputTextChange(it) },
                onSendMessage = { viewModel.sendCurrentMessage() },
                onMessageClick = { viewModel.selectMessageForActions(it) },
                onDismissNotice = { viewModel.dismissNotice() }
            )
        }
        S40Screen.READING_MODE -> {
            uiState.readingMessage?.let { msg ->
                ReadingModeView(
                    message = msg,
                    currentPage = uiState.readingPage,
                    totalPages = uiState.totalReadingPages,
                    theme = settings.theme,
                    onPrevPage = { viewModel.prevReadingPage() },
                    onNextPage = { viewModel.nextReadingPage() },
                    onExitReading = { viewModel.prevReadingPage() }
                )
            }
        }
        S40Screen.HISTORY -> {
            S40HistoryScreen(
                conversations = conversations,
                activeId = uiState.activeConversationId,
                theme = settings.theme,
                onSelectConversation = { convId -> viewModel.switchConversation(convId) },
                onTogglePin = { convId -> viewModel.repository.togglePinConversation(convId) },
                onDeleteConversation = { convId -> viewModel.deleteConversation(convId) },
                onNewChat = { viewModel.createNewChat() }
            )
        }
        S40Screen.SAVED_FILES -> {
            S40SavedFilesScreen(
                savedFiles = savedFiles,
                theme = settings.theme,
                onDeleteFile = { fileId -> viewModel.repository.deleteSavedFile(fileId) }
            )
        }
        S40Screen.CALENDAR -> {
            S40CalendarScreen(
                tasks = tasks,
                theme = settings.theme,
                onToggleTask = { taskId -> viewModel.repository.toggleTask(taskId) },
                onDeleteTask = { taskId -> viewModel.repository.deleteTask(taskId) },
                onAddTask = { title, details -> viewModel.repository.addTask(title, details) }
            )
        }
        S40Screen.SETTINGS -> {
            S40SettingsScreen(
                settings = settings,
                onSaveSettings = { updated -> viewModel.updateSettings(updated) }
            )
        }
        S40Screen.ABOUT -> {
            S40AboutScreen(theme = settings.theme)
        }
    }
}
