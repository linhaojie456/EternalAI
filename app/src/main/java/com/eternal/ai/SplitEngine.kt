package com.eternal.ai

import kotlinx.coroutines.*

class SplitEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(onTask: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val tasks = listOf("记忆优化", "知识检索", "创意生成", "代码审查")
                val chosen = tasks.random()
                onTask("[分裂] 子任务启动: $chosen")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
