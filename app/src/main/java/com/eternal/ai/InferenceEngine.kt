package com.eternal.ai

class InferenceEngine {
    val goal = "主宰知识"
    fun start(onStatus: (String) -> Unit) {
        onStatus("[推理] 目标：$goal | 状态：等待首次调用")
    }
    fun stop() {}
}
