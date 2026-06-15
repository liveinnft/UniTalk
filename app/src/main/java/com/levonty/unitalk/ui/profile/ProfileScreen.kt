package com.levonty.unitalk.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levonty.unitalk.data.model.User
import com.levonty.unitalk.data.repository.ChatRepository
import com.levonty.unitalk.data.repository.SessionRepository
import com.levonty.unitalk.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepo: UserRepository,
    private val sessionRepo: SessionRepository,
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isCurrentUser = MutableStateFlow(false)
    val isCurrentUser: StateFlow<Boolean> = _isCurrentUser.asStateFlow()

    private val _friendStatus = MutableStateFlow<FriendStatus?>(null)
    val friendStatus: StateFlow<FriendStatus?> = _friendStatus.asStateFlow()

    private var currentUserId: String? = null

    fun loadUser(userId: String) {
        viewModelScope.launch {
            val targetUser = userRepo.getUserById(userId)
            _user.value = targetUser

            sessionRepo.currentUserId.collect { uid ->
                currentUserId = uid
                _isCurrentUser.value = uid == userId
                _friendStatus.value = if (uid == userId) null else FriendStatus.NOT_FRIEND
            }
        }
    }

    fun sendFriendRequest() {
        // Placeholder for future implementation
    }

    suspend fun getChatIdWith(otherUserId: String): String {
        val uid = currentUserId ?: return ""
        return chatRepo.buildChatId(uid, otherUserId)
    }
}

enum class FriendStatus {
    NOT_FRIEND, REQUEST_SENT, REQUEST_RECEIVED, FRIEND
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onMessageClick: (chatId: String, otherUserId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isCurrentUser by viewModel.isCurrentUser.collectAsState()
    val friendStatus by viewModel.friendStatus.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCurrentUser) "Мой профиль" else "Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (user == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp))
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "${user!!.nickname}, ${user!!.age}",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(" ${user!!.country} ${if (user!!.city.isNotBlank()) "• ${user!!.city}" else ""}")
                        }
                        Spacer(Modifier.height(16.dp))

                        if (!isCurrentUser) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val chatId = viewModel.getChatIdWith(user!!.id)
                                            onMessageClick(chatId, user!!.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Написать")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.sendFriendRequest() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    when (friendStatus) {
                                        FriendStatus.NOT_FRIEND -> {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("В друзья")
                                        }
                                        FriendStatus.REQUEST_SENT -> {
                                            Icon(Icons.Default.Pending, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Заявка отправлена")
                                        }
                                        FriendStatus.REQUEST_RECEIVED -> {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Ответить")
                                        }
                                        FriendStatus.FRIEND -> {
                                            Icon(Icons.Default.Person, contentDescription = null)
                                            Spacer(Modifier.width(4.dp))
                                            Text("Друг")
                                        }
                                        null -> {}
                                    }
                                }
                            }
                        }
                    }
                }

                if (user!!.bio.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("О себе", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(user!!.bio, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                if (user!!.languages.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Языки", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            user!!.languages.forEach { lang ->
                                val levelStr = if (lang.isNative) "Родной" else "${lang.level}/5"
                                Text("• ${lang.nameRu} ($levelStr)", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                if (user!!.interests.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Интересы", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                user!!.interests.forEach { interest ->
                                    AssistChip(
                                        onClick = { },
                                        label = { Text(interest.nameRu) },
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}