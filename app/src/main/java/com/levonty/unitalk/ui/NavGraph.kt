package com.levonty.unitalk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levonty.unitalk.ui.auth.RegisterScreen
import com.levonty.unitalk.ui.auth.RegisterViewModel
import com.levonty.unitalk.ui.chat.ChatListScreen
import com.levonty.unitalk.ui.chat.ChatScreen
import com.levonty.unitalk.ui.profile.ProfileScreen
import com.levonty.unitalk.ui.search.SearchScreen

object Routes {
    const val REGISTER = "register"
    const val MAIN = "main"
    const val SEARCH = "search"
    const val PROFILE = "profile/{userId}"
    const val CHAT = "chat/{chatId}/{otherUserId}"
    const val CHATS = "chats"

    fun profile(userId: String) = "profile/$userId"
    fun chat(chatId: String, otherUserId: String) = "chat/$chatId/$otherUserId"
}

@Composable
fun UniTalkNavGraph(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.REGISTER) {
            val vm: RegisterViewModel = hiltViewModel()
            RegisterScreen(
                viewModel = vm,
                onRegistered = {
                    navController.navigate(Routes.SEARCH) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SEARCH) {
            SearchScreen(
                onUserClick = { userId ->
                    navController.navigate(Routes.profile(userId))
                },
                onChatsClick = { navController.navigate(Routes.CHATS) },
                onProfileClick = { userId -> navController.navigate(Routes.profile(userId)) }
            )
        }

        composable(
            Routes.PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val userId = back.arguments?.getString("userId") ?: return@composable
            ProfileScreen(
                userId = userId,
                onMessageClick = { chatId, otherUserId ->
                    navController.navigate(Routes.chat(chatId, otherUserId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.CHAT,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType }
            )
        ) { back ->
            val chatId = back.arguments?.getString("chatId") ?: return@composable
            val otherUserId = back.arguments?.getString("otherUserId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                otherUserId = otherUserId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHATS) {
            ChatListScreen(
                onChatClick = { chatId, otherUserId ->
                    navController.navigate(Routes.chat(chatId, otherUserId))
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}