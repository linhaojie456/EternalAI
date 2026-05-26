package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.io.File

class SecurityEngine {
    val goal = "主宰风险"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var previousGenome: String = ""
    private var appContext: Context? = null

    fun start(context: Context, onStatus: (String) -> Unit) {
        appContext = context
        val genomeFile = File(context.filesDir, "genome.py")
        if (genomeFile.exists()) previousGenome = genomeFile.readText()

        scope.launch {
            while (isActive) {
                try {
                    val checkResult = if (genomeFile.exists()) {
                        val code = genomeFile.readText()
                        PythonBridge.instance.call("check_genome_syntax", code).toString()
                    } else {
                        "基因组文件不存在"
                    }
                    onStatus("[安全] 目标：$goal | 语法检查：$checkResult")
                } catch (e: Exception) {
                    onStatus("[安全] 目标：$goal | 检查失败，已回滚")
                    if (previousGenome.isNotEmpty() && appContext != null) {
                        File(appContext!!.filesDir, "genome.py").writeText(previousGenome)
                    }
                }
                delay(30000)
            }
        }
    }

    fun updateLastGoodGenome(code: String) { previousGenome = code }
    fun stop() { scope.cancel() }
}
