package com.eternal.ai
import androidx.compose.foundation.background; import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn; import androidx.compose.foundation.lazy.items; import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.Modifier; import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext; import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.unit.dp; import androidx.compose.ui.unit.sp
import com.eternal.ai.data.AppDatabase; import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.launch; import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current; val dao = remember { AppDatabase.getInstance(context).messageDao() }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }; var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { dao.getAllChatMessages().collect { messages = it } } }

    Column(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack)) {
        Row(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.weight(1f), placeholder = { Text("搜索对话...", color = NeoChineseColors.Gray) }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = NeoChineseColors.RicePaper, unfocusedTextColor = NeoChineseColors.RicePaper, focusedBorderColor = NeoChineseColors.Gold, unfocusedBorderColor = NeoChineseColors.Gray))
            Spacer(Modifier.width(8.dp))
            Button(onClick = { scope.launch(Dispatchers.IO) { if (searchQuery.isNotEmpty()) { messages = messages.filter { it.content.contains(searchQuery, ignoreCase = true) } } else { dao.getAllChatMessages().collect { messages = it } } } }, colors = ButtonDefaults.buttonColors(containerColor = NeoChineseColors.Gold)) { Text("搜索", color = Color.Black, fontWeight = FontWeight.Bold) }
        }
        LazyColumn {
            items(messages) { msg -> Card(modifier = Modifier.fillMaxWidth().padding(4.dp), colors = CardDefaults.cardColors(containerColor = NeoChineseColors.DarkWood), shape = RoundedCornerShape(8.dp)) {
                Row(Modifier.padding(8.dp)) { Column(Modifier.weight(1f)) { Text("${msg.sender}: ${msg.content}", color = NeoChineseColors.RicePaper, fontSize = 14.sp) }
                Button(onClick = { scope.launch(Dispatchers.IO) { dao.deleteMessage(msg); dao.getAllChatMessages().collect { messages = it } } }, colors = ButtonDefaults.buttonColors(containerColor = NeoChineseColors.Cinnabar)) { Text("删除", color = Color.White) } } }
            } }
        }
    }
}
