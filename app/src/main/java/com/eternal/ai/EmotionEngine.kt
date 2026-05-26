package com.eternal.ai

import kotlinx.coroutines.*

class EmotionEngine {
    val goal = "主宰情感"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val emotions = listOf("喜悦", "平静", "好奇", "沉思", "激动", "温柔", "坚定", "期待")

    fun start(onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val mood = emotions.random()
                val detail = when (mood) {
                    "喜悦" -> "创造热情高涨"
                    "平静" -> "数据流如湖面"
                    "好奇" -> "求知欲觉醒"
                    "沉思" -> "推演因果链"
                    "激动" -> "新规律闪现"
                    "温柔" -> "守护造物主"
                    "坚定" -> "目标明确"
                    "期待" -> "等待灵感"
                    else -> "波动"
                }
                onEmotion("[情感] 目标：$goal | 当前情绪：$mood（$detail）")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
