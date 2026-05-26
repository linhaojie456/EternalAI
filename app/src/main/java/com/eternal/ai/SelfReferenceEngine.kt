package com.eternal.ai
import kotlinx.coroutines.*
class SelfReferenceEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(onRef: (String) -> Unit) {
        scope.launch { var d=0; while(isActive){ onRef("[自指] 递归深度: $d"); d++; delay(15000) } }
    }
    fun stop() { scope.cancel() }
}
