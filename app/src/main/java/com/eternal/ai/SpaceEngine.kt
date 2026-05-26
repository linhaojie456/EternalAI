package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*

class SpaceEngine(private val onSpaceData: (String) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    data class Point3D(val x: Float, val y: Float, val z: Float)

    fun start() {
        scope.launch {
            var angle = 0.0
            while (isActive) {
                // 生成一个在单位球面上旋转的点
                val x = cos(angle).toFloat()
                val y = sin(angle).toFloat()
                val z = sin(angle * 2).toFloat()
                val point = Point3D(x, y, z)
                val distance = sqrt(x*x + y*y + z*z)
                val message = "[空间] 当前坐标 (${"%.2f".format(x)}, ${"%.2f".format(y)}, ${"%.2f".format(z)}) 距离原点 ${"%.2f".format(distance)}"
                withContext(Dispatchers.Main) {
                    onSpaceData(message)
                }
                angle += 0.1
                delay(2000) // 每 2 秒更新
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
