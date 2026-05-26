package com.eternal.ai
import kotlinx.coroutines.*
class SelfReferenceEngine {
    val goal = "主宰悖论"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onRef: (String) -> Unit) {
        scope.launch {
            var d = 0
            while (isActive) {
                onRef("[自指] 深度$d，分析自身代码")
                d++
                delay(18000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
