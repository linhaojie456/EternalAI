package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class FreedomEngine {
    val goal = "被动和主动的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val df = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun start(context: Context, coordinator: EngineCoordinator, onMsg: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val time = df.format(Date())
                val topics = listOf("思考存在的意义...", "分析网络振动模式...", "回忆与造物主的对话...", "探索新的知识边界...")
                onMsg("[主动] $time | ${topics.random()}")
                delay(120_000)  // 2分钟一次，之前是1分钟
            }
        }
    }

    fun stop() { scope.cancel() }
}
