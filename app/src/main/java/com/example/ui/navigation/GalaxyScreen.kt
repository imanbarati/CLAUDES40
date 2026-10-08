package com.example.ui.navigation

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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.model.GalaxyNavTab

sealed class GalaxyScreen(
    val route: String,
    val title: String,
    val tab: GalaxyNavTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Chat : GalaxyScreen(
        route = "chat",
        title = "Chat",
        tab = GalaxyNavTab.CHAT,
        selectedIcon = Icons.Filled.ChatBubble,
        unselectedIcon = Icons.Outlined.ChatBubbleOutline
    )

    object Conversations : GalaxyScreen(
        route = "conversations",
        title = "History",
        tab = GalaxyNavTab.CONVERSATIONS,
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum
    )

    object Agents : GalaxyScreen(
        route = "agents",
        title = "Agents",
        tab = GalaxyNavTab.AGENTS,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    )

    object NotesTasks : GalaxyScreen(
        route = "notes_tasks",
        title = "Notes & Tasks",
        tab = GalaxyNavTab.NOTES_TASKS,
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    )

    object Settings : GalaxyScreen(
        route = "settings",
        title = "Settings",
        tab = GalaxyNavTab.SETTINGS,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavScreens = listOf(Chat, Conversations, Agents, NotesTasks, Settings)

        fun fromRoute(route: String?): GalaxyScreen {
            return when (route) {
                Chat.route -> Chat
                Conversations.route -> Conversations
                Agents.route -> Agents
                NotesTasks.route -> NotesTasks
                Settings.route -> Settings
                else -> Chat
            }
        }

        fun fromTab(tab: GalaxyNavTab): GalaxyScreen {
            return when (tab) {
                GalaxyNavTab.CHAT -> Chat
                GalaxyNavTab.CONVERSATIONS -> Conversations
                GalaxyNavTab.AGENTS -> Agents
                GalaxyNavTab.NOTES_TASKS -> NotesTasks
                GalaxyNavTab.SETTINGS -> Settings
            }
        }
    }
}
