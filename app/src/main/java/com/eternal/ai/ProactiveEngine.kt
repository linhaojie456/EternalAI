package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ProactiveEngine {
    val goal = "主宰自由"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val df = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun start(context: Context, onMsg: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val time = df.format(Date())
                onMsg("[主动] 目标：$goal | 当前时间：$time | 自由思绪蔓延")
                delay(60000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
