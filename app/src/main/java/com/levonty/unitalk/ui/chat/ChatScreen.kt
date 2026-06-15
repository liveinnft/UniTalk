package com.levonty.unitalk.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.levonty.unitalk.data.model.Message
import com.levonty.unitalk.data.repository.ChatRepository
import com.levonty.unitalk.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepo: ChatRepository,
    private val sessionRepo: SessionRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    var currentUserId: String? = null
        private set

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            sessionRepo.currentUserId.collect { userId ->
                currentUserId = userId
            }
            chatRepo.getMessages(chatId).collect { msgList ->
                _messages.value = msgList.sortedBy { it.timestamp }
            }
        }
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val senderId = currentUserId ?: return@launch
            chatRepo.sendMessage(chatId, senderId, text)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatId: String,
    otherUserId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatId) {
        viewModel.loadMessages(chatId)
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чат") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(8.dp),
                reverseLayout = false
            ) {
                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isMine = message.senderId == viewModel.currentUserId
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Сообщение...") },
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(chatId, inputText)
                            inputText = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Отправить")
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMine: Boolean) {
    val bubbleColor = if (isMine) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bubbleColor)
                .padding(8.dp)
                .widthIn(max = 240.dp)
        ) {
            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}