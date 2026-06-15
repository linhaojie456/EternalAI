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
                val passiveWeight = Random.nextFloat() * 0.7f
                val activeWeight = 1f - passiveWeight
                val freedom = activeWeight / (passiveWeight + activeWeight)
                onMsg("[自由] 被动度: ${"%.2f".format(passiveWeight)}，自由度: ${"%.2f".format(freedom)}")
                delay(20000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
