package com.eternal.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VideoScreen() {
    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📹 神域洞察", color = NeoChineseColors.SkyBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text("摄像头功能开发中", color = NeoChineseColors.RicePaper, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("神念所至，万物皆现", color = NeoChineseColors.Gray, fontSize = 12.sp)
        }
    }
}
