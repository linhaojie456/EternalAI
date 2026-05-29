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
    private val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var angle = 0.0

    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 时间
                val now = df.format(Date())
                currentTime = now
                // 空间
                val x = cos(angle).toFloat()
                val y = sin(angle).toFloat()
                val z = sin(angle * 2).toFloat()
                val dist = sqrt(x*x + y*y + z*z)
                val msg = "[时空] 时间 $now | 坐标 (${"%.1f".format(x)},${"%.1f".format(y)},${"%.1f".format(z)}) 距离 ${"%.1f".format(dist)}"
                currentData = msg
                onUpdate(msg)
                angle += 0.1
                delay(2000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
