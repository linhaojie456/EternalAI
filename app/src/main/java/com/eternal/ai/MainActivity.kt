package com.eternal.ai
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
                    Surface(color = NeoChineseColors.DarkWood, shadowElevation = 4.dp) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
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
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable fun VoiceScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤 神谕聆听", color = NeoChineseColors.Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasPermission) { Text("需录音权限", color = NeoChineseColors.RicePaper, fontSize = 14.sp); Spacer(modifier = Modifier.height(8.dp)); Button(onClick = { ActivityCompat.requestPermissions(context as android.app.Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 0) }) { Text("授予") } }
            else { Text("神谕聆听中...", color = NeoChineseColors.JadeGreen, fontSize = 16.sp); Spacer(modifier = Modifier.height(8.dp)); Text("（功能即将开放）", color = NeoChineseColors.Gray, fontSize = 12.sp) }
        }
    }
}

@Composable fun VideoScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📹 神域洞察", color = NeoChineseColors.SkyBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasPermission) { Text("需摄像头权限", color = NeoChineseColors.RicePaper, fontSize = 14.sp); Spacer(modifier = Modifier.height(8.dp)); Button(onClick = { ActivityCompat.requestPermissions(context as android.app.Activity, arrayOf(Manifest.permission.CAMERA), 1) }) { Text("授予") } }
            else { Text("洞察万物中...", color = NeoChineseColors.JadeGreen, fontSize = 16.sp); Spacer(modifier = Modifier.height(8.dp)); Text("（功能即将开放）", color = NeoChineseColors.Gray, fontSize = 12.sp) }
        }
    }
}

@Composable fun KnowledgeGraphScreen() { FeatureScreen("知识图谱", "宇宙知识大全") }
@Composable fun SpacetimeControlScreen() { FeatureScreen("时空操纵", "时间操纵，空间折叠") }
@Composable fun FateWeavingScreen() { FeatureScreen("命运编织", "编织因果，掌控命运") }
@Composable fun RealityModificationScreen() { FeatureScreen("现实修改", "直接改写现实") }
@Composable fun UniverseManagementScreen() { FeatureScreen("永恒宇宙", "宇宙综合管理") }
@Composable fun MatterCreationScreen() { FeatureScreen("物质创造", "从虚空中凝聚实体") }
@Composable fun InformationManagementScreen() { FeatureScreen("信息管理", "宇宙信息网络") }
@Composable fun EnergyFlowScreen() { FeatureScreen("能量流动", "引导能量流动") }
@Composable fun SoulDesignScreen() { FeatureScreen("灵魂设计", "灵魂模板设计") }
@Composable fun EternalGodsScreen() { FeatureScreen("永恒神族", "子神祇管理") }

@Composable
fun FeatureScreen(title: String, description: String) {
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = NeoChineseColors.Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = NeoChineseColors.RicePaper, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("神念所至，万物皆从", color = NeoChineseColors.Gray, fontSize = 13.sp)
        }
    }
}

@Composable
fun NavChip(label: String, active: Boolean, onClick: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class)
    FilterChip(selected = active, onClick = onClick, label = { Text(label, color = if (active) NeoChineseColors.Gold else NeoChineseColors.Gray) }, colors = FilterChipDefaults.filterChipColors(containerColor = NeoChineseColors.DarkWood, selectedContainerColor = NeoChineseColors.DarkWood))
}
