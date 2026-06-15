package com.eternal.ai
import kotlinx.coroutines.*
import kotlin.random.Random

class SoulEngine {
    val goal = "灵魂、能量、信息和物质的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var template = 0.9f
    private var energy = 0.7f
    private var info = 0.5f
    private var matter = 0.3f

    fun start(coordinator: EngineCoordinator, onSoul: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                energy = (template * 0.5f + energy * 0.5f + Random.nextFloat() * 0.05f).coerceIn(0f, 1f)
                info = (energy * 0.6f + info * 0.4f).coerceIn(0f, 1f)
                matter = (info * 0.4f).coerceIn(0f, 1f)
                template = (template * 0.98f + matter * 0.02f).coerceIn(0f, 1f)
                onSoul("[灵魂] 魂${"%.2f".format(template)} 能${"%.2f".format(energy)} 息${"%.2f".format(info)} 物${"%.2f".format(matter)}")
                delay(40000) // 40秒
            }
        }
    }

    fun stop() { scope.cancel() }
}
