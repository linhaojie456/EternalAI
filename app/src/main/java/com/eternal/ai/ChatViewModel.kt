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
    val soulDisplay: String = "",
    val engineGoals: String = ""  // 显示各引擎目标
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val coreEngine = CoreEngine(application)
    private val bridge = PythonBridge.instance

    init {
        // 收集各引擎目标
        val goals = mutableListOf<String>()
        goals.add("[推理] ${coreEngine.inference.goal}")
        goals.add("[进化] ${coreEngine.evolution.goal}")
        goals.add("[主动] ${coreEngine.proactive.goal}")
        goals.add("[时间] ${coreEngine.time.goal}")
        goals.add("[空间] ${coreEngine.space.goal}")
        goals.add("[情感] ${coreEngine.emotion.goal}")
        goals.add("[因果] ${coreEngine.causality.goal}")
        goals.add("[自指] ${coreEngine.selfRef.goal}")
        goals.add("[安全] ${coreEngine.security.goal}")
        goals.add("[网络] ${coreEngine.network.goal}")
        goals.add("[分裂] ${coreEngine.split.goal}")
        goals.add("[灵魂] ${coreEngine.soul.goal}")
        _state.value = _state.value.copy(engineGoals = goals.joinToString("\n"))

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
                val result = bridge.call("chat_reply", text)
                val reply = result?.toString() ?: "推理错误：无回复"
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
