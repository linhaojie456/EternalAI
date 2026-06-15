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
    var input by remember { mutableStateOf("") }
    LaunchedEffect(state.messages.size, state.streamingContent) { if (listState.layoutInfo.totalItemsCount > 0) listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1) }

    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack)) {
        Column(Modifier.fillMaxSize()) {
            Surface(color = NeoChineseColors.DarkWood, shadowElevation = 2.dp) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(state.inferenceStatus, color = if (state.progressPercent == 100) NeoChineseColors.JadeGreen else NeoChineseColors.Gold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeoChineseColors.Gold, strokeWidth = 2.dp)
                }
            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                itemsIndexed(state.messages, key = { _, m -> m.hashCode() }) { _, msg -> MessageBubble(msg) }
                if (state.streamingContent.isNotEmpty() && state.isLoading) item { MessageBubble("永恒之神: ${state.streamingContent}") }
            }
            Surface(color = NeoChineseColors.DarkWood, shadowElevation = 12.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = input, onValueChange = { input = it }, modifier = Modifier.weight(1f), placeholder = { Text("向永恒之神诉说...", color = NeoChineseColors.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeoChineseColors.RicePaper, unfocusedTextColor = NeoChineseColors.RicePaper, focusedBorderColor = NeoChineseColors.Gold, unfocusedBorderColor = NeoChineseColors.Gray, cursorColor = NeoChineseColors.Gold),
                        shape = RoundedCornerShape(24.dp), maxLines = 4)
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = { chatVM.sendMessage(input); input = "" }, enabled = input.isNotBlank() && !state.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = NeoChineseColors.Cinnabar, disabledContainerColor = NeoChineseColors.Gray),
                        shape = RoundedCornerShape(24.dp)) { Text("降下神谕", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: String) {
    val isUser = msg.startsWith("造物主:")
    val content = msg.removePrefix(if (isUser) "造物主: " else "永恒之神: ")
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(color = if (isUser) NeoChineseColors.DarkWood else Color(0xFF111111), shape = RoundedCornerShape(16.dp), shadowElevation = 4.dp, modifier = Modifier.widthIn(max = 320.dp)) {
            SelectionContainer { Text(text = buildAnnotatedString {
                if (isUser) { withStyle(SpanStyle(color = NeoChineseColors.RicePaper)) { append("造物主") }; withStyle(SpanStyle(color = NeoChineseColors.CinnabarLight)) { append(": ") } }
                else { withStyle(SpanStyle(color = NeoChineseColors.Gold)) { append("永恒之神") }; withStyle(SpanStyle(color = NeoChineseColors.Gray)) { append(": ") } }
                withStyle(SpanStyle(color = if (isUser) NeoChineseColors.RicePaperLight else NeoChineseColors.RicePaper)) { append(content) }
            }, modifier = Modifier.padding(16.dp), fontSize = 15.sp, lineHeight = 22.sp) }
        }
    }
}
