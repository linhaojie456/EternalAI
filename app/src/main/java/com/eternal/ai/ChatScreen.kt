package com.eternal.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
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
            // 消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                itemsIndexed(state.messages) { _, msg ->
                    val isUser = msg.startsWith("造物主:")
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            color = if (isUser) DeepSeekColors.UserBubble else DeepSeekColors.AiBubble,
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp,
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
                                    modifier = Modifier.padding(16.dp),
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            // 输入栏（立体感增强）
            Surface(
                color = DeepSeekColors.Surface,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    var input by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = input, onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("与永恒对话...", color = DeepSeekColors.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepSeekColors.White,
                            unfocusedTextColor = DeepSeekColors.White,
                            focusedBorderColor = DeepSeekColors.Gold,
                            unfocusedBorderColor = DeepSeekColors.CardBorder,
                            cursorColor = DeepSeekColors.Gold
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { chatVM.sendMessage(input); input = "" },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepSeekColors.Gold),
                        shape = RoundedCornerShape(24.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text("发送", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
