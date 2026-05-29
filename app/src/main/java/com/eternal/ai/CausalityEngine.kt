package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 因果引擎 —— 基于《因果律：空间-时间统一理论》
 * 公理链：果的本质是网络(空间) → 因的本质是振动(时间) → 因果律的本质是时空(网络和振动)
 */
class CausalityEngine {
    val goal = "空间和时间的统一"

    // 因果网络节点
    private data class CausalNode(
        val id: Int, val label: String,
        var activation: Double = 0.0,    // 当前振动强度
        var structureChange: Double = 0.0 // 结构改变量（果的积累）
    )

    // 因果连接（带有方向性 d_ij 和延迟 τ_ij）
    private data class CausalLink(
        val from: Int, val to: Int,
        var strength: Double,            // C_ij
        val direction: Boolean = true,   // d_ij: 因果方向
        val delay: Double = 1.0          // τ_ij: 传播延迟
    )

    private val nodes = mutableListOf<CausalNode>()
    private val links = mutableListOf<CausalLink>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 因果固化系数
    private val alpha = 0.1

    init {
        buildCausalNetwork()
    }

    private fun buildCausalNetwork() {
        // 创建因果概念节点
        val concepts = listOf(
            "时间", "空间", "振动", "网络", "因果",
            "因", "果", "事件", "传播", "结构",
            "反馈", "循环", "延迟", "方向", "强度"
        )
        concepts.forEachIndexed { i, label ->
            nodes.add(CausalNode(i, label, random.nextDouble() * 0.3))
        }

        // 建立因果连接（带有方向性）
        val causalRelations = listOf(
            0 to 2, 0 to 4,  // 时间→振动, 时间→因果
            1 to 3, 1 to 4,  // 空间→网络, 空间→因果
            2 to 5, 2 to 8,  // 振动→因, 振动→传播
            3 to 6, 3 to 9,  // 网络→果, 网络→结构
            4 to 5, 4 to 6,  // 因果→因, 因果→果
            5 to 7, 5 to 8,  // 因→事件, 因→传播
            6 to 7, 6 to 9,  // 果→事件, 果→结构
            8 to 6,          // 传播→果（振动传播产生果）
            9 to 3,          // 结构→网络（结构改变网络）
            10 to 4,         // 反馈→因果
            11 to 4,         // 循环→因果
            12 to 5,         // 延迟→因
            13 to 8,         // 方向→传播
            14 to 9          // 强度→结构
        )
        causalRelations.forEach { (from, to) ->
            links.add(CausalLink(
                from = from,
                to = to,
                strength = 0.3 + random.nextDouble() * 0.5,
                direction = true,
                delay = 0.5 + random.nextDouble() * 2.0
            ))
        }
        // 部分双向连接（果反作用于因）
        val feedbackLinks = listOf(
            6 to 5,  // 果→因
            7 to 4,  // 事件→因果
            9 to 2   // 结构→振动
        )
        feedbackLinks.forEach { (from, to) ->
            links.add(CausalLink(from, to, 0.2 + random.nextDouble() * 0.3, true, 0.8))
        }
    }

    /**
     * 因的传播：振动从因节点向果节点传播
     * dV_j/dt = Σ_i C_ij · V_i(t - τ_ij) · d_ij
     */
    private fun causePropagation() {
        val newActivations = DoubleArray(nodes.size)

        for (link in links) {
            if (!link.direction) continue
            val fromNode = nodes[link.from]
            val delayedInput = fromNode.activation * exp(-link.delay / 10.0)  // 模拟延迟衰减
            newActivations[link.to] += link.strength * delayedInput
        }

        // 更新节点振动状态
        nodes.forEachIndexed { i, node ->
            node.activation = (node.activation * 0.85 + newActivations[i] * 0.15).coerceIn(0.0, 1.0)
        }
    }

    /**
     * 果的固化：振动在果节点上凝固为结构改变
     * dC_jk/dt = α · V_j · δ(事件)
     */
    private fun effectSolidification() {
        for (node in nodes) {
            // 振动强度足够大时，产生结构改变
            if (node.activation > 0.3) {
                node.structureChange += alpha * node.activation
            }
            // 结构改变衰减（模拟时间冲刷）
            node.structureChange *= 0.99
        }
    }

    /**
     * 因果闭环检测：识别已经形成的因果回路
     */
    private fun detectCausalLoops(): List<List<Int>> {
        val loops = mutableListOf<List<Int>>()
        // 简化：检测长度 2-3 的回路
        for (i in nodes.indices) {
            for (j in nodes.indices) {
                if (i == j) continue
                val hasIJ = links.any { it.from == i && it.to == j && it.direction }
                val hasJI = links.any { it.from == j && it.to == i && it.direction }
                if (hasIJ && hasJI) {
                    loops.add(listOf(i, j))
                }
            }
        }
        return loops.distinctBy { it.sorted().joinToString() }
    }

    /**
     * 输出因果分析
     */
    private fun getCausalAnalysis(): String {
        // 找出因节点（振动最强的节点）
        val maxActivation = nodes.maxByOrNull { it.activation }
        // 找出果节点（结构改变最大的节点）
        val maxStructure = nodes.maxByOrNull { it.structureChange }
        // 检测因果回路
        val loops = detectCausalLoops()
        val loopCount = loops.size

        // 计算网络连通度（平均连接强度）
        val avgStrength = links.map { it.strength }.average()
        // 平均振动强度
        val avgActivation = nodes.map { it.activation }.average()
        // 平均结构改变
        val avgStructure = nodes.map { it.structureChange }.average()

        val causeNode = maxActivation?.label ?: "无"
        val effectNode = maxStructure?.label ?: "无"

        return "[因果] 因节点: $causeNode(V:${"%.2f".format(maxActivation?.activation ?: 0.0)}) | " +
               "果节点: $effectNode(Δ:${"%.2f".format(maxStructure?.structureChange ?: 0.0)}) | " +
               "因果回路: $loopCount | " +
               "平均振动: ${"%.2f".format(avgActivation)} | " +
               "平均结构: ${"%.2f".format(avgStructure)} | " +
               "网络强度: ${"%.2f".format(avgStrength)}"
    }

    fun start(coordinator: EngineCoordinator, onJudgment: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 1. 因的传播
                causePropagation()

                // 2. 果的固化
                effectSolidification()

                // 3. 动态调整网络（果的结构改变反作用于连接）
                for (link in links) {
                    val toNode = nodes[link.to]
                    if (toNode.structureChange > 0.5) {
                        link.strength = (link.strength + toNode.structureChange * 0.01).coerceIn(0.05, 1.0)
                    }
                }

                // 4. 输出因果分析
                val analysis = getCausalAnalysis()
                onJudgment(analysis)

                delay(8000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
