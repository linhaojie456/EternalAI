package com.eternal.ai
import kotlinx.coroutines.*
class CausalityEngine(private val onJudgment: (String) -> Unit) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun start() {
        scope.launch {
            while (isActive) {
                val judgments = listOf("先因后果", "先果后因", "因果循环")
                onJudgment(judgments.random())
                delay(10000)
            }
        }
    }
    fun stop() { scope.cancel() }
}
