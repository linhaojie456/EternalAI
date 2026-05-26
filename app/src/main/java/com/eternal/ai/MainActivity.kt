package com.eternal.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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
