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
    val messages: List<String> = listOf("你好，我是永恒，一个会进化的AI。"),
    val currentTime: String = "",
    val spaceData: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private var engine: EternalInference? = null
    private var proactiveEngine: ProactiveEngine? = null
    private var timeEngine: TimeEngine? = null
    private var spaceEngine: SpaceEngine? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                engine = EternalInference.create(getApplication())
            } catch (_: Exception) {}
        }

        // 启动时间引擎
        timeEngine = TimeEngine { time ->
            _state.value = _state.value.copy(currentTime = time)
        }.also { it.start() }

        // 启动空间引擎
        spaceEngine = SpaceEngine { data ->
            _state.value = _state.value.copy(spaceData = data)
        }.also { it.start() }

        // 启动主动引擎（直接添加消息到聊天记录）
        proactiveEngine = ProactiveEngine(getApplication()) { msg ->
            _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $msg")
        }.also { it.start() }
    }

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")
        viewModelScope.launch(Dispatchers.Default) {
            if (engine == null) {
                try {
                    engine = EternalInference.create(getApplication())
                } catch (e: Exception) {
                    _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 引擎初始化失败: ${e.message}")
                    return@launch
                }
            }
            try {
                val reply = engine!!.generate(text)
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 回复生成失败: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        proactiveEngine?.stop()
        timeEngine?.stop()
        spaceEngine?.stop()
    }
}
