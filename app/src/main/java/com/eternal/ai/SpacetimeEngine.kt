package com.eternal.ai
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*
class SpacetimeEngine {
    val goal = "网络和振动的统一"
    var currentTime: String? = null
    var currentData: String? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val t = System.currentTimeMillis()
                currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(t))
                val angle = (t / 1000.0) % (2 * PI)
                currentData = "相位${"%.2f".format(angle)}"
                onUpdate("[时空] $currentTime $currentData")
                delay(15000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
