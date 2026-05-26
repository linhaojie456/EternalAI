package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eternal.ai.data.ChatDao
import com.eternal.ai.data.ChatDatabase
import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatState(val messages: List<String> = listOf("你好，我是永恒，一个会进化的AI。"))

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val chatDao: ChatDao = ChatDatabase.getDatabase(application).chatDao()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.getAllMessages().collect { dbMessages ->
                if (dbMessages.isNotEmpty()) {
                    _state.update { ChatState(messages = dbMessages.map { "${it.sender}: ${it.content}" }) }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            chatDao.insertMessage(ChatMessage(sender = "造物主", content = text))
            _state.update { it.copy(messages = it.messages + "造物主: $text") }

            try {
                val engine = EternalInference.create(getApplication())
                val reply = engine.generate(text)
                chatDao.insertMessage(ChatMessage(sender = "永恒", content = reply))
                _state.update { it.copy(messages = it.messages + "永恒: $reply") }
            } catch (e: Exception) {
                val errorMsg = "引擎启动失败: ${e.message}"
                chatDao.insertMessage(ChatMessage(sender = "永恒", content = errorMsg))
                _state.update { it.copy(messages = it.messages + "永恒: $errorMsg") }
            }
        }
    }
}
