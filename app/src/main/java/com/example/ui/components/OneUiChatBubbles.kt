package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.GalaxyTheme
import com.example.ui.theme.OneUiAmber
import com.example.ui.theme.OneUiCyan
import com.example.ui.theme.OneUiEmerald
import com.example.ui.theme.OneUiIndigo
import com.example.ui.theme.OneUiRose
import com.example.ui.theme.OneUiViolet

// Samsung One UI Signature Squircle Corner Radii for Chat Bubbles
val OneUiUserSquircle = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 24.dp,
    bottomEnd = 6.dp // Subtle tail pointing right
)

val OneUiAssistantSquircle = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = 6.dp, // Subtle tail pointing left
    bottomEnd = 24.dp
)

/**
 * Quick export feature: shares an individual chat message to other Android apps
 */
fun shareChatMessage(context: Context, message: ChatMessage) {
    val exportSnippet = buildString {
        if (message.role == "user") {
            append("Query:\n${message.content}\n")
        } else {
            append("🤖 ${message.agentName}:\n\n${message.content}\n")
            if (message.sources.isNotEmpty()) {
                append("\nVerified Web Sources:\n")
                message.sources.forEachIndexed { i, src ->
                    append("[${i + 1}] $src\n")
                }
            }
        }
        append("\n— Shared from Claude for Galaxy")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Claude Galaxy - ${if (message.role == "user") "Question" else message.agentName}")
        putExtra(Intent.EXTRA_TEXT, exportSnippet)
    }
    val chooser = Intent.createChooser(intent, "Share conversation snippet via")
    context.startActivity(chooser)
}

@Composable
fun OneUiChatBubble(
    message: ChatMessage,
    theme: GalaxyTheme,
    onOpenActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message.role == "user") {
        OneUiUserChatBubble(
            message = message,
            theme = theme,
            modifier = modifier
        )
    } else {
        OneUiAssistantChatBubble(
            message = message,
            theme = theme,
            onOpenActions = onOpenActions,
            modifier = modifier
        )
    }
}

@Composable
fun OneUiUserChatBubble(
    message: ChatMessage,
    theme: GalaxyTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = theme.isDark

    // Samsung One UI Color Roles for User Bubble
    val bubbleColor = if (isDark) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    val textColor = if (isDark) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(OneUiUserSquircle)
                .background(bubbleColor)
                .border(1.dp, borderColor, OneUiUserSquircle)
                .padding(horizontal = 16.dp, vertical = 13.dp)
                .testTag("user_bubble_${message.id}")
        ) {
            Column {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.formattedTime,
                        color = textColor.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Claude Query", message.content))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy query",
                                tint = textColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { shareChatMessage(context, message) },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share query to other apps",
                                tint = textColor.copy(alpha = 0.75f),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OneUiAssistantChatBubble(
    message: ChatMessage,
    theme: GalaxyTheme,
    onOpenActions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = theme.isDark

    // Detect agent-specific accent color & icon
    val (agentColor, agentIcon) = getAgentVisuals(message.agentName)

    // Samsung One UI Color Roles for Assistant Bubble
    val surfaceColor = if (isDark) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isDark) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isDark) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Agent Persona Identity Header (One UI Avatar Squircle + Badge)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 6.dp, bottom = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(agentColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = agentIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = message.agentName,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            if (message.isPinned) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = OneUiRose,
                    modifier = Modifier.size(12.dp)
                )
            }

            if (message.isSaved) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Saved",
                    tint = agentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Assistant Message Bubble
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(OneUiAssistantSquircle)
                .background(surfaceColor)
                .border(1.dp, borderColor, OneUiAssistantSquircle)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("assistant_bubble_${message.id}")
        ) {
            Column {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.SansSerif
                )

                // Web Sources Citation Section (Squircle Cards)
                if (message.sources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isDark) MaterialTheme.colorScheme.background
                                else MaterialTheme.colorScheme.secondaryContainer
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = OneUiEmerald,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Verified Live Sources & References",
                                color = OneUiEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                        message.sources.forEachIndexed { i, src ->
                            Text(
                                text = "[${i + 1}] $src",
                                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Metadata & One UI Action Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.formattedTime,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Copy Button Pill
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Claude Message", message.content))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Share Button Pill (Quick Export to installed Android apps)
                        IconButton(
                            onClick = { shareChatMessage(context, message) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share snippet with other apps",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Message Actions Sheet Trigger Pill
                        IconButton(
                            onClick = onOpenActions,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Message Actions",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getAgentVisuals(agentName: String): Pair<Color, ImageVector> {
    return when {
        agentName.contains("Web", ignoreCase = true) -> Pair(OneUiEmerald, Icons.Default.Search)
        agentName.contains("Summarizer", ignoreCase = true) -> Pair(OneUiAmber, Icons.Default.Compress)
        agentName.contains("ELI5", ignoreCase = true) || agentName.contains("Simplifier", ignoreCase = true) -> Pair(OneUiCyan, Icons.Default.Lightbulb)
        agentName.contains("Translator", ignoreCase = true) -> Pair(OneUiRose, Icons.Default.Language)
        agentName.contains("Task", ignoreCase = true) || agentName.contains("Planner", ignoreCase = true) -> Pair(OneUiIndigo, Icons.Default.DateRange)
        else -> Pair(OneUiViolet, Icons.Default.AutoAwesome)
    }
}
