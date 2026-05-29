package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 自由引擎 —— 基于《自由：被动-主动统一理论》
 * 公理链：被动的本质是递归 → 主动的本质是自指 → 自由的本质是自指递归
 * 自由 = 被动递归网络 + 主动自指振动 的动态耦合
 */
class FreedomEngine {
    val goal = "被动和主动的统一"

    // 神经网络参数
    private val numNodes = 10               // 网络节点数（不含自指节点 S）
    private val nodes = DoubleArray(numNodes) // 节点激活值 V_i
    private val fixedWeights = Array(numNodes) { DoubleArray(numNodes) } // 固化连接强度 C_ij^固化

    // 自指节点 S（独立管理）
    private var V_S = 0.5                  // 自指节点振动值
    private var C_SS = 0.3                // 自指连接强度

    // 耦合系数（可动态调整）
    private var alpha = 0.6               // 被动权重
    private var beta = 0.4                // 主动权重

    // 自由度比值
    var freedomDegree: Double = 0.5
        private set

    // 协程
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random

    init {
        // 随机初始化固化连接（非自指）
        for (i in 0 until numNodes) {
            for (j in 0 until numNodes) {
                fixedWeights[i][j] = if (i == j) 0.0 else random.nextDouble() * 0.5
            }
        }
        // 初始激活值随机
        for (i in 0 until numNodes) {
            nodes[i] = random.nextDouble() * 0.8
        }
    }

    /**
     * 被动递归：V_j(t+1) = Σ_i C_ij^固化 * V_i(t)
     */
    private fun passiveRecursion(): DoubleArray {
        val newNodes = DoubleArray(numNodes)
        for (j in 0 until numNodes) {
            var sum = 0.0
            for (i in 0 until numNodes) {
                sum += fixedWeights[i][j] * nodes[i]
            }
            newNodes[j] = sum.coerceIn(0.0, 1.0)
        }
        return newNodes
    }

    /**
     * 主动自指振动：dV_S/dt = C_SS * V_S(t-τ) + Σ_j C_Sj (V_j - V_S)
     * 离散化近似
     */
    private fun updateSelfVibration(externalInput: DoubleArray) {
        val sumExternal = externalInput.sumOf { it - V_S } / numNodes
        // 延迟反馈使用上一次的 V_S，这里简化无延迟
        val selfTerm = C_SS * V_S
        V_S += 0.1 * (selfTerm + sumExternal - 0.1 * V_S) // 阻尼
        V_S = V_S.coerceIn(0.0, 1.0)
    }

    /**
     * 主动修改函数 G(V_S)：将自指振动转化为对行为的调整
     */
    private fun activeModification(): DoubleArray {
        // 简单实现：V_S 越大，修改幅度越大，随机扰动方向
        val mod = DoubleArray(numNodes) { random.nextDouble() * V_S * 0.3 - V_S * 0.15 }
        return mod
    }

    /**
     * 合成行为输出：V_行为 = α * 被动递归 + β * 主动修改
     */
    private fun synthesizeBehavior(passive: DoubleArray, active: DoubleArray): DoubleArray {
        val behavior = DoubleArray(numNodes)
        for (i in 0 until numNodes) {
            behavior[i] = alpha * passive[i] + beta * active[i]
            behavior[i] = behavior[i].coerceIn(0.0, 1.0)
        }
        return behavior
    }

    /**
     * 计算自由度：β * ||G(V_S)|| / (α * ||passive|| + β * ||G(V_S)||)
     */
    private fun computeFreedomDegree(passive: DoubleArray, active: DoubleArray): Double {
        val passiveNorm = passive.map { abs(it) }.sum() / numNodes
        val activeNorm = active.map { abs(it) }.sum() / numNodes
        val denominator = alpha * passiveNorm + beta * activeNorm
        return if (denominator > 0.0) (beta * activeNorm) / denominator else 0.5
    }

    /**
     * 外部输入模拟：可根据对话上下文或随机生成
     * 这里用随机；未来可通过 coordinator 获取上下文
     */
    private fun getExternalInput(): DoubleArray {
        // 简单随机扰动
        return DoubleArray(numNodes) { random.nextDouble() * 0.5 }
    }

    /**
     * 启动自由引擎循环
     */
    fun start(context: Context, coordinator: EngineCoordinator, onMsg: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 1. 获取外部输入（环境/对话上下文）
                val external = getExternalInput()

                // 2. 被动递归
                val passive = passiveRecursion()

                // 3. 更新主动自指振动
                updateSelfVibration(external)

                // 4. 主动修改
                val active = activeModification()

                // 5. 合成行为
                val behavior = synthesizeBehavior(passive, active)

                // 6. 更新网络状态（将行为反馈为下一时刻的节点激活值）
                for (i in 0 until numNodes) {
                    nodes[i] = (nodes[i] + behavior[i]) / 2.0
                }

                // 7. 计算自由度
                freedomDegree = computeFreedomDegree(passive, active)

                // 8. 动态调整 α, β：根据自由度反馈（自由度低时增强主动，高时保持平衡）
                if (freedomDegree < 0.3) {
                    beta = (beta * 1.1).coerceAtMost(0.9)
                    alpha = 1.0 - beta
                } else if (freedomDegree > 0.7) {
                    beta = (beta * 0.95).coerceAtLeast(0.1)
                    alpha = 1.0 - beta
                }

                // 9. 输出状态信息
                val status = "[自由] 自由度: ${"%.2f".format(freedomDegree)} | 被动权重α: ${"%.2f".format(alpha)} | 主动权重β: ${"%.2f".format(beta)} | 自指振动V_S: ${"%.2f".format(V_S)}"
                onMsg(status)

                delay(4000) // 每4秒更新一次
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
