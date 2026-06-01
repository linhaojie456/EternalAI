package com.eternal.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("chat") }
            Box(modifier = Modifier.fillMaxSize().background(DeepSeekColors.Background)) {
                when (currentScreen) {
                    "chat" -> ChatScreen()
                    "dev" -> DevScreen()
                    "history" -> HistoryScreen(onBack = { currentScreen = "chat" })
                }
            }
            // 底部导航
            Surface(
                color = DeepSeekColors.Surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { currentScreen = "chat" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)
                    ) {
                        Text("聊天", color = if (currentScreen == "chat") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(
                        onClick = { currentScreen = "dev" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)
                    ) {
                        Text("开发", color = if (currentScreen == "dev") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(
                        onClick = { currentScreen = "history" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)
                    ) {
                        Text("历史", color = if (currentScreen == "history") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(
                        onClick = { startActivity(Intent(this@MainActivity, EngineMonitorActivity::class.java)) },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)
                    ) {
                        Text("引擎", color = DeepSeekColors.Gray)
                    }
                }
            }
        }
    }
}
