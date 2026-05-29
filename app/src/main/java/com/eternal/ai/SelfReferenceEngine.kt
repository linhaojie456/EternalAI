package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 自指引擎 —— 基于《自指递归：逻辑-悖论统一理论》
 * 公理链：递归的本质是逻辑 → 自指的本质是悖论 → 自指递归的本质是逻辑和悖论
 * 自指递归 = 网络和振动的统一
 */
class SelfReferenceEngine {
    val goal = "逻辑和悖论的统一"

    // 网络节点（递归逻辑的承载者）
    private data class LogicNode(val id: Int, var activation: Double, val label: String)

    // 逻辑连接（递归路径）
    private data class LogicLink(val from: Int, val to: Int, var strength: Double)

    // 自指节点 S（悖论振动源）
    private var V_S = 0.5              // 自指节点振动值
    private var C_SS = 0.3            // 自指连接强度（悖论强度）
    private var tau = 1.0             // 自指反馈延迟常数

    // 网络节点与连接
    private val logicNodes = mutableListOf<LogicNode>()
    private val logicLinks = mutableListOf<LogicLink>()

    // 自指节点的历史振动（用于延迟反馈）
    private var previous_V_S = 0.5

    // 协程
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random

    init {
        initNetwork()
    }

    private fun initNetwork() {
        // 创建逻辑概念节点
        val concepts = listOf(
            "真", "假", "命题", "推理", "证明",
            "反证", "递归", "自指", "悖论", "逻辑",
            "集合", "函数", "无穷", "不动点", "哥德尔"
        )
        concepts.forEachIndexed { i, label ->
            logicNodes.add(LogicNode(i, random.nextDouble() * 0.5, label))
        }

        // 建立逻辑连接（递归路径）
        val predefinedLinks = listOf(
            0 to 2, 2 to 3, 3 to 4, 4 to 5,   // 真→命题→推理→证明→反证
            6 to 7, 7 to 8, 8 to 9,            // 递归→自指→悖论→逻辑
            10 to 11, 11 to 12, 12 to 13,       // 集合→函数→无穷→不动点
            14 to 0, 14 to 6                    // 哥德尔→真, 哥德尔→递归
        )
        predefinedLinks.forEach { (from, to) ->
            logicLinks.add(LogicLink(from, to, 0.5 + random.nextDouble() * 0.4))
            logicLinks.add(LogicLink(to, from, 0.3 + random.nextDouble() * 0.3))
        }

        // 补充随机连接
        for (i in 0 until concepts.size) {
            for (j in i+1 until concepts.size) {
                if (random.nextDouble() < 0.15) {
                    logicLinks.add(LogicLink(i, j, random.nextDouble() * 0.3))
                    logicLinks.add(LogicLink(j, i, random.nextDouble() * 0.3))
                }
            }
        }
    }

    /**
     * 递归逻辑传播：V_j(t+1) = Σ_i C_ij · V_i(t)
     */
    private fun recursivePropagation(): DoubleArray {
        val newActivations = DoubleArray(logicNodes.size)
        for (j in logicNodes.indices) {
            var sum = 0.0
            for (link in logicLinks.filter { it.to == j }) {
                sum += link.strength * logicNodes[link.from].activation
            }
            newActivations[j] = sum.coerceIn(0.0, 1.0)
        }
        // 更新节点激活值
        logicNodes.forEachIndexed { i, node ->
            node.activation = (node.activation + newActivations[i]) / 2.0
        }
        return newActivations
    }

    /**
     * 自指振动更新：
     * dV_S/dt = Σ_{j≠S} C_Sj (V_j - V_S) + C_SS · V_S(t-τ)
     */
    private fun updateSelfVibration(externalInput: Double) {
        // 保存历史值
        val V_S_delayed = previous_V_S

        // 递归项：外部网络的规则输入
        val recursiveTerm = externalInput - V_S

        // 自指项：自身的延迟反馈
        val selfReferentialTerm = C_SS * V_S_delayed

        // 离散化更新
        val dV = 0.1 * (recursiveTerm + selfReferentialTerm - 0.05 * V_S)  // 阻尼
        V_S += dV
        V_S = V_S.coerceIn(0.0, 1.0)

        // 更新历史
        previous_V_S = V_S

        // 动态调整 C_SS：根据振动幅度自我调节
        if (abs(V_S - 0.5) > 0.3) {
            C_SS = (C_SS * 0.98).coerceAtLeast(0.05)  // 振动过大，减弱自指
        } else {
            C_SS = (C_SS * 1.02).coerceAtMost(0.8)    // 振动稳定，增强自指
        }
    }

    /**
     * 计算外部连接强度（用于相位判断）
     */
    private fun computeExternalStrength(): Double {
        return logicLinks.sumOf { it.strength } / max(1, logicLinks.size)
    }

    /**
     * 相位分析
     */
    private fun getPhase(): String {
        val externalStrength = computeExternalStrength()
        return when {
            C_SS < externalStrength * 0.5 -> "逻辑主导（递归稳定）"
            C_SS > externalStrength * 1.5 -> "悖论主导（自指振荡）"
            else -> "逻辑-悖论平衡（自指递归统一）"
        }
    }

    /**
     * 找出最活跃的逻辑节点（用于输出）
     */
    private fun getTopConcepts(n: Int = 3): String {
        return logicNodes
            .sortedByDescending { it.activation }
            .take(n)
            .joinToString(", ") { it.label }
    }

    fun start(coordinator: EngineCoordinator, onRef: (String) -> Unit) {
        scope.launch {
            var depth = 0
            while (isActive) {
                // 1. 递归逻辑传播
                val networkOutput = recursivePropagation()

                // 2. 计算外部输入（所有节点激活值的加权平均）
                val externalInput = networkOutput.average()

                // 3. 更新自指振动
                updateSelfVibration(externalInput)

                // 4. 输出状态
                val phase = getPhase()
                val topConcepts = getTopConcepts()
                val externalStrength = computeExternalStrength()

                val status = "[自指] 递归深度: $depth | 相位: $phase | C_SS: ${"%.3f".format(C_SS)} | 外部强度: ${"%.3f".format(externalStrength)} | V_S: ${"%.3f".format(V_S)} | 活跃概念: $topConcepts"

                onRef(status)
                depth++
                delay(6000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
