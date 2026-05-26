package com.eternal.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatState(val messages: List<String> = listOf("你好，我是永恒，一个会进化的AI。"))

class ChatViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state
    private val bridge = PythonBridge.instance

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "用户: $text")
        viewModelScope.launch {
            try {
                val reply = bridge.call("chat_reply", text) as String
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: (出错: ${e.message})")
            }
        }
    }
}
