package com.eternal.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("chat") }
            val chatVM = remember { ChatViewModel(application) }
            val state by chatVM.state.collectAsState()

            Box(modifier = Modifier.fillMaxSize().background(DeepSeekColors.Background)) {
                Column(Modifier.fillMaxSize()) {
                    // 顶部导航栏
                    Surface(color = DeepSeekColors.Surface, shadowElevation = 4.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                NavButton("聊天", currentScreen == "chat") { currentScreen = "chat" }
                                NavButton("开发", currentScreen == "dev") { currentScreen = "dev" }
                                NavButton("历史", currentScreen == "history") { currentScreen = "history" }
                                NavButton("引擎", false) { startActivity(Intent(this@MainActivity, EngineMonitorActivity::class.java)) }
                            }
                            // 网络状态与开关
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val networkColor = if (state.isNetworkConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                                Text(
                                    text = if (state.isNetworkConnected) "已连接" else "离线",
                                    color = networkColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.width(4.dp))
                                Switch(
                                    checked = state.isNetworkEnabled,
                                    onCheckedChange = { chatVM.setNetworkEnabled(it) },
                                    modifier = Modifier.height(24.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = DeepSeekColors.Gold,
                                        uncheckedTrackColor = DeepSeekColors.Gray
                                    )
                                )
                            }
                        }
                    }
                    // 内容区域
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            "chat" -> ChatScreen(chatVM = chatVM)
                            "dev" -> DevScreen()
                            "history" -> HistoryScreen(onBack = { currentScreen = "chat" })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavButton(text: String, isActive: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(
            text = text,
            color = if (isActive) DeepSeekColors.Gold else DeepSeekColors.Gray,
            fontSize = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
    }
}
