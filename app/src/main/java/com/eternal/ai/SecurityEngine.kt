package com.eternal.ai

import kotlinx.coroutines.*
import java.io.File

class SecurityEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var previousGenome: String = ""
    private val genomeFile: File
        get() {
            val context = com.chaquo.python.Python.getPlatform()?.application
                ?: return File("/dev/null")
            return File(context.filesDir, "genome.py")
        }

    fun start(onStatus: (String) -> Unit) {
        if (genomeFile.exists()) previousGenome = genomeFile.readText()
        scope.launch {
            while (isActive) {
                try {
                    val python = com.chaquo.python.Python.getInstance()
                    val module = python.getModule("evo_core")
                    val checkResult = module.callAttr("check_genome_syntax", previousGenome).toString()
                    onStatus("[安全] 基因组语法检查: $checkResult")
                } catch (e: Exception) {
                    onStatus("[安全] 检查失败，已回滚")
                    if (previousGenome.isNotEmpty()) genomeFile.writeText(previousGenome)
                }
                delay(30000)
            }
        }
    }

    fun updateLastGoodGenome(code: String) { previousGenome = code }
    fun stop() { scope.cancel() }
}
