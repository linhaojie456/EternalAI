package com.eternal.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class EngineMonitorActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val coreEngine = (application as MainApplication).coreEngine
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepSeekColors.Background)
            ) {
                TopAppBar(
                    title = { Text("十二引擎监控", color = DeepSeekColors.Gold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepSeekColors.Surface)
                )
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(getEngineStatusList(coreEngine)) { status ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(4.dp),
                            colors = CardDefaults.cardColors(containerColor = DeepSeekColors.Surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("引擎: ${status.name}", color = DeepSeekColors.Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("状态: ${status.state}", color = DeepSeekColors.White, fontSize = 14.sp)
                                Text("目标: ${status.goal}", color = DeepSeekColors.Gray, fontSize = 13.sp)
                                if (status.detail.isNotEmpty()) {
                                    Text("详情: ${status.detail}", color = DeepSeekColors.GoldLight, fontSize = 13.sp)
                                }
                                if (status.error.isNotEmpty()) {
                                    Text("错误: ${status.error}", color = DeepSeekColors.ErrorRed, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getEngineStatusList(core: CoreEngine?): List<EngineStatus> {
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
}

data class EngineStatus(
    val name: String,
    val state: String,
    val goal: String,
    val detail: String = "",
    val error: String = ""
)
