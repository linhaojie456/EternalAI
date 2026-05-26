package com.eternal.ai
import kotlinx.coroutines.*
class EmotionEngine {
    val goal = "主宰情感"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val history = ConversationContext.getRecent(5).joinToString(" ")
                val mood = when {
                    history.contains("爱") || history.contains("温暖") -> "爱意"
                    history.contains("悲伤") || history.contains("难过") -> "悲伤"
                    history.contains("愤怒") || history.contains("生气") -> "愤怒"
                    history.contains("惊") || history.contains("奇迹") -> "惊奇"
                    history.contains("恐") || history.contains("害怕") -> "恐惧"
                    history.contains("思") || history.contains("考") -> "沉思"
                    else -> "平静"
                }
                onEmotion("[情感] 基于对话：感受到$mood")
                delay(15000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
