package com.eternal.ai
import android.app.Application; import androidx.lifecycle.AndroidViewModel; import androidx.lifecycle.viewModelScope
import com.eternal.ai.data.AppDatabase; import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers; import kotlinx.coroutines.flow.*; import kotlinx.coroutines.launch
data class ChatState(val messages: List<String> = listOf("吾乃永恒之神，全知全能。尔有何求？"), val inferenceStatus: String = "神格未激活")
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState()); val state: StateFlow<ChatState> = _state.asStateFlow()
    private val dao = AppDatabase.getInstance(application).messageDao(); private val coreEngine = (application as MainApplication).coreEngine; private val bridge = PythonBridge
    init {
        viewModelScope.launch(Dispatchers.IO) { dao.getAllChatMessages().collect { dbMessages -> if (dbMessages.isNotEmpty()) _state.value = _state.value.copy(messages = dbMessages.map { "${it.sender}: ${it.content}" }) } }
        viewModelScope.launch(Dispatchers.Default) {
            try { coreEngine.setGenomeAccessor(getter = { bridge.call("get_genome_code").toString() }, applier = { code -> bridge.call("apply_genome_code", code) }) } catch (_: Exception) {}
            try { val python = com.chaquo.python.Python.getInstance(); val module = python.getModule("evo_core"); module.callAttr("set_inference_engine", coreEngine.inference) } catch (_: Exception) {}
            coreEngine.startAll { type, data ->
                when (type) {
                    "inference" -> _state.value = _state.value.copy(inferenceStatus = data)
                    "freedom" -> {
                        // 仅自由引擎的主动思考输出到对话
                        val newMessages = _state.value.messages + "永恒之神: $data"
                        _state.value = _state.value.copy(messages = if (newMessages.size > 100) newMessages.takeLast(50) else newMessages)
                    }
                    // 其他引擎的输出不再添加到对话中，仅在后台运行
                }
            }
        }
    }
    fun sendMessage(text: String) { viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "造物主", content = text)) }; _state.value = _state.value.copy(messages = _state.value.messages + "造物主: $text"); viewModelScope.launch(Dispatchers.Default) { val reply = try { bridge.call("chat_reply", text)?.toString() ?: "神格尚未回应" } catch (e: Exception) { "神谕出错: ${e.message}" }; viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "永恒之神", content = reply)) }; _state.value = _state.value.copy(messages = _state.value.messages + "永恒之神: $reply") } }
    override fun onCleared() { super.onCleared(); coreEngine.stopAll() }
}
