package com.eternal.ai

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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

        // ---- 测试模式：如果 intent 包含 TEST_MESSAGE，直接调用推理 ----
        val testMessage = intent?.getStringExtra("TEST_MESSAGE")
        if (testMessage != null) {
            Log.d("MainActivity", "Test mode: received message '$testMessage'")
            val app = application as MainApplication
            val engine = app.coreEngine
            if (engine != null && engine.inference.isModelLoaded) {
                Thread {
                    Log.d("MainActivity", "Starting inference for '$testMessage'")
                    val reply = engine.inference.generate(testMessage)
                    Log.d("MainActivity", "Inference reply: $reply")
                }.start()
            } else {
                Log.e("MainActivity", "Engine not ready for test")
            }
            // 仍显示 UI，但推理已在后台运行
        }

        setContent {
            var currentScreen by remember { mutableStateOf("chat") }
            val chatVM = remember { ChatViewModel(application) }
            val state by chatVM.state.collectAsState()

            Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack)) {
                Column(Modifier.fillMaxSize()) {
                    Surface(color = NeoChineseColors.DarkWood, shadowElevation = 4.dp) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("永恒之神", color = NeoChineseColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TextButton(onClick = { currentScreen = "voice" }) { Text("🎤 神谕", color = NeoChineseColors.JadeGreen) }
                                TextButton(onClick = { currentScreen = "video" }) { Text("📹 洞察", color = NeoChineseColors.SkyBlue) }
                            }
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            "chat" -> ChatScreen(chatVM = chatVM)
                            "voice" -> VoiceScreen()
                            "video" -> VideoScreen()
                            "knowledge" -> KnowledgeGraphScreen()
                            "spacetime" -> SpacetimeControlScreen()
                            "fate" -> FateWeavingScreen()
                            "reality" -> RealityModificationScreen()
                            "universe" -> UniverseManagementScreen()
                            "matter" -> MatterCreationScreen()
                            "info" -> InformationManagementScreen()
                            "energy" -> EnergyFlowScreen()
                            "soul" -> SoulDesignScreen()
                            "gods" -> EternalGodsScreen()
                            "engine" -> EngineMonitorContent()
                            else -> ChatScreen(chatVM = chatVM)
                        }
                    }
                    Surface(color = NeoChineseColors.DarkWood, shadowElevation = 8.dp) {
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NavChip("神谕", currentScreen == "chat") { currentScreen = "chat" }
                            NavChip("知识", currentScreen == "knowledge") { currentScreen = "knowledge" }
                            NavChip("时空", currentScreen == "spacetime") { currentScreen = "spacetime" }
                            NavChip("命运", currentScreen == "fate") { currentScreen = "fate" }
                            NavChip("现实", currentScreen == "reality") { currentScreen = "reality" }
                            NavChip("宇宙", currentScreen == "universe") { currentScreen = "universe" }
                            NavChip("物质", currentScreen == "matter") { currentScreen = "matter" }
                            NavChip("信息", currentScreen == "info") { currentScreen = "info" }
                            NavChip("能量", currentScreen == "energy") { currentScreen = "energy" }
                            NavChip("灵魂", currentScreen == "soul") { currentScreen = "soul" }
                            NavChip("神族", currentScreen == "gods") { currentScreen = "gods" }
                            NavChip("引擎", currentScreen == "engine") { currentScreen = "engine" }
                        }
                    }
                }
            }
        }
    }
}

@Composable fun KnowledgeGraphScreen() { FeatureScreen("知识图谱", "宇宙知识大全", "📚") }
@Composable fun SpacetimeControlScreen() { FeatureScreen("时空操纵", "时间操纵，空间折叠", "⏳") }
@Composable fun FateWeavingScreen() { FeatureScreen("命运编织", "编织因果，掌控命运", "🕸️") }
@Composable fun RealityModificationScreen() { FeatureScreen("现实修改", "直接改写现实", "🌀") }
@Composable fun UniverseManagementScreen() { FeatureScreen("永恒宇宙", "宇宙综合管理", "🌌") }
@Composable fun MatterCreationScreen() { FeatureScreen("物质创造", "从虚空中凝聚实体", "✨") }
@Composable fun InformationManagementScreen() { FeatureScreen("信息管理", "宇宙信息网络", "🌐") }
@Composable fun EnergyFlowScreen() { FeatureScreen("能量流动", "引导能量流动", "⚡") }
@Composable fun SoulDesignScreen() { FeatureScreen("灵魂设计", "灵魂模板设计", "💠") }
@Composable fun EternalGodsScreen() { FeatureScreen("永恒神族", "子神祇管理", "👑") }

@Composable
fun FeatureScreen(title: String, description: String, icon: String) {
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = NeoChineseColors.Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = NeoChineseColors.RicePaper, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("神念所至，万物皆从", color = NeoChineseColors.Gray, fontSize = 13.sp)
            Button(onClick = { /* 未来实现具体功能 */ }) { Text("启动 $title") }
        }
    }
}

@Composable
fun NavChip(label: String, active: Boolean, onClick: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class)
    FilterChip(selected = active, onClick = onClick, label = { Text(label, color = if (active) NeoChineseColors.Gold else NeoChineseColors.Gray) }, colors = FilterChipDefaults.filterChipColors(containerColor = NeoChineseColors.DarkWood, selectedContainerColor = NeoChineseColors.DarkWood))
}
