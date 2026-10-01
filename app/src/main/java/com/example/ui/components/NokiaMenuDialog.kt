package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.S40Screen
import com.example.model.ChatMessage
import com.example.model.S40Theme

data class S40MenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val isHighlighted: Boolean = false
)

@Composable
fun NokiaOptionsMenuDialog(
    theme: S40Theme,
    webSearchEnabled: Boolean,
    onDismiss: () -> Unit,
    onSelectScreen: (S40Screen) -> Unit,
    onNewChat: () -> Unit,
    onToggleWebSearch: () -> Unit,
    onToggleViewMode: () -> Unit
) {
    val items = listOf(
        S40MenuItem("new_chat", "1. New Chat", "Start fresh conversation", Icons.Default.Add),
        S40MenuItem("history", "2. Chat History", "Browse and continue past chats", Icons.Default.History),
        S40MenuItem(
            "web_search",
            "3. Web Search: " + if (webSearchEnabled) "[ON]" else "[OFF]",
            "Include live web sources with answers",
            Icons.Default.Search,
            isHighlighted = webSearchEnabled
        ),
        S40MenuItem("files", "4. Saved .TXT Files", "Stored responses on Memory Card", Icons.Default.Folder),
        S40MenuItem("tasks", "5. S40 To-Do & Calendar", "Claude generated reminders", Icons.Default.DateRange),
        S40MenuItem("view_mode", "6. Switch View Mode", "Nokia 6300 Phone / Fullscreen S40", Icons.Default.PhoneAndroid),
        S40MenuItem("settings", "7. Settings & API", "API key, models, themes, audio", Icons.Default.Settings),
        S40MenuItem("about", "8. About Claude S40", "Emir Karşıyakalı / Galaxy A53", Icons.Default.Info)
    )

    NokiaDialogContainer(
        title = "Options Menu",
        theme = theme,
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(items) { index, item ->
                NokiaMenuRow(
                    index = index + 1,
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    isHighlighted = item.isHighlighted,
                    theme = theme,
                    onClick = {
                        when (item.id) {
                            "new_chat" -> {
                                onNewChat()
                                onDismiss()
                            }
                            "history" -> onSelectScreen(S40Screen.HISTORY)
                            "web_search" -> onToggleWebSearch()
                            "files" -> onSelectScreen(S40Screen.SAVED_FILES)
                            "tasks" -> onSelectScreen(S40Screen.CALENDAR)
                            "view_mode" -> onToggleViewMode()
                            "settings" -> onSelectScreen(S40Screen.SETTINGS)
                            "about" -> onSelectScreen(S40Screen.ABOUT)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NokiaMessageActionsDialog(
    message: ChatMessage,
    theme: S40Theme,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    val items = listOf(
        S40MenuItem("READING_MODE", "1. Reading Mode", "Page through full reply (240x320)", Icons.Default.MenuBook),
        S40MenuItem("SHORTEN", "2. Shorten Reply", "Condense into 2-3 lines for S40", Icons.Default.Compress),
        S40MenuItem("SIMPLIFY", "3. Simplify (ELI5)", "Explain in simple terms", Icons.Default.Lightbulb),
        S40MenuItem("TRANSLATE_TR", "4. Translate to Türkçe", "Translate response to Turkish", Icons.Default.Language),
        S40MenuItem("TRANSLATE_EN", "5. Translate to English", "Translate response to English", Icons.Default.Language),
        S40MenuItem("SAVE_TXT", "6. Save as .TXT File", "Save to Memory Card", Icons.Default.Description),
        S40MenuItem("ADD_TODO", "7. Add to To-Do Tasks", "Extract reminders into S40 Calendar", Icons.Default.TaskAlt),
        S40MenuItem("PIN", if (message.isPinned) "8. Unpin Message" else "8. Pin Message", "Keep message at top", Icons.Default.PushPin)
    )

    NokiaDialogContainer(
        title = "Message Actions",
        theme = theme,
        onDismiss = onDismiss
    ) {
        // Message preview header
        Text(
            text = "\"" + (if (message.content.length > 50) message.content.take(47) + "..." else message.content) + "\"",
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = FontFamily.SansSerif,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(items) { index, item ->
                NokiaMenuRow(
                    index = index + 1,
                    title = item.title,
                    subtitle = item.subtitle,
                    icon = item.icon,
                    isHighlighted = false,
                    theme = theme,
                    onClick = {
                        onAction(item.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun NokiaDialogContainer(
    title: String,
    theme: S40Theme,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val primaryColor = Color(theme.primaryHex)

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
        ) {
            Column {
                // S40 Dialog Blue Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryColor)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                }

                // Dialog Body
                content()

                // Dialog Bottom Bar (Softkeys: Select | Back)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Select",
                        color = primaryColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Back",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onDismiss)
                    )
                }
            }
        }
    }
}

@Composable
private fun NokiaMenuRow(
    index: Int,
    title: String,
    subtitle: String?,
    icon: ImageVector,
    isHighlighted: Boolean,
    theme: S40Theme,
    onClick: () -> Unit
) {
    val primaryColor = Color(theme.primaryHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick
            )
            .background(if (isHighlighted) primaryColor.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag("menu_item_$index"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isHighlighted) primaryColor else Color(0xFF475569),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isHighlighted) primaryColor else Color(0xFF0F172A),
                fontSize = 13.sp,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}
