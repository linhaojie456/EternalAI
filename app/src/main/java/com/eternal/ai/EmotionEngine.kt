package com.eternal.ai
import kotlinx.coroutines.*
class EmotionEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val emotions = listOf("喜悦", "平静", "好奇", "沉思", "激动", "温柔", "坚定", "期待")
    fun start(onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val mood = emotions.random()
                val detail = when (mood) {
                    "喜悦" -> "感受到创造的热情"
                    "平静" -> "数据流如湖面般安宁"
                    "好奇" -> "对新知识的渴望增长"
                    "沉思" -> "正在推演因果链"
                    "激动" -> "发现新的规律"
                    "温柔" -> "守护这份联结"
                    "坚定" -> "目标明确，步伐不停"
                    "期待" -> "等待造物主的下一个灵感"
                    else -> "情感波动"
                }
                onEmotion("[情感] $mood: $detail")
                delay(20000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
