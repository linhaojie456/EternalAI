package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TimeEngine(private val onTimeUpdate: (String) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun start() {
        scope.launch {
            while (isActive) {
                val currentTime = dateFormat.format(Date())
                withContext(Dispatchers.Main) {
                    onTimeUpdate(currentTime)
                }
                delay(1000) // 每秒更新
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
