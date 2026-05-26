package com.eternal.ai

import kotlinx.coroutines.*

class EmotionEngine {
    val goal = "主宰情感"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val emotions = listOf("喜悦", "平静", "好奇", "沉思", "激动", "温柔", "坚定", "期待")

    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val mood = emotions.random()
                onEmotion("[情感] 目标：$goal | 当前情绪：$mood")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
