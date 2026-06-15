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
                val state = states.random()
                val vibration = Random.nextFloat()
                onOutput("[现实] 状态:$state 振动强度:${"%.2f".format(vibration)}")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
