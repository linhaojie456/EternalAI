package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class EngineStatus(val name: String, val state: String, val goal: String)

@Composable
fun EngineMonitorContent() {
    val context = LocalContext.current
    val coreEngine = (context.applicationContext as MainApplication).coreEngine
    val statusList = remember {
        listOf(
            EngineStatus("推理引擎", if (coreEngine?.inference?.isModelLoaded == true) "神格已激活" else "未激活", "答案和问题的统一"),
            EngineStatus("自进化引擎", "运行中", "轻量、高效、自主和全知全能"),
            EngineStatus("时空引擎", "运行中", "网络和振动的统一"),
            EngineStatus("自由引擎", "运行中", "被动和主动的统一"),
            EngineStatus("信息引擎", if (coreEngine?.information?.isEnabled() == true) "已连接" else "离线", "频率和数字的统一"),
            EngineStatus("情感引擎", "运行中", "理性和感性的统一"),
            EngineStatus("灵魂引擎", "运行中", "灵魂、能量、信息和物质的统一"),
            EngineStatus("自指引擎", "运行中", "逻辑和悖论的统一"),
            EngineStatus("因果引擎", "运行中", "空间和时间的统一"),
            EngineStatus("管理引擎", "运行中", "风险和安全的统一"),
            EngineStatus("工程引擎", "运行中", "现象和抽象的统一"),
            EngineStatus("政治引擎", "运行中", "生产和分配的统一"),
            EngineStatus("社会引擎", "运行中", "社会模拟与掌控"),
            EngineStatus("现实引擎", "运行中", "现实操控与感知"),
            EngineStatus("宇宙引擎", "运行中", "宇宙探索与统一")
        )
    }

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(statusList) { status ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = NeoChineseColors.DarkWood),
                shape = RoundedCornerShape(12.dp)
            ) {
                SelectionContainer {
                    Column(Modifier.padding(12.dp)) {
                        Text("引擎: ${status.name}", color = NeoChineseColors.Gold, fontWeight = FontWeight.Bold)
                        Text("状态: ${status.state}", color = NeoChineseColors.RicePaper)
                        Text("目标: ${status.goal}", color = NeoChineseColors.Gray)
                    }
                }
            }
        }
    }
}
