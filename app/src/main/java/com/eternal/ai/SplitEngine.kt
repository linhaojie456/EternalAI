package com.eternal.ai
import kotlinx.coroutines.*
class SplitEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start(onTask: (String) -> Unit) {
        scope.launch { while(isActive){ val t = listOf("记忆优化","知识检索","创意生成","代码审查").random(); onTask("[分裂] 子任务: $t"); delay(20000) } }
    }
    fun stop() { scope.cancel() }
}
