package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(chatVM: ChatViewModel = viewModel(), onShowMonitor: () -> Unit = {}) {
    val state by chatVM.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 自动滚动到底部
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // 顶部时空显示（来自 SpacetimeEngine 的输出会被过滤显示）
        // 注意：这里需要从 ViewModel 获取时空数据，但我们可以在 ChatViewModel 中增加 state 字段
        // 由于时间有限，我们简单地在界面顶部放置一个按钮进入后台模式
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("网络: ${if (state.isNetworkConnected) "已连接" else "离线"}")
            Switch(
                checked = state.isNetworkEnabled,
                onCheckedChange = { chatVM.setNetworkEnabled(it) }
            )
            Button(onClick = onShowMonitor) { Text("引擎监控") }
        }
        Divider()
        LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
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
