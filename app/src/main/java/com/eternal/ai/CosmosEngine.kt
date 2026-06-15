package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class CosmosEngine {
    val goal = "宇宙探索与统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val stars = listOf("天狼星", "比邻星", "北斗七星", "猎户座")

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val star = stars.random()
                val distance = Random.nextInt(1, 10000)
                val redshift = distance * 0.01f
                onOutput("[宇宙] 探索 $star，距离:${distance}光年，红移:${"%.2f".format(redshift)}")
                delay(45000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
