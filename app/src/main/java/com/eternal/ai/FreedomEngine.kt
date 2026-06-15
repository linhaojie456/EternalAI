package com.eternal.ai
import android.content.Context
import kotlinx.coroutines.*
import kotlin.random.Random
class FreedomEngine {
    val goal = "被动和主动的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(context: Context, coordinator: EngineCoordinator, onMsg: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val p = Random.nextFloat() * 0.8f
                onMsg("[自由] 被动${"%.2f".format(p)} 主动${"%.2f".format(1-p)}")
                delay(40000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
