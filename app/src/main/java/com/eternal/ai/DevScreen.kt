package com.eternal.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun DevScreen(devVM: DevViewModel = viewModel()) {
    val state by devVM.state.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.devMessages.size) {
        if (state.devMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.devMessages.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.genomeCode,
            onValueChange = { devVM.updateGenomeCode(it) },
            modifier = Modifier.fillMaxWidth().weight(0.6f),
            label = { Text("genome.py") }
        )
        Divider()
        LazyColumn(state = listState, Modifier.weight(0.4f)) {
            items(state.devMessages) { msg ->
                SelectionContainer { Text(msg, modifier = Modifier.padding(4.dp)) }
            }
        }
        Row(Modifier.padding(4.dp)) {
            var input by remember { mutableStateOf("") }
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            Button(onClick = { devVM.sendDevCommand(input); input = "" }) { Text("发送") }
            Button(onClick = { devVM.applyGenomeCode() }) { Text("应用修改") }
        }
    }
}
