package com.eternal.ai
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
class MainActivity : ComponentActivity() {
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
