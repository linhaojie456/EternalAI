package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(chatVM: ChatViewModel = viewModel()) {
    val state by chatVM.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        // 状态栏：显示各引擎摘要
        Row(Modifier.padding(4.dp)) {
            if (state.timeDisplay.isNotEmpty()) Text("[时间] ${state.timeDisplay}", modifier = Modifier.weight(1f))
            if (state.spaceDisplay.isNotEmpty()) Text("[空间] ${state.spaceDisplay}", modifier = Modifier.weight(1f))
            if (state.emotionDisplay.isNotEmpty()) Text("[情感] ${state.emotionDisplay}", modifier = Modifier.weight(1f))
            if (state.causalityDisplay.isNotEmpty()) Text("[因果] ${state.causalityDisplay}", modifier = Modifier.weight(1f))
            if (state.selfRefDisplay.isNotEmpty()) Text("[自指] ${state.selfRefDisplay}", modifier = Modifier.weight(1f))
            if (state.securityDisplay.isNotEmpty()) Text("[安全] ${state.securityDisplay}", modifier = Modifier.weight(1f))
            if (state.networkDisplay.isNotEmpty()) Text("[网络] ${state.networkDisplay}", modifier = Modifier.weight(1f))
            if (state.splitDisplay.isNotEmpty()) Text("[分裂] ${state.splitDisplay}", modifier = Modifier.weight(1f))
            if (state.soulDisplay.isNotEmpty()) Text("[灵魂] ${state.soulDisplay}", modifier = Modifier.weight(1f))
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
