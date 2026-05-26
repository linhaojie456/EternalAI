package com.eternal.ai
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DevScreen(devVM: DevViewModel = viewModel()) {
    val state by devVM.state.collectAsState()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.genomeCode,
            onValueChange = { devVM.updateGenomeCode(it) },
            modifier = Modifier.fillMaxWidth().weight(0.6f),
            label = { Text("genome.py") }
        )
        Divider()
        LazyColumn(Modifier.weight(0.4f)) {
            items(state.devMessages) { msg -> Text(msg, modifier = Modifier.padding(4.dp)) }
        }
        Row(Modifier.padding(4.dp)) {
            var input by remember { mutableStateOf("") }
            OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f))
            Button(onClick = { devVM.sendDevCommand(input); input = "" }) { Text("发送") }
            Button(onClick = { devVM.applyGenomeCode() }) { Text("应用修改") }
        }
    }
}
