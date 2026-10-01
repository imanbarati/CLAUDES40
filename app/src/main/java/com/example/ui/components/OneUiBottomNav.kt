package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.model.GalaxyNavTab
import com.example.model.GalaxyTheme

@Composable
fun OneUiBottomNav(
    currentTab: GalaxyNavTab,
    theme: GalaxyTheme,
    onTabSelected: (GalaxyNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(theme.primaryHex)
    val isDark = theme.isDark

    val navBarBg = if (isDark) Color(0xFF0F172A) else Color(0xFFFFFFFF)
    val activeIndicator = primaryColor.copy(alpha = 0.2f)
    val unselectedColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = navBarBg
    ) {
        val tabs = listOf(
            Triple(GalaxyNavTab.CHAT, "Chat", Pair(Icons.Default.ChatBubble, Icons.Outlined.ChatBubbleOutline)),
            Triple(GalaxyNavTab.CONVERSATIONS, "History", Pair(Icons.Default.Forum, Icons.Outlined.Forum)),
            Triple(GalaxyNavTab.AGENTS, "Agents", Pair(Icons.Default.AutoAwesome, Icons.Outlined.AutoAwesome)),
            Triple(GalaxyNavTab.NOTES_TASKS, "Notes", Pair(Icons.Default.Folder, Icons.Outlined.Folder)),
            Triple(GalaxyNavTab.SETTINGS, "Settings", Pair(Icons.Default.Settings, Icons.Outlined.Settings))
        )

        tabs.forEach { (tab, label, icons) ->
            val isSelected = currentTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) icons.first else icons.second,
                        contentDescription = label,
                        tint = if (isSelected) primaryColor else unselectedColor
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primaryColor else unselectedColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = activeIndicator
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
            )
        }
    }
}
