package com.levonty.unitalk.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.levonty.unitalk.data.repository.SessionRepository
import com.levonty.unitalk.ui.auth.RegisterScreen
import com.levonty.unitalk.ui.chat.ChatScreen
import com.levonty.unitalk.ui.profile.ProfileScreen
import com.levonty.unitalk.ui.welcome.WelcomeScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

object Routes {
    const val WELCOME = "welcome"
    const val REGISTER = "register"
    const val MAIN = "main/{userId}"
    const val PROFILE = "profile/{userId}"
    const val CHAT = "chat/{chatId}/{otherUserId}"

    fun main(userId: String) = "main/$userId"
    fun profile(userId: String) = "profile/$userId"
    fun chat(chatId: String, otherUserId: String) = "chat/$chatId/$otherUserId"
}

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionRepo: SessionRepository
) : ViewModel() {
    val userId: StateFlow<String?> = sessionRepo.currentUserId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}

@Composable
fun UniTalkNavGraph(startDestination: String = Routes.WELCOME) {
    val navController = rememberNavController()
    val rootVm: RootViewModel = hiltViewModel()
    val userId by rootVm.userId.collectAsState()

    LaunchedEffect(userId) {
        if (userId != null) {
            val current = navController.currentDestination?.route
            if (current == Routes.WELCOME || current == Routes.REGISTER) {
                navController.navigate(Routes.main(userId!!)) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.WELCOME) {
            WelcomeScreen(onGetStarted = { navController.navigate(Routes.REGISTER) })
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered = { /* LaunchedEffect above handles redirect */ }
            )
        }

        composable(
            Routes.MAIN,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val uid = back.arguments?.getString("userId") ?: return@composable
            MainScreen(
                currentUserId = uid,
                onNavigateToProfile = { navController.navigate(Routes.profile(it)) },
                onNavigateToChat = { chatId, otherUserId -> navController.navigate(Routes.chat(chatId, otherUserId)) }
            )
        }

        composable(
            Routes.PROFILE,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val profileId = back.arguments?.getString("userId") ?: return@composable
            ProfileScreen(
                userId = profileId,
                onMessageClick = { chatId, otherUserId -> navController.navigate(Routes.chat(chatId, otherUserId)) },
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
            ChatScreen(chatId = chatId, otherUserId = otherUserId, onBack = { navController.popBackStack() })
        }
    }
}