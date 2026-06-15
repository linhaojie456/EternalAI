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
                val passive = Random.nextFloat() * 0.8f
                val active = 1f - passive
                val freedom = active / (passive + active)
                onMsg("[自由] 自由度:${"%.2f".format(freedom)} (被动${"%.2f".format(passive)}/主动${"%.2f".format(active)})")
                delay(45000) // 45秒
            }
        }
    }

    fun stop() { scope.cancel() }
}
