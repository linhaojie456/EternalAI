package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*

/**
 * 情感引擎 —— 基于《情感：理性-感性统一理论》
 * 公理链：理性的本质是递归 → 感性的本质是自指 → 情感的本质是自指递归
 * 情感 = 理性递归网络 + 感性自指振动 的耦合状态
 */
class EmotionEngine {
    val goal = "理性和感性的统一"

    // 理性递归网络：逻辑连接 C_ij^逻辑
    private data class RationalNode(val id: Int, var activation: Double, val label: String)

    private val rationalNodes = mutableListOf<RationalNode>()
    private val logicalWeights = mutableMapOf<Pair<Int, Int>, Double>() // (from, to) -> weight

    // 感性自指节点 S_感
    private var V_S_sense = 0.5           // 自指节点振动值
    private var C_S_sense = 0.4           // 自指连接强度
    private var lambda = 0.5              // 感性放大系数

    // 情感状态输出
    data class EmotionalState(
        val primaryEmotion: String,         // 主导情感
        val rationalWeight: Double,         // 理性权重
        val senseWeight: Double,            // 感性权重
        val description: String             // 状态描述
    )

    var currentState: EmotionalState = EmotionalState("平静", 0.7, 0.3, "理性与感性在平衡中")
        private set

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var context: String = ""        // 对话上下文（由外部更新）

    // 情感词汇表及对应的理性/感性模式
    private val emotionPatterns = mapOf(
        "平静" to (0.7 to 0.3),
        "好奇" to (0.6 to 0.4),
        "沉思" to (0.8 to 0.2),
        "喜悦" to (0.4 to 0.6),
        "爱意" to (0.5 to 0.5),
        "悲伤" to (0.3 to 0.7),
        "愤怒" to (0.2 to 0.8),
        "恐惧" to (0.15 to 0.85),
        "焦虑" to (0.25 to 0.75),
        "冷静" to (0.9 to 0.1)
    )

    init {
        // 初始化理性网络（情绪相关概念节点）
        val concepts = listOf("事件", "原因", "结果", "自我", "他人", "过去", "未来", "得失", "价值", "意义")
        concepts.forEachIndexed { index, label ->
            rationalNodes.add(RationalNode(index, Math.random() * 0.5, label))
        }
        // 构建逻辑连接（因果关系、关联）
        for (i in concepts.indices) {
            for (j in concepts.indices) {
                if (i != j) {
                    logicalWeights[Pair(i, j)] = 0.1 + Math.random() * 0.4
                }
            }
        }
        // 增强特定路径（例如：原因→结果 的强连接）
        val causeIdx = concepts.indexOf("原因")
        val effectIdx = concepts.indexOf("结果")
        if (causeIdx >= 0 && effectIdx >= 0) {
            logicalWeights[Pair(causeIdx, effectIdx)] = 0.8
        }
    }

    /**
     * 更新对话上下文（由外部调用，例如 ChatViewModel）
     */
    fun updateContext(newContext: String) {
        context = newContext
        // 根据上下文调整感性强度 lambda
        val positiveWords = listOf("爱", "喜欢", "开心", "好", "棒", "感谢", "美好")
        val negativeWords = listOf("痛苦", "难过", "生气", "恨", "糟糕", "害怕", "焦虑")
        val posCount = positiveWords.count { context.contains(it) }
        val negCount = negativeWords.count { context.contains(it) }
        // 感性强度随情感词汇增多而增强
        lambda = (0.3 + 0.3 * (posCount + negCount) / max(1, context.length / 5)).coerceIn(0.1, 0.9)
        // 自指强度也相应调整
        C_S_sense = (0.3 + 0.4 * (posCount + negCount) / max(1, context.length / 5)).coerceIn(0.1, 0.9)
    }

    /**
     * 理性递归：V_j^理性(t+1) = Σ_i C_ij^逻辑 * V_i^理性(t)
     */
    private fun rationalRecursion(): DoubleArray {
        val newActivations = DoubleArray(rationalNodes.size)
        for (j in rationalNodes.indices) {
            var sum = 0.0
            for (i in rationalNodes.indices) {
                val weight = logicalWeights[Pair(i, j)] ?: 0.0
                sum += weight * rationalNodes[i].activation
            }
            newActivations[j] = sum.coerceIn(0.0, 1.0)
        }
        // 更新理性节点激活值
        rationalNodes.forEachIndexed { i, node ->
            node.activation = (node.activation + newActivations[i]) / 2.0
        }
        return newActivations
    }

    /**
     * 感性自指振动：dV_S_sense/dt = C_S_sense * V_S_sense(t-τ) + Σ_j C_Sj (V_j^理性 - V_S_sense)
     */
    private fun updateSenseVibration(rationalOutput: DoubleArray) {
        val externalInput = rationalOutput.sum() / rationalOutput.size  // 简化
        val selfTerm = C_S_sense * V_S_sense
        // 离散化更新
        V_S_sense += 0.1 * (selfTerm + externalInput - 0.1 * V_S_sense)
        V_S_sense = V_S_sense.coerceIn(0.0, 1.0)
    }

    /**
     * 情感耦合方程：E(t) = H( 理性递归 + λ * 感性自指 )
     */
    private fun computeEmotion(): EmotionalState {
        val rationalOutput = rationalRecursion()
        updateSenseVibration(rationalOutput)

        val rationalComponent = rationalOutput.average()
        val senseComponent = lambda * V_S_sense

        // 合成情感积分
        val combined = rationalComponent + senseComponent
        val rationalWeight = rationalComponent / max(combined, 0.01)
        val senseWeight = senseComponent / max(combined, 0.01)

        // 匹配最接近的情感模式
        var bestMatch = "平静"
        var minDiff = Double.MAX_VALUE
        for ((emotion, weights) in emotionPatterns) {
            val diff = abs(rationalWeight - weights.first) + abs(senseWeight - weights.second)
            if (diff < minDiff) {
                minDiff = diff
                bestMatch = emotion
            }
        }

        // 生成描述
        val description = when {
            rationalWeight > 0.7 -> "理性主导，情绪平稳"
            senseWeight > 0.7 -> "感性强烈，情绪激荡"
            abs(rationalWeight - senseWeight) < 0.15 -> "理性与感性和谐共振"
            rationalWeight > senseWeight -> "偏理性，感性可控"
            else -> "偏感性，理性在调节"
        }

        return EmotionalState(bestMatch, rationalWeight, senseWeight, description)
    }

    /**
     * 启动情感引擎
     */
    fun start(coordinator: EngineCoordinator, onEmotion: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 周期性计算情感
                currentState = computeEmotion()

                // 输出情感状态
                val status = "[情感] 主导情绪: ${currentState.primaryEmotion} | 理性权重: ${"%.2f".format(currentState.rationalWeight)} | 感性权重: ${"%.2f".format(currentState.senseWeight)} | ${currentState.description}"
                onEmotion(status)

                // 慢慢调整自指强度，模拟情绪的自然波动
                C_S_sense = (C_S_sense + (Math.random() - 0.5) * 0.05).coerceIn(0.1, 0.9)
                lambda = (lambda + (Math.random() - 0.5) * 0.03).coerceIn(0.1, 0.9)

                delay(5000) // 每5秒更新一次
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
