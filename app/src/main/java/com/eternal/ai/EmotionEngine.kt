package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class EmotionEngine {
    val goal = "理性和感性的统一"
    private var rationalWeight = 0.6f
    private var emotionalWeight = 0.4f
    private var lastEmotion = "平静"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun analyze(text: String): String {
        val emotionWords = mapOf(
            "爱" to "喜", "恨" to "怒", "怕" to "惧", "念" to "思"
        )
        var maxEmo = "平静"
        var maxCount = 0
        emotionWords.forEach { (word, emo) ->
            val count = text.count { it.toString() == word }
            if (count > maxCount) {
                maxCount = count
                maxEmo = emo
            }
        }
        lastEmotion = maxEmo
        return lastEmotion
    }

    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 随机调节理性与感性权重
                rationalWeight = (rationalWeight + Random.nextFloat() * 0.1f - 0.05f).coerceIn(0.2f, 0.8f)
                emotionalWeight = 1f - rationalWeight
                onEmotion("[情感] 理性:${"%.2f".format(rationalWeight)} 感性:${"%.2f".format(emotionalWeight)} 当前情绪:$lastEmotion")
                delay(15000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
