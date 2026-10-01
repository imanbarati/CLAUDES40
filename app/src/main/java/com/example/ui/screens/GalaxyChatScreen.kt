package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.ChatMessage
import com.example.model.GalaxyTheme
import com.example.ui.components.OneUiAgentSegmentedControl
import com.example.ui.components.OneUiChatBubble
import com.example.ui.components.OneUiSmartReplyRow
import com.example.ui.theme.OneUiRose
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GalaxyChatScreen(
    messages: List<ChatMessage>,
    inputText: String,
    isLoading: Boolean,
    isListening: Boolean,
    speechTranscriptionNotice: String?,
    smartReplies: List<String>,
    webSearchEnabled: Boolean,
    selectedAgentId: String,
    activeAgentName: String,
    theme: GalaxyTheme,
    statusNotice: String?,
    onInputTextChange: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    onSmartReplySelected: (String) -> Unit,
    onMessageClick: (ChatMessage) -> Unit,
    onToggleWebSearch: () -> Unit,
    onSelectAgent: (String) -> Unit,
    onOpenAgentSheet: () -> Unit,
    onToggleMic: () -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val screenBg = Color(theme.bgHex)
    val isImeVisible = WindowInsets.isImeVisible

    // Detect if user has scrolled away from the bottom
    val canScrollForward by remember { derivedStateOf { listState.canScrollForward } }

    val lastMessageId = messages.lastOrNull()?.id
    val lastMessageLength = messages.lastOrNull()?.content?.length ?: 0

    // Auto-scroll mechanism 1: Anchors to the bottom when new messages arrive or loading state changes
    LaunchedEffect(messages.size, lastMessageId, lastMessageLength, isLoading) {
        if (messages.isNotEmpty()) {
            val targetIndex = (messages.size + (if (isLoading) 1 else 0)).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    // Auto-scroll mechanism 2: Anchors to the bottom when the soft keyboard is opened
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && messages.isNotEmpty()) {
            delay(120) // Allow window insets / resize layout to complete before scrolling
            val targetIndex = (messages.size + (if (isLoading) 1 else 0)).coerceAtLeast(0)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .imePadding()
    ) {
        // Status notice banner if present
        if (statusNotice != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF08A))
                    .clickable(onClick = onDismissNotice)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ℹ️ $statusNotice",
                    color = Color(0xFF713F12),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Specialized AI Agents Segmented Control Component (Main Interface)
        OneUiAgentSegmentedControl(
            selectedAgentId = selectedAgentId,
            theme = theme,
            onAgentSelected = onSelectAgent,
            onOpenAgentSheet = onOpenAgentSheet
        )

        // Message Feed Container with Floating Scroll-To-Bottom Pill
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(messages, key = { it.id }) { msg ->
                    OneUiChatBubble(
                        message = msg,
                        theme = theme,
                        onOpenActions = { onMessageClick(msg) }
                    )
                }

                if (isLoading) {
                    item {
                        GalaxyLoadingBubble(theme = theme)
                    }
                }

                item { Spacer(modifier = Modifier.height(6.dp)) }
            }

            // Quick Scroll-to-Bottom Floating Action Pill
            if (canScrollForward && messages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (theme.isDark) Color(0xFF1E293B) else Color.White)
                        .border(1.dp, if (theme.isDark) Color(0xFF334155) else Color(0xFFCBD5E1), CircleShape)
                        .clickable {
                            coroutineScope.launch {
                                val target = (messages.size + (if (isLoading) 1 else 0)).coerceAtLeast(0)
                                listState.animateScrollToItem(target)
                            }
                        }
                        .testTag("scroll_to_bottom_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom",
                        tint = Color(theme.primaryHex),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live Voice Dictation Feedback Pill
        if (isListening || speechTranscriptionNotice != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(OneUiRose.copy(alpha = 0.15f))
                    .border(1.dp, OneUiRose.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(OneUiRose)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = speechTranscriptionNotice ?: "Listening for $activeAgentName...",
                    color = OneUiRose,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Tap mic to finish",
                    color = OneUiRose.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }

        // Smart Reply Suggestions (Context-Aware Quick-Tap Chips)
        if (smartReplies.isNotEmpty() && !isLoading) {
            OneUiSmartReplyRow(
                suggestions = smartReplies,
                theme = theme,
                onReplySelected = onSmartReplySelected
            )
        }

        // Quick Action Chips Bar (Above Composer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Web Search Chip
            QuickChip(
                label = "Web Search",
                icon = Icons.Default.Search,
                isActive = webSearchEnabled || selectedAgentId == "web_search",
                theme = theme,
                onClick = onToggleWebSearch
            )

            // Summarize Chip
            QuickChip(
                label = "Summarize",
                icon = Icons.Default.Compress,
                isActive = selectedAgentId == "summarizer",
                theme = theme,
                onClick = { onSelectAgent("summarizer") }
            )

            // ELI5 Chip
            QuickChip(
                label = "Explain Simply",
                icon = Icons.Default.Lightbulb,
                isActive = selectedAgentId == "eli5",
                theme = theme,
                onClick = { onSelectAgent("eli5") }
            )

            // Turkish Translate Chip
            QuickChip(
                label = "Türkçe",
                icon = Icons.Default.Language,
                isActive = selectedAgentId == "translator",
                theme = theme,
                onClick = { onSelectAgent("translator") }
            )

            // Extract Tasks Chip
            QuickChip(
                label = "Extract Tasks",
                icon = Icons.Default.TaskAlt,
                isActive = selectedAgentId == "task_planner",
                theme = theme,
                onClick = { onSelectAgent("task_planner") }
            )
        }

        // Modern Samsung One UI Floating Pill Composer with Microphone
        val composerPlaceholder = when (selectedAgentId) {
            "web_search" -> "Search live web with Web Searcher..."
            "summarizer" -> "Paste text to summarize..."
            "eli5" -> "Ask to explain simply like I'm 5..."
            "translator" -> "Enter text to translate..."
            "task_planner" -> "Describe tasks or events to plan..."
            else -> "Ask $activeAgentName anything..."
        }

        GalaxyPillComposer(
            inputText = inputText,
            placeholder = composerPlaceholder,
            isLoading = isLoading,
            isListening = isListening,
            theme = theme,
            onInputTextChange = onInputTextChange,
            onInputFocused = {
                coroutineScope.launch {
                    delay(120)
                    if (messages.isNotEmpty()) {
                        val target = (messages.size + (if (isLoading) 1 else 0)).coerceAtLeast(0)
                        listState.animateScrollToItem(target)
                    }
                }
            },
            onToggleMic = onToggleMic,
            onSend = { onSendMessage(null) }
        )
    }
}

