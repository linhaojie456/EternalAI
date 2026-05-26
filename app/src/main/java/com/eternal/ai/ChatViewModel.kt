package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

    private var engine: EternalInference? = null

    init {
        // 后台初始化引擎
        viewModelScope.launch(Dispatchers.Default) {
            try {
                engine = EternalInference.create(getApplication())
            } catch (e: Exception) {
                // 引擎初始化失败会在第一次发消息时报告
            }
        }
    }

    fun sendMessage(text: String) {
        _state.update { it.copy(messages = it.messages + "造物主: $text") }

        viewModelScope.launch(Dispatchers.Default) {
            if (engine == null) {
                // 尝试重新初始化
                try {
                    engine = EternalInference.create(getApplication())
                } catch (e: Exception) {
                    _state.update { it.copy(messages = it.messages + "永恒: 引擎初始化失败: ${e.message}") }
                    return@launch
                }
            }

            try {
                val reply = engine!!.generate(text)
                _state.update { it.copy(messages = it.messages + "永恒: $reply") }
            } catch (e: Exception) {
                _state.update { it.copy(messages = it.messages + "永恒: 回复生成失败: ${e.message}") }
            }
        }
    }
}
