package com.eternal.ai
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ConversationContext {
    private val _messages = MutableStateFlow(mutableListOf("你好，我是永恒。"))
    val messages: StateFlow<List<String>> = _messages

    fun add(msg: String) {
        _messages.value = (_messages.value + msg).toMutableList()
    }

    fun getRecent(n: Int = 10): List<String> {
        return _messages.value.takeLast(n)
    }

    fun getHistory(): String {
        return _messages.value.joinToString("\n")
    }
}
