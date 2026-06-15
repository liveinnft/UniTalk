package com.levonty.unitalk.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levonty.unitalk.data.model.Chat
import com.levonty.unitalk.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepo: ChatRepository
) : ViewModel() {

    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    init {
        viewModelScope.launch {
            chatRepo.getAllChats().collect { chatList ->
                _chats.value = chatList.sortedByDescending { it.lastMessageTime }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (chatId: String, otherUserId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чаты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Нет чатов", style = MaterialTheme.typography.titleMedium)
                    Text("Начните общение через поиск")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chats) { chat ->
                    ChatItem(chat = chat) {
                        onChatClick(chat.id, chat.participantId)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Чат с пользователем",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.lastMessage.take(50),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())
                        .format(Date(chat.lastMessageTime)),
                    style = MaterialTheme.typography.bodySmall
                )
                if (chat.unreadCount > 0) {
                    Badge { Text(chat.unreadCount.toString()) }
                }
            }
        }
    }
}