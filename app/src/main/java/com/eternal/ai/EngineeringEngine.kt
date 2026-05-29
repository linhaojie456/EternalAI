package com.eternal.ai

import kotlinx.coroutines.*

class EngineeringEngine {
    val goal = "现象和抽象的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator, onTask: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onTask("[工程] 子任务：代码重构 / 架构优化")
                delay(22000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
