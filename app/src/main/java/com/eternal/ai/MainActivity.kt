package com.eternal.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var devMode by remember { mutableStateOf(false) }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (devMode) "永恒 · 开发模式" else "永恒 · 聊天") },
                        actions = {
                            Button(onClick = { devMode = !devMode }) {
                                Text(if (devMode) "聊天" else "开发")
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.padding(padding)) {
                    if (devMode) DevScreen() else ChatScreen()
                }
            }
        }
    }
}
