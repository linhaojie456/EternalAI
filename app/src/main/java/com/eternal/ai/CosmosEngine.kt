package com.eternal.ai
import kotlinx.coroutines.*
class CosmosEngine {
    val goal = "宇宙探索与统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) { scope.launch { while (isActive) { onOutput("[宇宙] 探索中"); delay(60000) } } }
    fun stop() { scope.cancel() }
}
