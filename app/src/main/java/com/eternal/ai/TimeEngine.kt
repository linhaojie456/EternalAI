package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TimeEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun start(onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val now = dateFormat.format(Date())
                withContext(Dispatchers.Main) { onUpdate(now) }
                delay(1000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
