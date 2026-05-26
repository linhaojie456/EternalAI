package com.eternal.ai

import kotlinx.coroutines.*

class CausalityEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(onJudgment: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val judgments = listOf("先因后果", "先果后因", "因果循环", "先因无果", "先果无因")
                onJudgment(judgments.random())
                delay(10000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
