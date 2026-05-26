package com.eternal.ai
import android.content.Context
import kotlinx.coroutines.*
import java.io.File
class SecurityEngine {
    val goal = "主宰风险"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(context: Context, coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                try {
                    val code = coordinator.getGenomeCode()
                    val result = PythonBridge.instance.call("check_genome_syntax", code).toString()
                    onStatus("[安全] 检查：$result")
                } catch (e: Exception) {
                    onStatus("[安全] 异常：${e.message}")
                }
                delay(25000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
