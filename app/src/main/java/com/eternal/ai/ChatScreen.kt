package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(chatVM: ChatViewModel = viewModel()) {
    val state by chatVM.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        // 网络状态指示器 + 开关
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            val networkColor = if (state.isNetworkConnected) {
                androidx.compose.ui.graphics.Color(0xFF4CAF50)
            } else {
                androidx.compose.ui.graphics.Color(0xFFF44336)
            }
            Text(
                text = if (state.isNetworkConnected) "● 网络已连接" else "● 网络离线",
                color = networkColor,
                modifier = Modifier.padding(end = 8.dp)
            )
            Switch(
                checked = state.isNetworkEnabled,
                onCheckedChange = { chatVM.setNetworkEnabled(it) },
                modifier = Modifier.padding(end = 4.dp)
            )
        }
        Divider()
        LazyColumn(Modifier.weight(1f)) {
            items(state.messages) { msg ->
                SelectionContainer { Text(msg, modifier = Modifier.padding(8.dp)) }
            }
        }
        Row(Modifier.padding(8.dp)) {
            var input by remember { mutableStateOf("") }
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            Button(onClick = { chatVM.sendMessage(input); input = "" }) { Text("发送") }
        }
    }
}
