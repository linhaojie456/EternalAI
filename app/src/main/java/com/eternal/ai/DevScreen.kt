package com.eternal.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
