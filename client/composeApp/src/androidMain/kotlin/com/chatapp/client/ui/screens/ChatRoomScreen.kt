package com.chatapp.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.chatapp.client.data.model.ChatRoom
import com.chatapp.client.data.model.Message
import com.chatapp.client.data.remote.ApiClient
import kotlinx.coroutines.launch

data class ChatRoomScreen(
    val apiClient: ApiClient,
    val chatRoom: ChatRoom
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val listState = rememberLazyListState()

        var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
        var messageText by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        var isSending by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var connectionState by remember { mutableStateOf<String>("Disconnected") }

        // Initialize WebSocket
        // Load messages with auto-refresh
        LaunchedEffect(Unit) {
            // Initial load
            apiClient.getMessages(chatRoom.id)
                .onSuccess { messageList ->
                    messages = messageList.reversed()
                    isLoading = false

                    if (messageList.isNotEmpty()) {
                        listState.scrollToItem(messageList.size - 1)
                    }
                }
                .onFailure { error ->
                    errorMessage = error.message
                    isLoading = false
                }

            // Auto-refresh every 2 seconds
            while (true) {
                kotlinx.coroutines.delay(2000)
                apiClient.getMessages(chatRoom.id)
                    .onSuccess { messageList ->
                        val newMessages = messageList.reversed()
                        val oldSize = messages.size
                        messages = newMessages

                        // Auto-scroll if new messages
                        if (newMessages.size > oldSize) {
                            listState.animateScrollToItem(newMessages.size - 1)
                        }
                    }
            }
        }


        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(chatRoom.otherUser.displayName ?: chatRoom.otherUser.username)
                            Text(
                                text = connectionState,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (connectionState == "Connected") {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("←", style = MaterialTheme.typography.headlineSmall)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                isLoading = true
                                apiClient.getMessages(chatRoom.id)
                                    .onSuccess { messageList ->
                                        messages = messageList.reversed()
                                        isLoading = false
                                    }
                                    .onFailure { error ->
                                        errorMessage = error.message
                                        isLoading = false
                                    }
                            }
                        }) {
                            Text("🔄")
                        }
                    }
                )
            },
            bottomBar = {
                MessageInputBar(
                    messageText = messageText,
                    onMessageChange = { messageText = it },
                    onSendClick = {
                        if (messageText.isNotBlank()) {
                            scope.launch {
                                isSending = true
                                apiClient.sendMessage(chatRoom.id, messageText)
                                    .onSuccess { sentMessage ->
                                        // Add to local list immediately (optimistic UI)
                                        messages = messages + sentMessage
                                        messageText = ""
                                        // Scroll to bottom
                                        listState.animateScrollToItem(messages.size - 1)
                                    }
                                    .onFailure { error ->
                                        errorMessage = "Failed to send: ${error.message}"
                                    }
                                isSending = false
                            }
                        }
                    },
                    enabled = !isSending
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error: $errorMessage",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        apiClient.getMessages(chatRoom.id)
                                            .onSuccess { messageList ->
                                                messages = messageList.reversed()
                                                isLoading = false
                                            }
                                            .onFailure { error ->
                                                errorMessage = error.message
                                                isLoading = false
                                            }
                                    }
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Retry")
                            }
                        }
                    }

                    messages.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No messages yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Start the conversation!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                MessageBubble(
                                    message = message,
                                    isCurrentUser = message.senderUsername != chatRoom.otherUser.username
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isCurrentUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(message.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isCurrentUser) {
                        Text(
                            text = when (message.status) {
                                com.chatapp.client.data.model.MessageStatus.SENT -> "✓"
                                com.chatapp.client.data.model.MessageStatus.DELIVERED -> "✓✓"
                                com.chatapp.client.data.model.MessageStatus.READ -> "✓✓"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.status == com.chatapp.client.data.model.MessageStatus.READ) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onSendClick,
                enabled = enabled && messageText.isNotBlank()
            ) {
                Text(
                    text = "➤",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (enabled && messageText.isNotBlank()) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

fun formatTime(isoDateTime: String): String {
    return try {
        val time = isoDateTime.split("T")[1].take(5)
        time
    } catch (e: Exception) {
        isoDateTime
    }
}