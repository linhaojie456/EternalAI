package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chaquo.python.PyObject
import com.eternal.ai.data.AppDatabase
import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DevState(
    val genomeCode: String = "",
    val devMessages: List<String> = emptyList()
)

class DevViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(DevState())
    val state: StateFlow<DevState> = _state.asStateFlow()
    private val dao = AppDatabase.getInstance(application).messageDao()
    private val bridge = PythonBridge

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllDevMessages().collect { dbMessages ->
                if (dbMessages.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        devMessages = dbMessages.map { "${it.sender}: ${it.content}" }
                    )
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val code = bridge.call("get_genome_code").toString()
                if (code != "null" && code.isNotEmpty()) {
                    _state.value = _state.value.copy(genomeCode = code)
                }
            } catch (_: Exception) {}
        }
    }

    fun updateGenomeCode(newCode: String) {
        _state.value = _state.value.copy(genomeCode = newCode)
    }

    fun sendDevCommand(cmd: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertMessage(ChatMessage(sender = "造物主", content = cmd, isDevMode = true))
        }
        _state.value = _state.value.copy(devMessages = _state.value.devMessages + "造物主: $cmd")

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val result = bridge.call("generate_code_from_chat", cmd, _state.value.genomeCode)
                val resultList = (result as? PyObject)?.asList() ?: listOf(_state.value.genomeCode, "未知结果")
                val newCode = resultList.getOrNull(0)?.toString() ?: _state.value.genomeCode
                val errorMsg = resultList.getOrNull(1)?.toString()

                if (errorMsg != null && errorMsg != "None" && errorMsg.isNotEmpty()) {
                    _state.value = _state.value.copy(
                        devMessages = _state.value.devMessages + "永恒: 生成失败: $errorMsg"
                    )
                } else {
                    viewModelScope.launch(Dispatchers.IO) {
                        dao.insertMessage(ChatMessage(sender = "永恒", content = "代码已生成", isDevMode = true))
                    }
                    _state.value = _state.value.copy(
                        genomeCode = newCode,
                        devMessages = _state.value.devMessages + "永恒: 代码已生成，请查看上方编辑器并应用"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    devMessages = _state.value.devMessages + "永恒: 生成异常: ${e.message}"
                )
            }
        }
    }

    fun applyGenomeCode() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                bridge.call("apply_genome_code", _state.value.genomeCode)
                _state.value = _state.value.copy(devMessages = _state.value.devMessages + "永恒: 基因组已应用")
            } catch (e: Exception) {
                _state.value = _state.value.copy(devMessages = _state.value.devMessages + "永恒: 应用失败: ${e.message}")
            }
        }
    }
}
