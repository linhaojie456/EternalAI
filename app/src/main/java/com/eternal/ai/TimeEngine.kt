package com.eternal.ai
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
class TimeEngine {
    val goal = "主宰时间"
    var currentTime: String? = null
        private set
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                delay(1000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
