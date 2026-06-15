package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class EngineeringEngine {
    val goal = "现象和抽象的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var precision = 0.7f

    fun start(coordinator: EngineCoordinator, onTask: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                precision = (precision + Random.nextFloat() * 0.06f - 0.03f).coerceIn(0.4f, 0.99f)
                onTask("[工程] 抽象精度:${"%.2f".format(precision)}")
                delay(25000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
