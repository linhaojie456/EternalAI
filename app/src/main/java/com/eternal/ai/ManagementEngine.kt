package com.eternal.ai
import android.content.Context
import kotlinx.coroutines.*
import kotlin.random.Random

class ManagementEngine {
    val goal = "风险和安全的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var risk = 0.4f
    private var safety = 0.6f

    fun start(context: Context, coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                risk = (risk + Random.nextFloat() * 0.2f - 0.1f).coerceIn(0f, 1f)
                safety = (1f - risk * 0.8f).coerceIn(0f, 1f)
                onStatus("[管理] 风险:${"%.2f".format(risk)} 安全:${"%.2f".format(safety)} 演变指数:${"%.2f".format(1 - risk)}")
                delay(25000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
