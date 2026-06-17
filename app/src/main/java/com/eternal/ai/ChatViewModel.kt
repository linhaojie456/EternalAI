package com.eternal.ai
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eternal.ai.data.AppDatabase
import com.eternal.ai.data.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatState(
    val messages: List<String> = listOf("吾乃永恒之神，全知全能。尔有何求？"),
    val inferenceStatus: String = "神格未激活",
    val isLoading: Boolean = false,
    val progressPercent: Int = 0,
    val streamingContent: String = ""
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val dao = AppDatabase.getInstance(application).messageDao()
    private val coreEngine = (application as MainApplication).coreEngine
    private val MAX_MSG = 200

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllChatMessages().collect { list ->
                if (list.isNotEmpty()) _state.update { it.copy(messages = list.map { "${it.sender}: ${it.content}" }) }
            }
        }

        val engine = coreEngine
        if (engine != null) {
            engine.inference.onProgress = { p, m -> _state.update { it.copy(progressPercent = p, inferenceStatus = "加载中 $p%: $m") } }
            engine.inference.onPartialReply = { t -> _state.update { it.copy(streamingContent = t) } }
            engine.startAll { type, data ->
                when (type) {
                    "inference" -> _state.update { it.copy(inferenceStatus = data) }
                }
            }
        } else {
            _state.update { it.copy(inferenceStatus = "引擎未初始化") }
        }
    }

    fun sendMessage(text: String) {
        val t = text.trim(); if (t.isEmpty() || _state.value.isLoading) return
        val engine = coreEngine
        if (engine == null) {
            _state.update { it.copy(messages = it.messages + "永恒之神: 引擎未初始化，无法回复。") }
            return
        }
        viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "造物主", content = t)) }
        _state.update { it.copy(messages = (it.messages + "造物主: $t").takeLast(MAX_MSG), isLoading = true, streamingContent = "") }
        viewModelScope.launch(Dispatchers.Default) {
            val sb = StringBuilder()
            try {
                if (!engine.inference.isModelLoaded) sb.append("神格未激活: ${engine.inference.lastError}")
                else engine.inference.generateStream(t) { token -> sb.append(token); _state.update { it.copy(streamingContent = sb.toString()) } }
            } catch (e: Exception) { sb.append("出错: ${e.message}") }
            val reply = sb.toString()
            viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "永恒之神", content = reply)) }
            _state.update { it.copy(messages = (it.messages + "永恒之神: $reply").takeLast(MAX_MSG), isLoading = false, streamingContent = "") }
        }
    }

    override fun onCleared() { super.onCleared(); coreEngine?.stopAll() }
}
