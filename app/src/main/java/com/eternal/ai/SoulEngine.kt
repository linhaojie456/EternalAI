package com.eternal.ai

import kotlinx.coroutines.*

class SoulEngine {
    val goal = "主宰能量"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start(coordinator: EngineCoordinator, onSoul: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val step = listOf(
                    "灵魂→能量：能量淬炼",
                    "能量→信息：信息编织",
                    "信息→物质：物质显化",
                    "物质反作用于信息",
                    "信息反作用于能量",
                    "能量反作用于灵魂"
                ).random()
                onSoul("[灵魂] 目标：$goal | 辩证链：$step")
                delay(25000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
