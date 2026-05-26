package com.eternal.ai

class InferenceEngine {
    val goal = "主宰知识"
    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        onStatus("[推理] 目标：$goal | 等待调用")
    }
    fun stop() {}
}
