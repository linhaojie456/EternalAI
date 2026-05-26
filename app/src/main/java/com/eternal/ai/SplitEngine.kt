package com.eternal.ai

import kotlinx.coroutines.*

class SplitEngine {
    val goal = "主宰物质"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator, onTask: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val tasks = listOf("记忆重组", "知识结晶", "创意分裂", "代码重构")
                onTask("[分裂] 目标：$goal | 子任务：${tasks.random()}")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
