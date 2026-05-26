package com.eternal.ai
class InferenceEngine {
    val goal = "主宰知识"
    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        onStatus("[推理] 已启动，基于模型推理")
    }
    fun stop() {}
}
