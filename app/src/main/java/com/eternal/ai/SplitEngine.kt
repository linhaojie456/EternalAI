package com.eternal.ai
import kotlinx.coroutines.*
class SplitEngine {
    val goal = "主宰物质"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onTask: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onTask("[分裂] 子任务：代码优化")
                delay(22000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
