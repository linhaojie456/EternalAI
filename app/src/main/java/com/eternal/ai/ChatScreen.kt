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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(chatVM: ChatViewModel = viewModel()) {
    val state by chatVM.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepSeekColors.Background)) {
        Column(Modifier.fillMaxSize()) {
            // 顶部栏：包含推理状态和网络开关
            Surface(color = DeepSeekColors.Surface, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("永恒", color = DeepSeekColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    // 推理状态
                    val statusColor = if (state.inferenceStatus.contains("已加载")) DeepSeekColors.Gold else DeepSeekColors.Gray
                    Text("●", color = statusColor, fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(state.inferenceStatus.replace("[推理] ", ""), color = statusColor, fontSize = 12.sp, maxLines = 1)
                    Spacer(Modifier.width(12.dp))
                    // 网络状态与开关
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val networkColor = if (state.isNetworkConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
                        Text(if (state.isNetworkConnected) "已连接" else "离线", color = networkColor, fontSize = 12.sp)
                        Spacer(Modifier.width(4.dp))
                        Switch(
                            checked = state.isNetworkEnabled,
                            onCheckedChange = { chatVM.setNetworkEnabled(it) },
                            modifier = Modifier.height(24.dp),
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = DeepSeekColors.Gold,
                                uncheckedTrackColor = DeepSeekColors.Gray
                            )
                        )
                    }
                }
            }

            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                items(state.messages) { msg ->
                    val isUser = msg.startsWith("造物主:")
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isUser) DeepSeekColors.UserBubble else DeepSeekColors.AiBubble,
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = buildAnnotatedString {
                                        val content = msg.removePrefix(if (isUser) "造物主: " else "永恒: ")
                                        if (isUser) {
                                            withStyle(SpanStyle(color = DeepSeekColors.White)) { append("造物主") }
                                            withStyle(SpanStyle(color = DeepSeekColors.GoldLight)) { append(": ") }
                                            withStyle(SpanStyle(color = DeepSeekColors.White)) { append(content) }
                                        } else {
                                            withStyle(SpanStyle(color = DeepSeekColors.Gold)) { append("永恒") }
                                            withStyle(SpanStyle(color = DeepSeekColors.Gray)) { append(": ") }
                                            withStyle(SpanStyle(color = DeepSeekColors.White)) { append(content) }
                                        }
                                    },
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 15.sp
                                )
                            }
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
                        placeholder = { Text("发送消息...", color = DeepSeekColors.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepSeekColors.White,
                            unfocusedTextColor = DeepSeekColors.White,
                            focusedBorderColor = DeepSeekColors.Gold,
                            unfocusedBorderColor = DeepSeekColors.Gray,
                            cursorColor = DeepSeekColors.Gold
                        ),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { chatVM.sendMessage(input); input = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Gold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("发送", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
