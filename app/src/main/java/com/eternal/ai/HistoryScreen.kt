package com.eternal.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eternal.ai.data.AppDatabase
import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val dao = AppDatabase.getInstance(androidx.compose.ui.platform.LocalContext.current).messageDao()
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            dao.getAllChatMessages().collect { dbMessages ->
                messages = dbMessages
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(DeepSeekColors.Background)) {
        Row(modifier = Modifier.padding(8.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("搜索对话...") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DeepSeekColors.White,
                    unfocusedTextColor = DeepSeekColors.White,
                    focusedBorderColor = DeepSeekColors.Gold,
                    unfocusedBorderColor = DeepSeekColors.Gray
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    if (searchQuery.isNotEmpty()) {
                        val filtered = messages.filter { it.content.contains(searchQuery, ignoreCase = true) }
                        messages = filtered
                    } else {
                        dao.getAllChatMessages().collect { messages = it }
                    }
                }
            }) {
                Text("搜索")
            }
        }
        LazyColumn {
            items(messages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSeekColors.Surface)
                ) {
                    Row(Modifier.padding(8.dp)) {
                        Text("${msg.sender}: ${msg.content}", color = DeepSeekColors.White)
                        Spacer(Modifier.weight(1f))
                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.deleteMessage(msg)
                                dao.getAllChatMessages().collect { messages = it }
                            }
                        }) {
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}
