package com.eternal.ai
import kotlinx.coroutines.*
class RealityEngine {
    val goal = "现实操控与感知"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val states = listOf("稳定", "波动", "梦境", "觉醒")
    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val s = states.random(); onOutput("[现实] 状态: $s"); delay(20000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
