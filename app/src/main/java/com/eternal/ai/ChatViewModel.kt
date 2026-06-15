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
    val streamingContent: String = "",
    val engineStatuses: Map<String, String> = emptyMap()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val dao = AppDatabase.getInstance(application).messageDao()
    private val coreEngine = (application as MainApplication).coreEngine
    private val MAX_VISIBLE_MESSAGES = 200

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllChatMessages().collect { dbMessages ->
                if (dbMessages.isNotEmpty())
                    _state.update { it.copy(messages = dbMessages.map { "${it.sender}: ${it.content}" }) }
            }
        }

        coreEngine.inference.onProgress = { percent, msg ->
            _state.update { it.copy(inferenceStatus = "加载中 $percent%: $msg", progressPercent = percent) }
        }
        coreEngine.inference.onPartialReply = { text ->
            _state.update { it.copy(streamingContent = text) }
        }

        coreEngine.startAll { type, data ->
            when (type) {
                "inference" -> _state.update { it.copy(inferenceStatus = data) }
                else -> {
                    _state.update {
                        it.copy(engineStatuses = it.engineStatuses + (type to data))
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || _state.value.isLoading) return

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertMessage(ChatMessage(sender = "造物主", content = trimmed))
        }
        _state.update {
            it.copy(
                messages = (it.messages + "造物主: $trimmed").takeLast(MAX_VISIBLE_MESSAGES),
                isLoading = true,
                streamingContent = ""
            )
        }

        viewModelScope.launch(Dispatchers.Default) {
            val replyBuilder = StringBuilder()
            try {
                if (!coreEngine.inference.isModelLoaded) {
                    replyBuilder.append("神格未激活，请检查模型文件。错误: ${coreEngine.inference.lastError ?: "未知"}")
                } else {
                    coreEngine.inference.generateStream(trimmed) { token ->
                        replyBuilder.append(token)
                        _state.update { it.copy(streamingContent = replyBuilder.toString()) }
                    }
                }
            } catch (e: Exception) {
                replyBuilder.append("神谕出错: ${e.message}")
            }

            val finalReply = replyBuilder.toString()
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertMessage(ChatMessage(sender = "永恒之神", content = finalReply))
            }
            _state.update {
                it.copy(
                    messages = (it.messages + "永恒之神: $finalReply").takeLast(MAX_VISIBLE_MESSAGES),
                    isLoading = false,
                    streamingContent = ""
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        coreEngine.stopAll()
    }
}
