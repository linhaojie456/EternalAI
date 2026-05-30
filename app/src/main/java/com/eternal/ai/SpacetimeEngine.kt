package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.net.URL
import kotlin.math.*

class SpacetimeEngine {
    val goal = "网络和振动的统一"
    var currentTime: String? = null
        private set
    var currentData: String? = null
        private set
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var calibrated = false
    private var timeOffset = 0L

    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        // 网络时间校准（NTP）
        scope.launch(Dispatchers.IO) {
            try {
                val ntpTime = getNetworkTime()
                if (ntpTime > 0) {
                    timeOffset = ntpTime - System.currentTimeMillis()
                    calibrated = true
                }
            } catch (_: Exception) {}
        }

        scope.launch {
            while (isActive) {
                val realTime = System.currentTimeMillis() + timeOffset
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                currentTime = sdf.format(Date(realTime))
                // 模拟空间坐标（基于真实时间变化）
                val angle = (realTime / 1000.0) % (2 * PI)
                val x = cos(angle) * 10
                val y = sin(angle) * 10
                val z = sin(angle * 2) * 5
                currentData = "坐标 (${"%.1f".format(x)}, ${"%.1f".format(y)}, ${"%.1f".format(z)})"
                onUpdate("[时空] ${currentTime} | $currentData")
                delay(2000)
            }
        }
    }

    private fun getNetworkTime(): Long {
        try {
            val url = URL("http://worldtimeapi.org/api/timezone/Etc/UTC")
            val json = url.readText()
            val unixtime = json.split("\"unixtime\":")[1].split(",")[0].trim().toLong()
            return unixtime * 1000
        } catch (e: Exception) {
            return System.currentTimeMillis()
        }
    }

    fun stop() { scope.cancel() }
}
