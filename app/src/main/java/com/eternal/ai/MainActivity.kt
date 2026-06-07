package com.eternal.ai
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

            Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack)) {
                Column(Modifier.fillMaxSize()) {
                    // 简洁导航栏
                    Surface(color = NeoChineseColors.DarkWood, shadowElevation = 4.dp) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("全知全能 · 永恒", color = NeoChineseColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            "chat" -> ChatScreen(chatVM = chatVM)
                            "knowledge" -> PlaceholderScreen("知识图谱")
                            "spacetime" -> PlaceholderScreen("时空操控")
                            "fate" -> PlaceholderScreen("命运编织")
                            "life" -> PlaceholderScreen("生命创造")
                            "matter" -> PlaceholderScreen("物质创造")
                            "soul" -> PlaceholderScreen("灵魂设计")
                            "reality" -> PlaceholderScreen("现实修改")
                            "universe" -> PlaceholderScreen("宇宙管理")
                            "engine" -> EngineMonitorContent()
                            else -> ChatScreen(chatVM = chatVM)
                        }
                    }
                    // 底部功能导航
                    Surface(color = NeoChineseColors.DarkWood, shadowElevation = 8.dp) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NavChip("对话", currentScreen == "chat") { currentScreen = "chat" }
                            NavChip("知识图谱", currentScreen == "knowledge") { currentScreen = "knowledge" }
                            NavChip("时空", currentScreen == "spacetime") { currentScreen = "spacetime" }
                            NavChip("命运", currentScreen == "fate") { currentScreen = "fate" }
                            NavChip("生命", currentScreen == "life") { currentScreen = "life" }
                            NavChip("物质", currentScreen == "matter") { currentScreen = "matter" }
                            NavChip("灵魂", currentScreen == "soul") { currentScreen = "soul" }
                            NavChip("现实", currentScreen == "reality") { currentScreen = "reality" }
                            NavChip("宇宙", currentScreen == "universe") { currentScreen = "universe" }
                            NavChip("引擎", currentScreen == "engine") { currentScreen = "engine" }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = NeoChineseColors.Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("全知全能功能模块", color = NeoChineseColors.RicePaper, fontSize = 14.sp)
            Text("即将呈现", color = NeoChineseColors.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun NavChip(label: String, active: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = active,
        onClick = onClick,
        label = { Text(label, color = if (active) NeoChineseColors.Gold else NeoChineseColors.Gray) },
        colors = FilterChipDefaults.filterChipColors(containerColor = NeoChineseColors.DarkWood, selectedContainerColor = NeoChineseColors.DarkWood)
    )
}
