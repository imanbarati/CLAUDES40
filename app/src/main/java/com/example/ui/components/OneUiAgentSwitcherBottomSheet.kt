package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentDefinition
import com.example.model.GalaxyTheme
import com.example.ui.theme.OneUiAmber
import com.example.ui.theme.OneUiCyan
import com.example.ui.theme.OneUiEmerald
import com.example.ui.theme.OneUiIndigo
import com.example.ui.theme.OneUiRose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiAgentSwitcherBottomSheet(
    agents: List<AgentDefinition>,
    selectedAgentId: String,
    theme: GalaxyTheme,
    onDismiss: () -> Unit,
    onSelectAgent: (String) -> Unit,
    onUsePrompt: (String) -> Unit
) {
    val isDark = theme.isDark
    val primaryColor = Color(theme.primaryHex)
    val sheetBg = if (isDark) Color(0xFF141720) else Color(0xFFFFFFFF)
    val cardBg = if (isDark) Color(0xFF1C202B) else Color(0xFFF8FAFC)
    val cardBorder = if (isDark) Color(0xFF282D3D) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val haptic = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Specialized AI Agents",
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Select an agent to tune Claude's responses",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(agents, key = { it.id }) { agent ->
                    val isSelected = agent.id == selectedAgentId

                    val icon = when (agent.id) {
                        "web_search" -> Icons.Default.Search
                        "summarizer" -> Icons.Default.Compress
                        "eli5" -> Icons.Default.Lightbulb
                        "translator" -> Icons.Default.Language
                        "task_planner" -> Icons.Default.DateRange
                        else -> Icons.Default.AutoAwesome
                    }

                    val agentColor = when (agent.id) {
                        "web_search" -> OneUiEmerald
                        "summarizer" -> OneUiAmber
                        "eli5" -> OneUiCyan
                        "translator" -> OneUiRose
                        "task_planner" -> OneUiIndigo
                        else -> primaryColor
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) agentColor.copy(alpha = 0.12f) else cardBg)
                            .border(
                                1.dp,
                                if (isSelected) agentColor else cardBorder,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectAgent(agent.id)
                                onDismiss()
                            }
                            .padding(14.dp)
                            .testTag("agent_sheet_item_${agent.id}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(agentColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = agentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = agent.name,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(agentColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = agent.tag,
                                            color = agentColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    text = agent.description,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(agentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active Agent",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Sample prompt starter chip
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDark) Color(0xFF10131A) else Color(0xFFEDF2F7))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectAgent(agent.id)
                                    onUsePrompt(agent.defaultPrompt)
                                    onDismiss()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Try prompt: ",
                                color = agentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "\"${agent.defaultPrompt.take(45)}...\"",
                                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
