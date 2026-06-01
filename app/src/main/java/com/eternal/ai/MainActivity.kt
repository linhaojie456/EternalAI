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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("chat") }
            val chatVM = (application as MainApplication).let { app ->
                ChatViewModel(app)
            }
            val state by chatVM.state.collectAsState()

            Box(modifier = Modifier.fillMaxSize().background(DeepSeekColors.Background)) {
                when (currentScreen) {
                    "chat" -> ChatScreen(chatVM = chatVM)
                    "dev" -> DevScreen()
                    "history" -> HistoryScreen(onBack = { currentScreen = "chat" })
                }
            }
            // 底部导航栏
            Surface(color = DeepSeekColors.Surface, shadowElevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { currentScreen = "chat" }, colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)) {
                        Text("聊天", color = if (currentScreen == "chat") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(onClick = { currentScreen = "dev" }, colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)) {
                        Text("开发", color = if (currentScreen == "dev") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(onClick = { currentScreen = "history" }, colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)) {
                        Text("历史", color = if (currentScreen == "history") DeepSeekColors.Gold else DeepSeekColors.Gray)
                    }
                    Button(onClick = { startActivity(Intent(this@MainActivity, EngineMonitorActivity::class.java)) }, colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Surface)) {
                        Text("引擎", color = DeepSeekColors.Gray)
                    }
                    // 网络状态与开关
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val networkColor = if (state.isNetworkConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                        Text(if (state.isNetworkConnected) "已连接" else "离线", color = networkColor, fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Switch(
                            checked = state.isNetworkEnabled,
                            onCheckedChange = { chatVM.setNetworkEnabled(it) },
                            modifier = Modifier.height(24.dp),
                            colors = SwitchDefaults.colors(checkedTrackColor = DeepSeekColors.Gold, uncheckedTrackColor = DeepSeekColors.Gray)
                        )
                    }
                }
            }
        }
    }
}
