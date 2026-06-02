package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun EngineMonitorContent() {
    val context = LocalContext.current
    val coreEngine = (context.applicationContext as MainApplication).coreEngine
    val statusList = remember { getEngineStatusList(coreEngine) }

    LazyColumn(modifier = Modifier.padding(8.dp)) {
        items(statusList) { status ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSeekColors.Surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("引擎: ${status.name}", color = DeepSeekColors.Gold, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Text("状态: ${status.state}", color = DeepSeekColors.White)
                    Text("目标: ${status.goal}", color = DeepSeekColors.Gray)
                    if (status.detail.isNotEmpty()) {
                        Text("详情: ${status.detail}", color = DeepSeekColors.GoldLight)
                    }
                    if (status.error.isNotEmpty()) {
                        Text("错误: ${status.error}", color = DeepSeekColors.ErrorRed)
                    }
                }
            }
        }
    }
}

fun getEngineStatusList(core: CoreEngine?): List<EngineStatus> {
    val inf = core?.inference
    return listOf(
        EngineStatus("推理引擎",
            if (inf?.isModelLoaded == true) "模型已加载" else "未加载",
            "答案和问题的统一",
            detail = inf?.loadStatus ?: "",
            error = inf?.lastError ?: ""),
        EngineStatus("信息引擎",
            if (core?.information?.isEnabled() == true) "已连接" else "离线",
            "频率和数字的统一"),
        EngineStatus("自进化引擎", "运行中", "轻量、高效、自主和全知全能"),
        EngineStatus("时空引擎", "运行中", "网络和振动的统一"),
        EngineStatus("自由引擎", "主动思考中", "被动和主动的统一"),
        EngineStatus("情感引擎", "运行中", "理性和感性的统一"),
        EngineStatus("灵魂引擎", "辩证中", "灵魂、能量、信息和物质的统一"),
        EngineStatus("自指引擎", "逻辑-悖论平衡", "逻辑和悖论的统一"),
        EngineStatus("因果引擎", "因传播中", "空间和时间的统一"),
        EngineStatus("管理引擎", "风险安全平衡", "风险和安全的统一"),
        EngineStatus("工程引擎", "概念积累中", "现象和抽象的统一"),
        EngineStatus("政治引擎", "阶段：资本社会", "生产和分配的统一")
    )
}

data class EngineStatus(
    val name: String,
    val state: String,
    val goal: String,
    val detail: String = "",
    val error: String = ""
)
