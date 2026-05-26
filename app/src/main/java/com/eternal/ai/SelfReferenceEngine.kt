package com.eternal.ai

import kotlinx.coroutines.*

class SelfReferenceEngine {
    val goal = "主宰悖论"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator, onRef: (String) -> Unit) {
        scope.launch {
            var depth = 0
            while (isActive) {
                onRef("[自指] 目标：$goal | 递归深度：$depth | 自我审视中")
                depth++
                delay(15000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
