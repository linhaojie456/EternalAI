package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class RealityEngine {
    val goal = "现实操控与感知"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val states = listOf("稳定", "波动", "梦境", "觉醒")

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                onOutput("[现实] ${states.random()}，振动:${"%.2f".format(Random.nextFloat())}")
                delay(30000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
