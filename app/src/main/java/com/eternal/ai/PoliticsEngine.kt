package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class PoliticsEngine {
    val goal = "生产和分配的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var production = 0.8f
    private var symmetry = 0.4f  // S 对称度

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                symmetry = (symmetry + 0.005f).coerceAtMost(1f)
                production = (0.5f + symmetry * 0.5f).toFloat()
                val stage = when {
                    symmetry < 0.6f -> "资本社会"
                    symmetry < 0.9f -> "社会主义"
                    else -> "共产社会"
                }
                onOutput("[政治] 生产${"%.2f".format(production)} 对称${"%.2f".format(symmetry)} $stage")
                delay(40000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