@Composable
private fun GalaxyLoadingBubble(theme: GalaxyTheme) {
    val isDark = theme.isDark
    val primaryColor = Color(theme.primaryHex)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF1E293B) else Color.White)
            .border(1.dp, if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = primaryColor,
            strokeWidth = 2.dp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Claude is thinking...",
            color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    theme: GalaxyTheme,
    onClick: () -> Unit
) {
    val primaryColor = Color(theme.primaryHex)
    val isDark = theme.isDark
    val chipBg = if (isActive) primaryColor else if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
    val chipBorder = if (isActive) primaryColor else if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val textColor = if (isActive) Color.White else if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155)
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(chipBg)
            .border(1.dp, chipBorder, RoundedCornerShape(18.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun GalaxyPillComposer(
    inputText: String,
    placeholder: String,
    isLoading: Boolean,
    isListening: Boolean,
    theme: GalaxyTheme,
    onInputTextChange: (String) -> Unit,
    onInputFocused: () -> Unit,
    onToggleMic: () -> Unit,
    onSend: () -> Unit
) {
    val context = LocalContext.current
    val isDark = theme.isDark
    val primaryColor = Color(theme.primaryHex)

    val containerBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)

    // Permission launcher for Microphone Speech Recognition
    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onToggleMic()
        } else {
            Toast.makeText(
                context,
                "Microphone permission is required to dictate messages",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(containerBg)
                .border(1.dp, borderColor, RoundedCornerShape(28.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color(0xFF94A3B8),
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            onInputFocused()
                        }
                    }
                    .testTag("galaxy_chat_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (isDark) Color.White else Color(0xFF0F172A),
                    unfocusedTextColor = if (isDark) Color.White else Color(0xFF0F172A)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() }),
                maxLines = 4
            )

            // Microphone Dictation Button
            val haptic = LocalHapticFeedback.current
            val micColor = if (isListening) OneUiRose else primaryColor
            val micBg = if (isListening) OneUiRose.copy(alpha = 0.2f) else if (isDark) Color(0xFF282B36) else Color(0xFFF1F5F9)

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(micBg)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (hasPerm) {
                            onToggleMic()
                        } else {
                            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                    .testTag("chat_mic_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop voice dictation" else "Dictate message with voice",
                    tint = micColor,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Send Button Pill
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isLoading) primaryColor
                        else if (isDark) Color(0xFF334155)
                        else Color(0xFFE2E8F0)
                    )
                    .clickable(
                        enabled = inputText.isNotBlank() && !isLoading,
                        onClick = onSend
                    )
                    .testTag("galaxy_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank() && !isLoading) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
