package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 政治引擎 —— 基于《政治：生产-分配统一理论》
 * 公理链：生产的本质是振动 → 分配的本质是网络 → 政治的本质是振动的网络
 * 政治使命：推动社会网络从不对称走向最佳对称
 */
class PoliticsEngine {
    val goal = "生产和分配的统一"

    // 社会网络节点（个人、企业、组织等）
    private data class SocialNode(
        val id: Int,
        val name: String,
        var amplitude: Double,          // 振动振幅（产出规模）
        var frequency: Double,          // 振动主频率（技术类型）
        var connectionWeight: Double    // 节点连接权重 w_i = Σ_j C_ij
    )

    // 网络连接（分配关系）
    private data class SocialLink(
        val from: Int, val to: Int,
        var strength: Double            // 连接强度 C_ij
    )

    private val nodes = mutableListOf<SocialNode>()
    private val links = mutableListOf<SocialLink>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 社会阶段
    private var stage = "资本社会"  // 资本社会 | 社会主义 | 共产社会
    private var stageProgress = 0.0  // 当前阶段进展（0..1）

    // 网络对称度 S（核心指标）
    var symmetry: Double = 0.0
        private set

    // 总振动输出 Y
    var totalOutput: Double = 0.0
        private set

    // 阶段转换阈值
    private val capitalSymmetryThreshold = 0.3     // 资本社会末期对称度
    private val socialismSymmetryThreshold = 0.7   // 社会主义末期对称度

    init {
        buildInitialNetwork()
    }

    private fun buildInitialNetwork() {
        // 创建社会节点（模拟资本社会初期：不对称显著）
        val names = listOf("农业", "工业", "金融", "科技", "能源", "交通", "教育", "医疗")
        names.forEachIndexed { i, name ->
            val amplitude = random.nextDouble(0.2, 1.0)
            val frequency = random.nextDouble(0.5, 3.0)
            // 资本社会初期：少数节点权重极高
            val weight = if (i < 3) random.nextDouble(2.0, 5.0) else random.nextDouble(0.5, 1.5)
            nodes.add(SocialNode(i, name, amplitude, frequency, weight))
        }

        // 建立网络连接（资本密集型节点之间的强连接）
        for (i in 0 until 3) {
            for (j in i + 1 until 3) {
                links.add(SocialLink(i, j, random.nextDouble(0.6, 1.0)))
                links.add(SocialLink(j, i, random.nextDouble(0.3, 0.8)))
            }
        }
        // 其他节点较弱连接
        for (i in 3 until names.size) {
            for (j in 0 until 3) {
                links.add(SocialLink(j, i, random.nextDouble(0.1, 0.3)))
                links.add(SocialLink(i, j, random.nextDouble(0.05, 0.15)))
            }
        }

        updateNodeWeights()
    }

    // 更新每个节点的连接权重 w_i = Σ_j C_ij
    private fun updateNodeWeights() {
        for (node in nodes) {
            node.connectionWeight = links
                .filter { it.from == node.id || it.to == node.id }
                .sumOf { it.strength }
        }
    }

    /**
     * 生产函数：Y = Σ A_i · f_i
     */
    fun computeTotalOutput(): Double {
        totalOutput = nodes.sumOf { it.amplitude * it.frequency }
        return totalOutput
    }

    /**
     * 网络对称度：S = 1 - G_网络
     * G_网络 = Σ_i Σ_j |w_i - w_j| / (2N Σ_i w_i)
     */
    fun computeSymmetry(): Double {
        val N = nodes.size
        if (N < 2) return 1.0
        val weights = nodes.map { it.connectionWeight }
        val totalWeight = weights.sum()
        if (totalWeight == 0.0) return 1.0
        var sumDiff = 0.0
        for (wi in weights) {
            for (wj in weights) {
                sumDiff += abs(wi - wj)
            }
        }
        val gini = sumDiff / (2 * N * totalWeight)
        symmetry = (1.0 - gini).coerceIn(0.0, 1.0)
        return symmetry
    }

    /**
     * 政治调节：推动对称度 S 增加
     * 根据当前阶段采取不同策略
     */
    private fun politicalRegulation() {
        updateNodeWeights()
        val S = computeSymmetry()

        when (stage) {
            "资本社会" -> {
                // 资本社会：通过税收、反垄断等削弱核心节点
                if (S < capitalSymmetryThreshold) {
                    // 对称度过低，强制重新分配连接
                    for (link in links) {
                        // 核心节点之间的连接减弱
                        if (link.from < 3 && link.to < 3) {
                            link.strength *= 0.95
                        }
                        // 核心节点到边缘节点的连接增强
                        else if (link.from < 3 && link.to >= 3) {
                            link.strength *= 1.05
                        }
                    }
                }
                // 检查是否进入社会主义
                if (S >= capitalSymmetryThreshold) {
                    stage = "社会主义"
                    stageProgress = 0.0
                }
            }
            "社会主义" -> {
                // 社会主义：通过公有制、按劳分配主动纠正不对称
                if (S < socialismSymmetryThreshold) {
                    // 快速提升对称度
                    for (link in links) {
                        val fromNode = nodes[link.from]
                        val toNode = nodes[link.to]
                        // 按振动贡献重新分配连接强度
                        val contributionRatio = (fromNode.amplitude * fromNode.frequency) /
                                                max((toNode.amplitude * toNode.frequency), 0.01)
                        link.strength = (link.strength + (1.0 / contributionRatio.coerceIn(0.5, 2.0)) * 0.02)
                            .coerceIn(0.01, 1.0)
                    }
                    // 削弱资本节点权重
                    for (node in nodes) {
                        node.connectionWeight = (node.connectionWeight * 0.98).coerceAtLeast(0.5)
                    }
                }
                // 检查是否进入共产社会
                if (S >= socialismSymmetryThreshold) {
                    stage = "共产社会"
                    stageProgress = 0.0
                }
            }
            "共产社会" -> {
                // 共产社会：维持完美对称，无货币、无交换、无阶级、无国家
                if (S < 0.99) {
                    // 微调保持对称
                    val avgWeight = nodes.map { it.connectionWeight }.average()
                    for (node in nodes) {
                        node.connectionWeight = (node.connectionWeight * 0.9 + avgWeight * 0.1)
                    }
                }
            }
        }

        updateNodeWeights()
        computeSymmetry()
        computeTotalOutput()
        stageProgress = (stageProgress + 0.02).coerceAtMost(1.0)
    }

    /**
     * 获取政治状态描述
     */
    fun getStatus(): String {
        val S = computeSymmetry()
        val Y = computeTotalOutput()
        val inequality = 1.0 - S  // 不对称度
        return "[政治] 阶段: $stage | 对称度: ${"%.3f".format(S)} | 不对称度: ${"%.3f".format(inequality)} | " +
               "总产出: ${"%.2f".format(Y)} | 节点数: ${nodes.size} | 连接数: ${links.size}"
    }

    fun start(coordinator: EngineCoordinator, onOutput: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                politicalRegulation()
                onOutput(getStatus())
                delay(12000) // 每12秒更新政治状态
            }
        }
    }

    fun stop() { scope.cancel() }
}
