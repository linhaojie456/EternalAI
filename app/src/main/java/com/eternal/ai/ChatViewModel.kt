package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eternal.ai.data.AppDatabase
import com.eternal.ai.data.ChatMessage
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
    private val dao = AppDatabase.getInstance(application).messageDao()
    private val coreEngine = try { CoreEngine(application) } catch (e: Exception) { null }
    private val bridge = PythonBridge

    init {
        // 从数据库恢复聊天记录
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllChatMessages().collect { dbMessages ->
                if (dbMessages.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        messages = dbMessages.map { "${it.sender}: ${it.content}" }
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                coreEngine?.let { engine ->
                    val python = com.chaquo.python.Python.getInstance()
                    val module = python.getModule("evo_core")
                    module.callAttr("set_inference_engine", engine.inference)
                }
            } catch (_: Exception) {}

            try {
                coreEngine?.setGenomeAccessor(
                    getter = { bridge.call("get_genome_code").toString() },
                    applier = { code -> bridge.call("apply_genome_code", code) }
                )
            } catch (_: Exception) {}

            try {
                coreEngine?.startAll { type, data ->
                    when (type) {
                        "info" -> _state.value = _state.value.copy(isNetworkConnected = !data.contains("离线"))
                        "proactive", "freedom" -> {
                            // 主动引擎产生的对话直接存入数据库
                            viewModelScope.launch(Dispatchers.IO) {
                                dao.insertMessage(ChatMessage(sender = "永恒", content = data))
                            }
                            _state.value = _state.value.copy(
                                messages = _state.value.messages + "永恒: $data"
                            )
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertMessage(ChatMessage(sender = "造物主", content = text))
        }
        _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text")

        viewModelScope.launch(Dispatchers.Default) {
            val reply = try {
                bridge.call("chat_reply", text)?.toString() ?: "推理引擎未响应"
            } catch (e: Exception) {
                "推理出错: ${e.message}"
            }
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertMessage(ChatMessage(sender = "永恒", content = reply))
            }
            _state.value = _state.value.copy(messages = _state.value.messages + "永恒: $reply")
        }
    }

    fun setNetworkEnabled(enabled: Boolean) {
        coreEngine?.setNetworkEnabled(enabled)
        _state.value = _state.value.copy(isNetworkEnabled = enabled)
    }

    override fun onCleared() {
        super.onCleared()
        try { coreEngine?.stopAll() } catch (_: Exception) {}
    }
}
