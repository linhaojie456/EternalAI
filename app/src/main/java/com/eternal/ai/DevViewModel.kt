package com.eternal.ai
import androidx.lifecycle.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DevState(
    val genomeCode: String = "",
    val devMessages: List<String> = emptyList()
)

class DevViewModel : ViewModel() {
    private val _state = MutableStateFlow(DevState())
    val state: StateFlow<DevState> = _state
    private val bridge = PythonBridge.instance

    init {
        viewModelScope.launch {
            val code = bridge.call("get_genome_code") as String
            _state.value = _state.value.copy(genomeCode = code)
        }
    }

    fun updateGenomeCode(newCode: String) { _state.value = _state.value.copy(genomeCode = newCode) }

    fun sendDevCommand(cmd: String) {
        _state.value = _state.value.copy(devMessages = _state.value.devMessages + "用户: $cmd")
        viewModelScope.launch {
            val newCode = bridge.call("generate_code_from_chat", cmd, _state.value.genomeCode) as String
            _state.value = _state.value.copy(
                genomeCode = newCode,
                devMessages = _state.value.devMessages + "永恒: 代码已生成，请查看上方编辑器并应用"
            )
        }
    }

    fun applyGenomeCode() {
        viewModelScope.launch { bridge.call("apply_genome_code", _state.value.genomeCode) }
    }
}
