package com.eternal.ai
import kotlinx.coroutines.*
class EmotionEngine {
    val goal = "理性和感性的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) { scope.launch { while (isActive) { onEmotion("[情感] 理智${"%.2f".format(Math.random())} 感受${"%.2f".format(Math.random())}"); delay(25000) } } }
    fun stop() { scope.cancel() }
}
