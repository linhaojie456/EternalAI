package com.eternal.ai

import kotlinx.coroutines.*
import java.io.File

class SecurityEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    // 保存上一次正常的基因组代码（用于回滚）
    private var previousGenome: String = ""
    private val genomeFile: File
        get() {
            val context = com.chaquo.python.Python.getPlatform()?.application
                ?: return File("/dev/null")
            return File(context.filesDir, "genome.py")
        }

    fun start(onStatus: (String) -> Unit) {
        // 加载当前基因组作为备份
        if (genomeFile.exists()) {
            previousGenome = genomeFile.readText()
        }
        scope.launch {
            while (isActive) {
                // 定期检查基因组是否可被 Python 正确加载
                try {
                    // 调用 Python 进行语法检查（简单 ast 解析，或者 exec 尝试编译）
                    val python = com.chaquo.python.Python.getInstance()
                    val module = python.getModule("evo_core")
                    val checkResult = module.callAttr("check_genome_syntax", previousGenome).toString()
                    onStatus("[安全] 基因组语法检查: $checkResult")
                } catch (e: Exception) {
                    onStatus("[安全] 检查失败，尝试回滚到上一个正常版本")
                    // 回滚
                    if (previousGenome.isNotEmpty()) {
                        genomeFile.writeText(previousGenome)
                    }
                }
                delay(30000) // 每30秒检查一次
            }
        }
    }

    fun updateLastGoodGenome(code: String) {
        previousGenome = code
    }

    fun stop() { scope.cancel() }
}
