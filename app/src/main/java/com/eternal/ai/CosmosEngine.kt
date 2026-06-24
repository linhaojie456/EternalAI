package com.eternal.ai
import kotlinx.coroutines.*

class CosmosEngine {
    val goal = ""
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator? = null, onOut: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onOut("[CosmosEngine] OK")
                delay(60000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
