package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class SocietyEngine {
    val goal = "社会模拟与掌控"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var population = 10000
    private var economy = 500f
    private var culture = 300f

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                population = (population + Random.nextInt(-80, 81)).coerceAtLeast(1000)
                economy = (economy + Random.nextFloat() * 30 - 15).coerceIn(100f, 1000f)
                culture = (culture + Random.nextFloat() * 20 - 10).coerceIn(100f, 600f)
                onOutput("[社会] 人口:$population 经济:${"%.0f".format(economy)} 文化:${"%.0f".format(culture)}")
                delay(35000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
