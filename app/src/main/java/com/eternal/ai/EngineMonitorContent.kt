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
