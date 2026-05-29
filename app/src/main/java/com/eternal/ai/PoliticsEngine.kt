package com.eternal.ai

import kotlinx.coroutines.*

class PoliticsEngine {
    val goal = "生产和分配的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onOutput("[政治] 资源分配优化中，追求公平与效率")
                delay(35000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
