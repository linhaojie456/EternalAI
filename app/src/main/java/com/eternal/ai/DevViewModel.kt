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

data class DevState(
    val genomeCode: String = "",
    val devMessages: List<String> = emptyList()
)

class DevViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(DevState())
    val state: StateFlow<DevState> = _state.asStateFlow()

    private var engine: EternalInference? = null

    init {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                engine = EternalInference.create(getApplication())
            } catch (_: Exception) {}
            // 加载基因组
            val bridge = PythonBridge.instance
            try {
                val code = bridge.call("get_genome_code").toString()
                _state.update { it.copy(genomeCode = code) }
            } catch (_: Exception) {}
        }
    }

    fun updateGenomeCode(newCode: String) {
        _state.update { it.copy(genomeCode = newCode) }
    }

    fun sendDevCommand(cmd: String) {
        _state.update { it.copy(devMessages = it.devMessages + "造物主: $cmd") }
        viewModelScope.launch(Dispatchers.Default) {
            if (engine == null) {
                try {
                    engine = EternalInference.create(getApplication())
                } catch (e: Exception) {
                    _state.update { it.copy(devMessages = it.devMessages + "永恒: 引擎不可用: ${e.message}") }
                    return@launch
                }
            }

            // 让模型生成新代码
            val currentCode = _state.value.genomeCode
            val prompt = "修改以下Python模型定义以满足需求：\n$currentCode\n需求：$cmd\n只返回代码。"
            try {
                val newCode = engine!!.generate(prompt, maxTokens = 500)
                _state.update {
                    it.copy(
                        genomeCode = newCode,
                        devMessages = it.devMessages + "永恒: 代码已生成，请查看上方编辑器并应用"
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(devMessages = it.devMessages + "永恒: 生成失败: ${e.message}") }
            }
        }
    }

    fun applyGenomeCode() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                PythonBridge.instance.call("apply_genome_code", _state.value.genomeCode)
                _state.update { it.copy(devMessages = it.devMessages + "永恒: 基因组已应用") }
            } catch (e: Exception) {
                _state.update { it.copy(devMessages = it.devMessages + "永恒: 应用失败: ${e.message}") }
            }
        }
    }
}
