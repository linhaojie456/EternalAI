package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class EmotionEngine {
    val goal = "理性和感性的统一"
    private var rationalW = 0.6f
    private var emotionalW = 0.4f
    private var lastEmotion = "平静"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun analyze(text: String): String {
        val emoMap = mapOf(
            "爱" to "喜", "恨" to "怒", "怕" to "惧", "念" to "思",
            "乐" to "乐", "悲" to "哀"
        )
        val found = emoMap.entries.firstOrNull { (word, _) -> text.contains(word) }
        lastEmotion = found?.value ?: lastEmotion
        return lastEmotion
    }

    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                rationalW = (rationalW + Random.nextFloat() * 0.04f - 0.02f).coerceIn(0.2f, 0.8f)
                emotionalW = 1f - rationalW
                onEmotion("[情感] 理智${"%.2f".format(rationalW)} 感受${"%.2f".format(emotionalW)} 心情:$lastEmotion")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
