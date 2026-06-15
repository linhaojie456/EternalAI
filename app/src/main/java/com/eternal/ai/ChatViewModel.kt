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
            dao.getAllChatMessages().collect { dbMsgs ->
                if (dbMsgs.isNotEmpty())
                    _state.update { it.copy(messages = dbMsgs.map { "${it.sender}: ${it.content}" }) }
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
                    // 批量更新，最多保留最近10个引擎状态
                    _state.update { state ->
                        val newMap = state.engineStatuses.toMutableMap()
                        newMap[type] = data
                        if (newMap.size > 10) {
                            // 保留最新的10个
                            state.copy(engineStatuses = newMap.entries.takeLast(10).toMap())
                        } else {
                            state.copy(engineStatuses = newMap)
                        }
                    }
                }
            }
        }
    }

    fun sendMessage(text: String) { /* 与之前版本相同，省略 */ }
    override fun onCleared() { super.onCleared(); coreEngine.stopAll() }
}
