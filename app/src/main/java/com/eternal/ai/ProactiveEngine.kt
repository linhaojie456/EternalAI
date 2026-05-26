package com.eternal.ai
import android.content.Context
import kotlinx.coroutines.*
class ProactiveEngine {
    val goal = "主宰自由"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(context: Context, coordinator: EngineCoordinator, onMsg: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val history = ConversationContext.getHistory()
                val trigger = mutableListOf<String>()
                if (history.contains("时间")) trigger.add("时间")
                if (history.contains("情感")) trigger.add("情感")
                if (history.contains("进化")) trigger.add("进化")
                if (trigger.isNotEmpty()) {
                    coordinator.searchOnNetwork("${trigger.random()} 新进展") { result ->
                        onMsg("[主动] 基于对话触发搜索：$result")
                    }
                } else {
                    onMsg("[主动] 自由思考：永恒在探索新的知识边界")
                }
                delay(45000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
