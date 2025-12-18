package com.chatapp.client.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.chatapp.client.data.model.ChatRoom
import com.chatapp.client.data.remote.ApiClient

data class ChatRoomScreen(
    val apiClient: ApiClient,
    val chatRoom: ChatRoom
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(chatRoom.otherUser.displayName ?: chatRoom.otherUser.username)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("←")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Chat Room",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Coming soon! 💬",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}