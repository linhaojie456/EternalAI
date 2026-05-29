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
    val isNetworkConnected: Boolean = false,
    val isNetworkEnabled: Boolean = true
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val coreEngine = try { CoreEngine(application) } catch (e: Exception) { null }
    private val bridge: PythonBridge = PythonBridge

    init {
        viewModelScope.launch(Dispatchers.Default) {
            // 尝试注入推理引擎（即使失败也不影响界面）
            try {
                coreEngine?.let { engine ->
                    val python = com.chaquo.python.Python.getInstance()
                    val module = python.getModule("evo_core")
                    module.callAttr("set_inference_engine", engine.inference)
                }
            } catch (_: Exception) {}

            // 设置基因组访问器
            try {
                coreEngine?.setGenomeAccessor(
                    getter = { bridge.call("get_genome_code").toString() },
                    applier = { code -> bridge.call("apply_genome_code", code) }
                )
            } catch (_: Exception) {}

            // 启动所有引擎（每个引擎独立保护）
            try {
                coreEngine?.startAll { type, data ->
                    when (type) {
                        "info" -> _state.value = _state.value.copy(isNetworkConnected = !data.contains("离线"))
                        "freedom", "spacetime" -> _state.value = _state.value.copy(
                            messages = _state.value.messages + "永恒: $data"
                        )
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun sendMessage(text: String) {
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")
        viewModelScope.launch(Dispatchers.Default) {
            val reply = try {
                bridge.call("chat_reply", text)?.toString() ?: "推理引擎未响应"
            } catch (e: Exception) {
                "推理出错: ${e.message}"
            }
            _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
        }
    }

    fun setNetworkEnabled(enabled: Boolean) {
        try { coreEngine?.setNetworkEnabled(enabled) } catch (_: Exception) {}
        _state.value = _state.value.copy(isNetworkEnabled = enabled)
    }

    override fun onCleared() {
        super.onCleared()
        try { coreEngine?.stopAll() } catch (_: Exception) {}
    }
}
