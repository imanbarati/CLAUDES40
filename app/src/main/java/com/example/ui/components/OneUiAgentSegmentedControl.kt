package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GalaxyTheme
import com.example.ui.theme.OneUiAmber
import com.example.ui.theme.OneUiCyan
import com.example.ui.theme.OneUiEmerald
import com.example.ui.theme.OneUiIndigo
import com.example.ui.theme.OneUiRose

data class SegmentAgentItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OneUiAgentSegmentedControl(
    selectedAgentId: String,
    theme: GalaxyTheme,
    onAgentSelected: (String) -> Unit,
    onOpenAgentSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = theme.isDark
    val primaryColor = Color(theme.primaryHex)
    val haptic = LocalHapticFeedback.current

    val agents = listOf(
        SegmentAgentItem("claude_core", "Claude", Icons.Default.AutoAwesome, primaryColor),
        SegmentAgentItem("web_search", "Web Searcher", Icons.Default.Search, OneUiEmerald),
        SegmentAgentItem("summarizer", "Summarizer", Icons.Default.Compress, OneUiAmber),
        SegmentAgentItem("eli5", "Explain Simply", Icons.Default.Lightbulb, OneUiCyan),
        SegmentAgentItem("translator", "Translator", Icons.Default.Language, OneUiRose),
        SegmentAgentItem("task_planner", "Task Manager", Icons.Default.DateRange, OneUiIndigo),
        SegmentAgentItem("local_gguf_host", "Local GGUF", Icons.Default.Memory, Color(0xFF10B981))
    )

    val containerBg = if (isDark) Color(0xFF161822) else Color(0xFFEDF2F7)
    val containerBorder = if (isDark) Color(0xFF282B36) else Color(0xFFE2E8F0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Horizontal Scrollable Segmented Track
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(containerBg)
                .border(1.dp, containerBorder, RoundedCornerShape(22.dp))
                .padding(3.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            agents.forEach { agent ->
                val isSelected = agent.id == selectedAgentId

                val activeBgColor by animateColorAsState(
                    targetValue = if (isSelected) agent.accentColor else Color.Transparent,
                    label = "segment_bg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else if (isDark) Color(0xFF94A3B8) else Color(0xFF475569),
                    label = "segment_text"
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(activeBgColor)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAgentSelected(agent.id)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("agent_segment_${agent.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = agent.icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = agent.label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Open Bottom Sheet Button Pill
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isDark) Color(0xFF1E222D) else Color(0xFFE2E8F0))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onOpenAgentSheet()
                }
                .testTag("expand_agent_sheet_btn"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "All Specialized Agents",
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
