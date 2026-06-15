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
    val progressPercent: Int = 0
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()
    private val dao = AppDatabase.getInstance(application).messageDao()
    private val coreEngine = (application as MainApplication).coreEngine
    private val MAX_VISIBLE_MESSAGES = 100

    init {
        viewModelScope.launch(Dispatchers.IO) {
            dao.getAllChatMessages().collect { dbMessages ->
                if (dbMessages.isNotEmpty())
                    _state.value = _state.value.copy(messages = dbMessages.map { "${it.sender}: ${it.content}" })
            }
        }

        // 监听模型加载进度
        coreEngine.inference.onProgress = { percent, msg ->
            _state.value = _state.value.copy(
                inferenceStatus = "加载中 $percent%: $msg",
                progressPercent = percent
            )
        }

        coreEngine.startAll { type, data ->
            when (type) {
                "inference" -> _state.value = _state.value.copy(inferenceStatus = data)
                "freedom" -> {
                    val newMessages = (_state.value.messages + "永恒之神: $data").takeLast(MAX_VISIBLE_MESSAGES)
                    _state.value = _state.value.copy(messages = newMessages)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            dao.insertMessage(ChatMessage(sender = "造物主", content = trimmed))
        }
        _state.value = _state.value.copy(
            messages = (_state.value.messages + "造物主: $trimmed").takeLast(MAX_VISIBLE_MESSAGES),
            isLoading = true
        )

        viewModelScope.launch(Dispatchers.Default) {
            val reply = try {
                if (!coreEngine.inference.isModelLoaded) {
                    "神格未激活，请检查模型文件。错误: ${coreEngine.inference.lastError ?: "未知"}"
                } else {
                    coreEngine.inference.generate(trimmed)
                }
            } catch (e: Exception) {
                "神谕出错: ${e.message}"
            }

            viewModelScope.launch(Dispatchers.IO) {
                dao.insertMessage(ChatMessage(sender = "永恒之神", content = reply))
            }
            _state.value = _state.value.copy(
                messages = (_state.value.messages + "永恒之神: $reply").takeLast(MAX_VISIBLE_MESSAGES),
                isLoading = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        coreEngine.stopAll()
    }
}
