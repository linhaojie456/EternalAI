package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.math.*
class SpaceEngine {
    val goal = "主宰空间"
    var currentData: String? = null
        private set
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var angle = 0.0
    fun start(coordinator: EngineCoordinator, onData: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val x = cos(angle).toFloat(); val y = sin(angle).toFloat(); val z = sin(angle * 2).toFloat()
                currentData = "(${"%.1f".format(x)},${"%.1f".format(y)},${"%.1f".format(z)})"
                angle += 0.1
                delay(2000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
