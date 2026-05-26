package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatState(val messages: List<String> = listOf("你好，我是永恒，一个会进化的AI。"))

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state

    private var engine: EternalInference? = null

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")
        viewModelScope.launch {
            if (engine == null) {
                try {
                    engine = withContext(Dispatchers.Default) {
                        EternalInference.create(getApplication())
                    }
                } catch (e: Exception) {
                    _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 引擎启动失败: ${e.message}")
                    return@launch
                }
            }
            try {
                val reply = withContext(Dispatchers.Default) {
                    engine?.generate(text)
                } ?: "错误：模型未加载"
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 回复生成失败: ${e.message}")
            }
        }
    }
}
