package com.eternal.ai
import kotlinx.coroutines.*

class SelfReferenceEngine {
    val goal = ""
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator? = null, onOut: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onOut("[SelfReferenceEngine] OK")
                delay(60000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
