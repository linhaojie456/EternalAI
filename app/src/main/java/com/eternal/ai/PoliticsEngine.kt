package com.eternal.ai
import kotlinx.coroutines.*
class PoliticsEngine {
    val goal = "生产和分配的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var symmetry = 0.4f

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                symmetry = (symmetry + 0.01f).coerceAtMost(1f)
                val stage = when {
                    symmetry < 0.6f -> "资本社会"
                    symmetry < 0.9f -> "社会主义"
                    else -> "共产社会"
                }
                onOutput("[政治] 生产${"%.2f".format(0.5f + symmetry * 0.5f)} 对称度${"%.2f".format(symmetry)} $stage")
                delay(50000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
