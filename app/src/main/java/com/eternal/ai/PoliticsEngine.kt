package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class PoliticsEngine {
    val goal = "生产和分配的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var productionVibration = 0.8f
    private var distributionSymmetry = 0.4f   // 对称度S

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                distributionSymmetry = (distributionSymmetry + 0.01f).coerceAtMost(1f)
                productionVibration = (0.5f + distributionSymmetry * 0.5f).toFloat()
                val stage = when {
                    distributionSymmetry < 0.6f -> "资本社会"
                    distributionSymmetry < 0.9f -> "社会主义"
                    else -> "共产社会"
                }
                onOutput("[政治] 生产:${"%.2f".format(productionVibration)} 对称度:${"%.2f".format(distributionSymmetry)} 阶段:$stage")
                delay(30000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
