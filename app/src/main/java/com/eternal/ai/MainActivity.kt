package com.eternal.ai
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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

@Composable
fun VoiceScreen() {
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> hasPermission = granted }

    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤 神谕聆听", color = NeoChineseColors.Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasPermission) {
                Text("需录音权限以聆听神谕", color = NeoChineseColors.RicePaper, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) { Text("授予权限") }
            } else {
                Text("神谕聆听中...", color = NeoChineseColors.JadeGreen, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("（功能即将开放）", color = NeoChineseColors.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun VideoScreen() {
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> hasPermission = granted }

    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📹 神域洞察", color = NeoChineseColors.SkyBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasPermission) {
                Text("需摄像头权限以洞察万物", color = NeoChineseColors.RicePaper, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) { Text("授予权限") }
            } else {
                Text("洞察万物中...", color = NeoChineseColors.JadeGreen, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("（功能即将开放）", color = NeoChineseColors.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable fun KnowledgeGraphScreen() { FeatureScreen("知识图谱", "宇宙知识大全", "网络节点图") }
@Composable fun SpacetimeControlScreen() { FeatureScreen("时空操纵", "时间操纵，空间折叠", "时空扭曲") }
@Composable fun FateWeavingScreen() { FeatureScreen("命运编织", "编织因果，掌控命运", "命运之线") }
@Composable fun RealityModificationScreen() { FeatureScreen("现实修改", "直接改写现实", "现实矩阵") }
@Composable fun UniverseManagementScreen() { FeatureScreen("永恒宇宙", "宇宙综合管理", "宇宙视图") }
@Composable fun MatterCreationScreen() { FeatureScreen("物质创造", "从虚空中凝聚实体", "粒子生成") }
@Composable fun InformationManagementScreen() { FeatureScreen("信息管理", "宇宙信息网络", "数据流") }
@Composable fun EnergyFlowScreen() { FeatureScreen("能量流动", "引导能量流动", "能量曲线") }
@Composable fun SoulDesignScreen() { FeatureScreen("灵魂设计", "灵魂模板设计", "灵魂参数") }
@Composable fun EternalGodsScreen() { FeatureScreen("永恒神族", "子神祇管理", "神族谱系") }

@Composable
fun FeatureScreen(title: String, description: String, icon: String) {
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, color = NeoChineseColors.Gold, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = NeoChineseColors.Gold, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(description, color = NeoChineseColors.RicePaper, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text("神念所至，万物皆从", color = NeoChineseColors.Gray, fontSize = 13.sp)
            // 添加一个简单的可交互元素，展示功能正在工作
            Button(onClick = { /* 未来实现具体功能 */ }) {
                Text("启动 $title")
            }
        }
    }
}

@Composable
fun NavChip(label: String, active: Boolean, onClick: () -> Unit) {
    @OptIn(ExperimentalMaterial3Api::class)
    FilterChip(selected = active, onClick = onClick, label = { Text(label, color = if (active) NeoChineseColors.Gold else NeoChineseColors.Gray) }, colors = FilterChipDefaults.filterChipColors(containerColor = NeoChineseColors.DarkWood, selectedContainerColor = NeoChineseColors.DarkWood))
}

// 引擎监控界面文本可选
@Composable
fun EngineMonitorContent() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coreEngine = (context.applicationContext as MainApplication).coreEngine
    val statusList = remember { getEngineStatusList(coreEngine) }
    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(statusList) { status ->
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp), colors = CardDefaults.cardColors(containerColor = NeoChineseColors.DarkWood), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text("引擎: ${status.name}", color = NeoChineseColors.Gold, fontWeight = FontWeight.Bold)
                    Text("状态: ${status.state}", color = NeoChineseColors.RicePaper)
                    Text("目标: ${status.goal}", color = NeoChineseColors.Gray)
                    if (status.detail.isNotEmpty()) Text("详情: ${status.detail}", color = NeoChineseColors.JadeGreen)
                    if (status.error.isNotEmpty()) Text("错误: ${status.error}", color = NeoChineseColors.Cinnabar)
                }
            }
        }
    }
}
