package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiActionSheet(
    message: ChatMessage,
    theme: GalaxyTheme,
    onDismiss: () -> Unit,
    onAction: (String) -> Unit
) {
    val context = LocalContext.current
    val primaryColor = Color(theme.primaryHex)
    val isDark = theme.isDark
    val sheetBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Drag handle & title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Message Actions",
                    color = textColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = message.formattedTime,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text preview pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                    .padding(10.dp)
            ) {
                Text(
                    text = "\"" + (if (message.content.length > 90) message.content.take(87) + "..." else message.content) + "\"",
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    fontSize = 12.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action rows
            ActionRow(
                title = "Copy Text",
                desc = "Copy message content to clipboard",
                icon = Icons.Default.ContentCopy,
                color = primaryColor,
                textColor = textColor
            ) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Claude Message", message.content))
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                onDismiss()
            }

            ActionRow(
                title = "⚡ Summarize Response",
                desc = "Executive key takeaways and bullet points",
                icon = Icons.Default.Compress,
                color = Color(0xFFEAB308),
                textColor = textColor
            ) {
                onAction("SHORTEN")
            }

            ActionRow(
                title = "💡 Explain Simply (ELI5)",
                desc = "Break down concepts in plain intuitive terms",
                icon = Icons.Default.Lightbulb,
                color = Color(0xFF38BDF8),
                textColor = textColor
            ) {
                onAction("SIMPLIFY")
            }

            ActionRow(
                title = "🇹🇷 Translate to Turkish",
                desc = "Fluent natural Turkish translation",
                icon = Icons.Default.Language,
                color = Color(0xFFEF4444),
                textColor = textColor
            ) {
                onAction("TRANSLATE_TR")
            }

            ActionRow(
                title = "🇬🇧 Translate to English",
                desc = "Fluent English translation",
                icon = Icons.Default.Language,
                color = Color(0xFF3B82F6),
                textColor = textColor
            ) {
                onAction("TRANSLATE_EN")
            }

            ActionRow(
                title = "📤 Share Snippet",
                desc = "Export message to WhatsApp, Gmail, Messages, etc.",
                icon = Icons.Default.Share,
                color = Color(0xFF0284C7),
                textColor = textColor
            ) {
                onAction("SHARE")
            }

            ActionRow(
                title = "💾 Save to Notes (.txt)",
                desc = "Store in saved documents",
                icon = Icons.Default.Description,
                color = Color(0xFF10B981),
                textColor = textColor
            ) {
                onAction("SAVE_NOTE")
            }

            ActionRow(
                title = "📅 Add to Galaxy Tasks & Calendar",
                desc = "Extract reminders into task manager",
                icon = Icons.Default.DateRange,
                color = Color(0xFF8B5CF6),
                textColor = textColor
            ) {
                onAction("ADD_TASK")
            }

            ActionRow(
                title = if (message.isPinned) "📌 Unpin Message" else "📌 Pin Message",
                desc = "Keep this answer highlighted",
                icon = Icons.Default.PushPin,
                color = Color(0xFFF43F5E),
                textColor = textColor
            ) {
                onAction("PIN")
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0))

            ActionRow(
                title = "⚡ Offload Deep Research (NVIDIA Node)",
                desc = "Offload recursive research to remote GPU tensor cores",
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF10B981),
                textColor = textColor
            ) {
                onAction("NVIDIA_DEEP_RESEARCH")
            }

            ActionRow(
                title = "🛡️ Offload Code & Security Audit (NVIDIA Node)",
                desc = "AST parsing, vulnerability scan, and architectural refactoring",
                icon = Icons.Default.Tune,
                color = Color(0xFF0284C7),
                textColor = textColor
            ) {
                onAction("NVIDIA_CODE_AUDIT")
            }

            ActionRow(
                title = "👥 Multi-Agent Consensus Debate (NVIDIA Node)",
                desc = "3-persona expert debate & consensus synthesis",
                icon = Icons.Default.Memory,
                color = Color(0xFF8B5CF6),
                textColor = textColor
            ) {
                onAction("NVIDIA_CONSENSUS")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }
    }
}
