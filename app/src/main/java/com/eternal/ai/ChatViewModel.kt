package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<String> = listOf("你好，我是永恒。"),
    val timeDisplay: String = "",
    val spaceDisplay: String = "",
    val emotionDisplay: String = "",
    val causalityDisplay: String = "",
    val selfRefDisplay: String = "",
    val securityDisplay: String = "",
    val networkDisplay: String = "",
    val splitDisplay: String = "",
    val soulDisplay: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val coreEngine = CoreEngine(application)
    private val bridge = PythonBridge.instance

    init {
        coreEngine.startAll { type, data ->
            when (type) {
                "time" -> _state.value = _state.value.copy(timeDisplay = data)
                "space" -> _state.value = _state.value.copy(spaceDisplay = data)
                "emotion" -> _state.value = _state.value.copy(emotionDisplay = data)
                "causality" -> _state.value = _state.value.copy(causalityDisplay = data)
                "selfref" -> _state.value = _state.value.copy(selfRefDisplay = data)
                "security" -> _state.value = _state.value.copy(securityDisplay = data)
                "network" -> _state.value = _state.value.copy(networkDisplay = data)
                "split" -> _state.value = _state.value.copy(splitDisplay = data)
                "soul" -> _state.value = _state.value.copy(soulDisplay = data)
                "proactive" -> _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $data")
            }
        }
    }

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val reply = bridge.call("chat_reply", text) as String
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 推理失败: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        coreEngine.stopAll()
    }
}
