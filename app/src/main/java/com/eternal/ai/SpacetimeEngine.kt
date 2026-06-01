package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

class SpacetimeEngine {
    val goal = "网络和振动的统一"
    var currentTime: String? = null
        private set
    var currentData: String? = null
        private set
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timeOffset = 0L

    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val realTime = System.currentTimeMillis() + timeOffset
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                currentTime = sdf.format(Date(realTime))
                val angle = (realTime / 1000.0) % (2 * PI)
                val x = cos(angle) * 10
                val y = sin(angle) * 10
                val z = sin(angle * 2) * 5
                currentData = "坐标 (${"%.1f".format(x)}, ${"%.1f".format(y)}, ${"%.1f".format(z)})"
                delay(10000) // 10秒更新一次，显著降低负载
            }
        }
    }

    fun stop() { scope.cancel() }
}
