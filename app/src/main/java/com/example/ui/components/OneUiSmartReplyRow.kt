package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GalaxyTheme
import com.example.ui.theme.OneUiViolet

/**
 * Smart Reply component that displays 3 quick-tap context-aware response chips
 * directly above the input area to accelerate user interactions.
 */
@Composable
fun OneUiSmartReplyRow(
    suggestions: List<String>,
    theme: GalaxyTheme,
    onReplySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val isDark = theme.isDark
    val primaryColor = Color(theme.primaryHex)

    val chipBg = if (isDark) Color(0xFF1E222D) else Color(0xFFF1F5F9)
    val chipBorder = if (isDark) Color(0xFF2E3547) else Color(0xFFCBD5E1)
    val textColor = if (isDark) Color(0xFFF1F5F9) else Color(0xFF0F172A)

    AnimatedVisibility(
        visible = suggestions.isNotEmpty(),
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Smart reply icon label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Smart Reply Suggestions",
                    tint = OneUiViolet,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Smart:",
                    color = OneUiViolet,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Suggestions Chips (up to 3 quick-tap choices)
            suggestions.take(3).forEachIndexed { index, suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReplySelected(suggestion)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("smart_reply_chip_$index")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = suggestion,
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
