package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random
class EmotionEngine {
    val goal = "理性和感性的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun analyze(text: String): String = listOf("平静","喜悦","悲伤").random()
    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) { scope.launch { while (isActive) { onEmotion("[情感] ${analyze("")}"); delay(25000) } } }
    fun stop() { scope.cancel() }
}
