package com.eternal.ai
import kotlinx.coroutines.*
class SoulEngine {
    val goal = "主宰能量"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onSoul: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val step = listOf("灵魂→能量", "能量→信息", "信息→物质").random()
                onSoul("[灵魂] $step")
                delay(25000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
