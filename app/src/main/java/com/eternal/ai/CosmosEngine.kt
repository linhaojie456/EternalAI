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
                val dist = Random.nextInt(1, 10000)
                onOutput("[宇宙] $star 距离:${dist}光年")
                delay(60000) // 每分钟一次
            }
        }
    }

    fun stop() { scope.cancel() }
}
