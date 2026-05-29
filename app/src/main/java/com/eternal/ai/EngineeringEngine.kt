package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

class EngineeringEngine {
    val goal = "现象和抽象的统一"

    data class Concept(
        val id: Int,
        val name: String,
        var fidelity: Double,
        var robustness: Double,
        val connections: MutableMap<Int, Double> = mutableMapOf()
    )

    data class Phenomenon(
        val id: Int,
        val description: String,
        var reality: Double,
        val basedOn: List<Int>
    )

    private val concepts = mutableListOf<Concept>()
    private val phenomena = mutableListOf<Phenomenon>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private fun abstractOperator(vibrationSpectrum: List<Double>): Concept {
        val id = concepts.size
        val name = "物质概念_$id"
        val complexity = if (vibrationSpectrum.size > 1) {
            val mean = vibrationSpectrum.average()
            sqrt(vibrationSpectrum.map { (it - mean).pow(2) }.average()) / (mean + 0.1)
        } else 0.5
        val fidelity = (0.3 + complexity * 0.7).coerceIn(0.1, 0.99)
        val robustness = fidelity * random.nextDouble(0.5, 1.0)
        return Concept(id, name, fidelity, robustness)
    }

    private fun conceptualizeOperator(phenomenon: Phenomenon): List<Concept> {
        val newConcepts = mutableListOf<Concept>()
        phenomenon.basedOn.forEach { baseId ->
            if (baseId < concepts.size) {
                val base = concepts[baseId]
                base.fidelity = (base.fidelity + random.nextDouble() * 0.1).coerceAtMost(0.99)
                base.robustness = (base.robustness + random.nextDouble() * 0.05).coerceAtMost(1.0)
            }
        }
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

    private fun designPhenomenon(): Phenomenon {
        if (concepts.size < 2) return Phenomenon(0, "等待概念积累", 0.0, emptyList())
        val selectedIds = concepts.indices.toList().shuffled().take(2)
        val description = "工程产物_" + selectedIds.joinToString("_") { concepts[it].name }
        val fidelitySum = selectedIds.sumOf { concepts[it].fidelity }
        val reality = (fidelitySum / selectedIds.size) * random.nextDouble(0.8, 1.0)
        return Phenomenon(phenomena.size, description, reality, selectedIds)
    }

    private fun manufacturePhenomenon(design: Phenomenon): Boolean {
        return random.nextDouble() < design.reality
    }

    private fun testPhenomenon(design: Phenomenon, manufactured: Boolean): String {
        return if (manufactured) "测试通过，现象已实现" else "制造失败，检测到概念与振动失配"
    }

    private fun optimizeConcepts() {
        concepts.forEach { concept ->
            concept.fidelity = (concept.fidelity + (random.nextDouble() - 0.5) * 0.05).coerceIn(0.1, 0.99)
            concept.robustness = (concept.robustness + (random.nextDouble() - 0.5) * 0.03).coerceAtMost(1.0)
        }
    }

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
        val initialSpectra = listOf(
            listOf(0.5, 0.8, 1.2),
            listOf(0.9, 1.1, 0.7),
            listOf(1.3, 0.6, 1.0),
            listOf(0.4, 0.7, 0.9)
        )
        initialSpectra.forEach { spectrum -> concepts.add(abstractOperator(spectrum)) }

        scope.launch {
            while (isActive) {
                val design = designPhenomenon()
                val manufactured = manufacturePhenomenon(design)
                val testResult = testPhenomenon(design, manufactured)
                if (manufactured) {
                    phenomena.add(design)
                    conceptualizeOperator(design)
                }
                optimizeConcepts()
                onTask(getEngineeringStatus() + " | 操作: $testResult")
                delay(15000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
