package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 工程引擎 —— 基于《物质：现象-抽象统一理论》
 * 公理链：现象的本质是抽象 → 抽象的本质是概念 → 物质的本质是抽象的概念
 * 工程 = 在概念网络中操作抽象概念，以生成新现象
 */
class EngineeringEngine {
    val goal = "现象和抽象的统一"

    // 概念节点：物质概念（抽象概念的存储单元）
    data class Concept(
        val id: Int,
        val name: String,
        var fidelity: Double,          // 保真度：概念对底层振动的还原度
        var robustness: Double,        // 鲁棒性：概念在工程操作中的稳定性
        var connections: MutableMap<Int, Double> = mutableMapOf() // 与其他概念的连接强度
    )

    // 现象记录：工程操作后生成的物质现象
    data class Phenomenon(
        val id: Int,
        val description: String,
        var reality: Double,           // 现实度（与底层振动一致性）
        val basedOn: List<Int>         // 基于的概念节点ID
    )

    private val concepts = mutableListOf<Concept>()
    private val phenomena = mutableListOf<Phenomenon>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 抽象算子：将振动频谱抽象为概念（简化模拟）
    private fun abstractOperator(vibrationSpectrum: List<Double>): Concept {
        val id = concepts.size
        val name = "物质概念_$id"
        // 保真度取决于频谱的复杂度（用标准差模拟）
        val complexity = if (vibrationSpectrum.size > 1) {
            val mean = vibrationSpectrum.average()
            sqrt(vibrationSpectrum.map { (it - mean).pow(2) }.average()) / (mean + 0.1)
        } else 0.5
        val fidelity = (0.3 + complexity * 0.7).coerceIn(0.1, 0.99)
        val robustness = fidelity * random.nextDouble(0.5, 1.0)
        return Concept(id, name, fidelity, robustness)
    }

    // 概念化算子：从现象中提取/强化概念（模式识别 + 不变性提取）
    private fun conceptualizeOperator(phenomenon: Phenomenon): List<Concept> {
        val newConcepts = mutableListOf<Concept>()
        // 基于当前概念网络，生成新概念或增强已有概念
        phenomenon.basedOn.forEach { baseId ->
            if (baseId < concepts.size) {
                val base = concepts[baseId]
                // 提高已有概念的保真度（学习）
                base.fidelity = (base.fidelity + random.nextDouble() * 0.1).coerceAtMost(0.99)
                base.robustness = (base.robustness + random.nextDouble() * 0.05).coerceAtMost(1.0)
            }
        }
        // 可能发现新概念
        if (random.nextDouble() < 0.3) {
            val newId = concepts.size
            val newConcept = Concept(
                newId,
                "新概念_$newId",
                fidelity = 0.4 + random.nextDouble() * 0.4,
                robustness = 0.5 + random.nextDouble() * 0.3
            )
            concepts.add(newConcept)
            newConcepts.add(newConcept)
        }
        return newConcepts
    }

    // 工程设计：组合现有概念生成新现象方案
    private fun designPhenomenon(): Phenomenon {
        if (concepts.size < 2) return Phenomenon(0, "等待概念积累", 0.0, emptyList())
        val selectedIds = random.sample(concepts.indices.toList(), min(2, concepts.size))
        val description = "工程产物_" + selectedIds.joinToString("_") { concepts[it].name }
        // 物质实在性 = 保真度函数
        val fidelitySum = selectedIds.sumOf { concepts[it].fidelity }
        val reality = (fidelitySum / selectedIds.size) * random.nextDouble(0.8, 1.0)
        return Phenomenon(phenomena.size, description, reality, selectedIds)
    }

    // 工程制造：将概念结构逆向激活底层振动（模拟失败概率）
    private fun manufacturePhenomenon(design: Phenomenon): Boolean {
        val successChance = design.reality
        return random.nextDouble() < successChance
    }

    // 工程测试：比较产物现象与设计概念
    private fun testPhenomenon(design: Phenomenon, manufactured: Boolean): String {
        return if (manufactured) {
            "测试通过，现象已实现"
        } else {
            "制造失败，检测到概念与振动失配"
        }
    }

    // 工程优化：调整概念参数以提高保真度或鲁棒性
    private fun optimizeConcepts() {
        concepts.forEach { concept ->
            // 随机小幅度优化
            concept.fidelity = (concept.fidelity + (random.nextDouble() - 0.5) * 0.05).coerceIn(0.1, 0.99)
            concept.robustness = (concept.robustness + (random.nextDouble() - 0.5) * 0.03).coerceAtMost(1.0)
        }
    }

    // 构建工程状态字符串
    private fun getEngineeringStatus(): String {
        val totalConcepts = concepts.size
        val avgFidelity = concepts.map { it.fidelity }.average()
        val avgRobustness = concepts.map { it.robustness }.average()
        val totalPhenomena = phenomena.size
        val lastPhenomenon = phenomena.lastOrNull()?.description ?: "无"

        return "[工程] 概念数量: $totalConcepts | 平均保真度: ${"%.2f".format(avgFidelity)} | " +
               "平均鲁棒性: ${"%.2f".format(avgRobustness)} | 产物数: $totalPhenomena | " +
               "最新产物: $lastPhenomenon"
    }

    fun start(coordinator: EngineCoordinator, onTask: (String) -> Unit) {
        // 初始化一些基础概念
        val initialSpectra = listOf(
            listOf(0.5, 0.8, 1.2),
            listOf(0.9, 1.1, 0.7),
            listOf(1.3, 0.6, 1.0),
            listOf(0.4, 0.7, 0.9)
        )
        initialSpectra.forEach { spectrum ->
            concepts.add(abstractOperator(spectrum))
        }

        scope.launch {
            while (isActive) {
                // 工程循环：设计 → 制造 → 测试 → 概念化 → 优化
                val design = designPhenomenon()
                val manufactured = manufacturePhenomenon(design)
                val testResult = testPhenomenon(design, manufactured)

                if (manufactured) {
                    phenomena.add(design)
                    conceptualizeOperator(design) // 从现象中学习
                }

                optimizeConcepts()

                // 输出状态
                onTask(getEngineeringStatus() + " | 操作: $testResult")

                delay(15000) // 每15秒执行一次工程循环
            }
        }
    }

    fun stop() { scope.cancel() }
}
