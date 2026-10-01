package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.KeyboardMode
import com.example.model.S40Theme
import kotlinx.coroutines.delay

@Composable
fun S40ChatScreen(
    messages: List<ChatMessage>,
    inputText: String,
    isLoading: Boolean,
    keyboardMode: KeyboardMode,
    theme: S40Theme,
    statusNotice: String?,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onMessageClick: (ChatMessage) -> Unit,
    onDismissNotice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val primaryColor = Color(theme.primaryHex)
    val bgColor = Color(theme.bgHex).copy(alpha = 0.35f)

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Status notice toast if active
        if (statusNotice != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFEF08A))
                    .clickable(onClick = onDismissNotice)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "ℹ️ $statusNotice",
                    color = Color(0xFF713F12),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    theme = theme,
                    onClick = { onMessageClick(msg) }
                )
            }

            if (isLoading) {
                item {
                    LoadingMessageItem(theme = theme)
                }
            }
        }

        // Bottom Input Line (when in QWERTY mode or previewing current T9 buffer)
        InputDisplayBar(
            inputText = inputText,
            keyboardMode = keyboardMode,
            theme = theme,
            onInputTextChange = onInputTextChange,
            onSend = onSendMessage
        )
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    theme: S40Theme,
    onClick: () -> Unit
) {
    val isUser = message.role == "user"
    val primaryColor = Color(theme.primaryHex)

    val cardBg = if (isUser) primaryColor.copy(alpha = 0.12f) else Color.White
    val borderColor = if (isUser) primaryColor.copy(alpha = 0.3f) else Color(0xFFE2E8F0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.88f else 0.96f)
                .clip(RoundedCornerShape(8.dp))
                .background(cardBg)
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(),
                    onClick = onClick
                )
                .padding(8.dp)
                .testTag("chat_msg_${message.id}")
        ) {
            Column {
                // Header of message card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isUser) {
                            Text(
                                text = "👤 You",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "*",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Claude S40",
                                    color = Color(0xFFB45309),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (message.isPinned) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        if (message.isSaved) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Saved",
                                tint = primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.formattedTime,
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Message Body Text
                Text(
                    text = message.content,
                    color = Color(0xFF0F172A),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontFamily = FontFamily.SansSerif
                )

                // Web Search Citations
                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Sources",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Web Sources:",
                                color = Color(0xFF0284C7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        message.sources.forEachIndexed { i, src ->
                            Text(
                                text = "[${i + 1}] $src",
                                color = Color(0xFF475569),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingMessageItem(theme: S40Theme) {
    val primaryColor = Color(theme.primaryHex)
    var dots by remember { mutableStateOf(".") }

    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            dots = when (dots) {
                "." -> ".."
                ".." -> "..."
                else -> "."
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = primaryColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Connecting to Claude$dots (8 KiB packet)",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InputDisplayBar(
    inputText: String,
    keyboardMode: KeyboardMode,
    theme: S40Theme,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val primaryColor = Color(theme.primaryHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(width = 1.dp, color = Color(0xFFCBD5E1))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text field or T9 typed buffer
            if (keyboardMode == KeyboardMode.SYSTEM_KEYBOARD) {
                TextField(
                    value = inputText,
                    onValueChange = onInputTextChange,
                    placeholder = {
                        Text(
                            text = "Type message for Claude...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 3
                )
            } else {
                // T9 Buffer preview box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = "Use keypad below to type (T9 Multitap)...",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "> $inputText",
                                color = Color(0xFF0F172A),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            // Animated cursor
                            var cursorVisible by remember { mutableStateOf(true) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    delay(500)
                                    cursorVisible = !cursorVisible
                                }
                            }
                            if (cursorVisible) {
                                Text(
                                    text = "█",
                                    color = primaryColor,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Quick Send Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) primaryColor else Color(0xFFE2E8F0))
                    .clickable(
                        enabled = inputText.isNotBlank(),
                        onClick = onSend
                    )
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
