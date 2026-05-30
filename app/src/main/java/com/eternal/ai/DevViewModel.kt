package com.eternal.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    private val bridge = PythonBridge

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val code = bridge.call("get_genome_code").toString()
                _state.value = _state.value.copy(genomeCode = code)
            } catch (_: Exception) {}
        }
    }

    fun updateGenomeCode(newCode: String) {
        _state.value = _state.value.copy(genomeCode = newCode)
    }

    fun sendDevCommand(cmd: String) {
        _state.value = _state.value.copy(devMessages = _state.value.devMessages + "造物主: $cmd")
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val newCode = bridge.call("generate_code_from_chat", cmd, _state.value.genomeCode).toString()
                _state.value = _state.value.copy(
                    genomeCode = newCode,
                    devMessages = _state.value.devMessages + "永恒: 代码已生成，请查看上方编辑器并应用"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    devMessages = _state.value.devMessages + "永恒: 生成失败: ${e.message}"
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
