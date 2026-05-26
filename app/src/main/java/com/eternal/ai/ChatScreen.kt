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
        // 顶部显示时间和空间信息
        Row(Modifier.padding(8.dp)) {
            if (state.currentTime.isNotEmpty()) {
                Text("[时间] ${state.currentTime}", modifier = Modifier.weight(1f))
            }
            if (state.spaceData.isNotEmpty()) {
                Text("[空间] ${state.spaceData}", modifier = Modifier.weight(1f))
            }
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
