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

    private val coreEngine = CoreEngine(application)
    private val bridge = PythonBridge.instance

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val python = Python.getInstance()
                val module = python.getModule("evo_core")
                module.callAttr("set_inference_engine", coreEngine.inference)
            } catch (e: Exception) {
                _state.value = _state.value.copy(messages = _state.value.messages + "永恒: 推理引擎注入失败: ${e.message}")
            }
        }

        coreEngine.setGenomeAccessor(
            getter = { bridge.call("get_genome_code").toString() },
            applier = { code -> bridge.call("apply_genome_code", code) }
        )

        coreEngine.setNetworkStatusCallback { connected ->
            _state.value = _state.value.copy(isNetworkConnected = connected)
        }

        coreEngine.startAll { type, data ->
            when (type) {
                "info" -> {
                    val connected = !data.contains("离线")
                    _state.value = _state.value.copy(isNetworkConnected = connected)
                }
                "freedom", "spacetime" -> _state.value = _state.value.copy(
                    messages = _state.value.messages + "永恒: $data"
                )
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

    fun setNetworkEnabled(enabled: Boolean) {
        coreEngine.setNetworkEnabled(enabled)
        _state.value = _state.value.copy(isNetworkEnabled = enabled)
    }

    override fun onCleared() {
        super.onCleared()
        coreEngine.stopAll()
    }
}
