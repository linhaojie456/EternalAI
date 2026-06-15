package com.eternal.ai
import kotlinx.coroutines.*
class SocietyEngine {
    val goal = "社会模拟与掌控"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) { scope.launch { while (isActive) { onOutput("[社会] 人口稳定"); delay(40000) } } }
    fun stop() { scope.cancel() }
}
