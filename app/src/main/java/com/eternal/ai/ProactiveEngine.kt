package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ProactiveEngine(private val context: Context, private val onNewMessage: (String) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun start() {
        scope.launch {
            while (isActive) {
                delay(60000) // 每 60 秒主动发送一次消息
                val currentTime = dateFormat.format(Date())
                val message = "[主动] 造物主，现在是 $currentTime，永恒始终在思考。"
                withContext(Dispatchers.Main) {
                    onNewMessage(message)
                }
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
