package com.eternal.ai
import android.app.Application
import android.util.Log
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
            Log.d("ChatViewModel", "coreEngine ready")
            engine.inference.onProgress = { p, m -> _state.update { it.copy(progressPercent = p, inferenceStatus = "加载中 $p%: $m") } }
            engine.inference.onPartialReply = { t -> _state.update { it.copy(streamingContent = t) } }
            engine.startAll { type, data ->
                when (type) {
                    "inference" -> _state.update { it.copy(inferenceStatus = data) }
                }
            }
        } else {
            Log.e("ChatViewModel", "coreEngine is null!")
            _state.update { it.copy(inferenceStatus = "引擎未初始化") }
        }
    }

    fun sendMessage(text: String) {
        val t = text.trim()
        if (t.isEmpty() || _state.value.isLoading) {
            Log.d("ChatViewModel", "sendMessage 跳过：empty=$t, isLoading=${_state.value.isLoading}")
            return
        }

        val engine = coreEngine
        if (engine == null) {
            Log.e("ChatViewModel", "sendMessage 失败：coreEngine 为 null")
            _state.update { it.copy(messages = it.messages + "永恒之神: 引擎未初始化，无法回复。") }
            return
        }

        if (!engine.inference.isModelLoaded) {
            Log.e("ChatViewModel", "sendMessage 失败：模型未加载，错误=${engine.inference.lastError}")
            _state.update { it.copy(messages = it.messages + "永恒之神: 神格未激活，错误：${engine.inference.lastError}") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "造物主", content = t)) }
        _state.update { it.copy(messages = (it.messages + "造物主: $t").takeLast(MAX_MSG), isLoading = true, streamingContent = "") }

        Log.d("ChatViewModel", "开始调用 generateStream，prompt=$t")
        viewModelScope.launch(Dispatchers.Default) {
            val sb = StringBuilder()
            try {
                engine.inference.generateStream(t) { token ->
                    sb.append(token)
                    _state.update { it.copy(streamingContent = sb.toString()) }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "generateStream 异常: ${e.message}")
                sb.append("出错: ${e.message}")
            }
            val reply = sb.toString()
            Log.d("ChatViewModel", "generateStream 完成，回复长度=${reply.length}，内容=${reply.take(100)}")
            viewModelScope.launch(Dispatchers.IO) { dao.insertMessage(ChatMessage(sender = "永恒之神", content = reply)) }
            _state.update { it.copy(messages = (it.messages + "永恒之神: $reply").takeLast(MAX_MSG), isLoading = false, streamingContent = "") }
        }
    }

    override fun onCleared() { super.onCleared(); coreEngine?.stopAll() }
}
