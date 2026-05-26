package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<String> = listOf("你好，我是永恒。"),
    val isNetworkConnected: Boolean = false,
    val isNetworkEnabled: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    
    private val inferenceEngine = InferenceEngine(application)

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val python = Python.getInstance()
            val module = python.getModule("evo_core")
            module.callAttr("set_inference_engine", inferenceEngine)
        }
    }

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val python = Python.getInstance()
                val module = python.getModule("evo_core")
                val reply = module.callAttr("chat_reply", text).toString()
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 推理失败: ${e.message}")
            }
        }
    }

    fun setNetworkEnabled(enabled: Boolean) {
        _state.value = _state.value.copy(isNetworkEnabled = enabled)
    }
}
