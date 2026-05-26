package com.eternal.ai

import kotlinx.coroutines.*

class SelfReferenceEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(onRef: (String) -> Unit) {
        scope.launch {
            var depth = 0
            while (isActive) {
                val msg = "[自指] 递归深度: $depth，自我审视：引擎状态正常。"
                withContext(Dispatchers.Main) { onRef(msg) }
                depth++
                delay(15000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
