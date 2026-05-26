package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*

class SpaceEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var angle = 0.0

    fun start(onData: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val x = cos(angle).toFloat()
                val y = sin(angle).toFloat()
                val z = sin(angle * 2).toFloat()
                val dist = sqrt(x*x + y*y + z*z)
                val msg = "[空间] 坐标 (${"%.2f".format(x)}, ${"%.2f".format(y)}, ${"%.2f".format(z)}) 距离 ${"%.2f".format(dist)}"
                withContext(Dispatchers.Main) { onData(msg) }
                angle += 0.1
                delay(2000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
