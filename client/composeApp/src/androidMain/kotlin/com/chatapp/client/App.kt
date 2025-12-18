package com.chatapp.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.chatapp.client.ui.screens.LoginScreen

@Composable
fun App() {
    MaterialTheme {
        Navigator(LoginScreen())
    }
}