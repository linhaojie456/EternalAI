package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TimeEngine {
    val goal = "主宰时间"
    var currentTime: String? = null
        private set
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val now = df.format(Date())
                currentTime = now
                onUpdate("[时间] 目标：$goal | 时间流 $now")
                delay(1000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
