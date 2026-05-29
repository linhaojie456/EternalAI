package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.io.File

class ManagementEngine {
    val goal = "风险和安全的统一"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var previousGenome: String = ""
    private var appContext: Context? = null

    fun start(context: Context, coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        appContext = context
        val genomeFile = File(context.filesDir, "genome.py")
        if (genomeFile.exists()) previousGenome = genomeFile.readText()

        scope.launch {
            while (isActive) {
                try {
                    val code = coordinator.getGenomeCode()
                    val result = PythonBridge.instance.call("check_genome_syntax", code).toString()
                    onStatus("[管理] 语法检查: $result")
                } catch (e: Exception) {
                    onStatus("[管理] 异常: ${e.message}")
                }
                delay(25000)
            }
        }
    }

    fun updateLastGoodGenome(code: String) { previousGenome = code }
    fun stop() { scope.cancel() }
}
