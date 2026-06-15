package com.levonty.unitalk.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.levonty.unitalk.ui.chat.ChatListScreen
import com.levonty.unitalk.ui.profile.ProfileScreen
import com.levonty.unitalk.ui.search.SearchScreen

data class BottomNavItem(
    val route: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("feed", Icons.Filled.Home, Icons.Outlined.Home, "Лента"),
    BottomNavItem("search", Icons.Filled.Search, Icons.Outlined.Search, "Поиск"),
    BottomNavItem("chats", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubble, "Чаты"),
    BottomNavItem("communities", Icons.Filled.Group, Icons.Outlined.Group, "Сообщества"),
    BottomNavItem("profile", Icons.Filled.Person, Icons.Outlined.Person, "Профиль")
)

@Composable
fun MainScreen(
    currentUserId: String,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToChat: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                if (selectedTab == index) item.iconSelected else item.iconUnselected,
                                contentDescription = item.label
                            )
                        },
                        label = null
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> FeedScreen()
                1 -> SearchScreen(
                    onUserClick = onNavigateToProfile,
                    onChatsClick = { selectedTab = 2 },
                    onProfileClick = onNavigateToProfile
                )
                2 -> ChatListScreen(
                    onChatClick = onNavigateToChat,
                    onBack = {}
                )
                3 -> CommunitiesScreen()
                4 -> ProfileScreen(
                    userId = currentUserId,
                    onMessageClick = onNavigateToChat,
                    onBack = {}
                )
            }
        }
    }
}

@Composable
fun FeedScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏠", style = MaterialTheme.typography.displayMedium)
        Text("Лента", style = MaterialTheme.typography.titleLarge)
        Text("Скоро", color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun CommunitiesScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👥", style = MaterialTheme.typography.displayMedium)
        Text("Сообщества", style = MaterialTheme.typography.titleLarge)
        Text("Скоро", color = MaterialTheme.colorScheme.outline)
    }
}