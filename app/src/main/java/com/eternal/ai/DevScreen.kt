package com.eternal.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DevScreen(devVM: DevViewModel = viewModel()) {
    val state by devVM.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.devMessages.size) {
        if (state.devMessages.isNotEmpty()) {
            listState.animateScrollToItem(state.devMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSeekColors.Background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 顶部提示
            Text(
                "genome.py",
                color = DeepSeekColors.Gold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(12.dp),
                fontSize = 18.sp
            )
            // 代码编辑器
            Surface(
                color = DeepSeekColors.Surface,
                modifier = Modifier.fillMaxWidth().weight(0.6f).padding(horizontal = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                OutlinedTextField(
                    value = state.genomeCode,
                    onValueChange = { devVM.updateGenomeCode(it) },
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = DeepSeekColors.White,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepSeekColors.Gold,
                        unfocusedBorderColor = DeepSeekColors.Gray
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            // 开发对话区
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(0.4f).padding(horizontal = 8.dp)
            ) {
                items(state.devMessages) { msg ->
                    Surface(
                        color = DeepSeekColors.Surface,
                        modifier = Modifier.fillMaxWidth().padding(2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        SelectionContainer {
                            Text(msg, color = DeepSeekColors.White, modifier = Modifier.padding(8.dp), fontSize = 13.sp)
                        }
                    }
                }
            }
            // 输入栏
            Surface(color = DeepSeekColors.Surface, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var input by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("输入修改指令...", color = DeepSeekColors.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepSeekColors.White,
                            unfocusedTextColor = DeepSeekColors.White,
                            focusedBorderColor = DeepSeekColors.Gold,
                            unfocusedBorderColor = DeepSeekColors.Gray,
                            cursorColor = DeepSeekColors.Gold
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { devVM.sendDevCommand(input); input = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("修改", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(4.dp))
                    Button(
                        onClick = { devVM.applyGenomeCode() },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("应用", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
