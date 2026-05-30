package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.*
import kotlin.random.Random

class ManagementEngine {
    val goal = "风险和安全的统一"

    private data class OrgNode(
        val id: Int,
        val label: String,
        var activation: Double = 0.0,
        var frequency: Double = 1.0,
        var isKeyNode: Boolean = false,
        var redundancyCount: Int = 1
    )

    private data class OrgLink(
        val from: Int,
        val to: Int,
        var strength: Double,
        var isCriticalPath: Boolean = false,
        var alternativePaths: Int = 0
    )

    private val nodes = mutableListOf<OrgNode>()
    private val links = mutableListOf<OrgLink>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var riskTolerance = 0.4
    private var previousGenome: String = ""
    private var appContext: Context? = null

    init {
        buildOrganizationNetwork()
    }

    private fun buildOrganizationNetwork() {
        val departments = listOf(
            "推理核心", "进化中心", "时空监测站", "自由意志部",
            "信息情报局", "情感调控室", "因果分析所", "自指实验室",
            "灵魂哲学部", "工程实施组", "政治协调会", "管理中枢"
        )
        departments.forEachIndexed { i, label ->
            val isKey = i < 4
            nodes.add(OrgNode(
                id = i, label = label,
                activation = random.nextDouble() * 0.3,
                frequency = 1.0 + random.nextDouble() * 2.0,
                isKeyNode = isKey,
                redundancyCount = if (isKey) 1 + random.nextInt(3) else 1
            ))
        }
        for (i in 0 until nodes.size) {
            for (j in i + 1 until nodes.size) {
                if (random.nextDouble() < 0.4) {
                    val isCritical = nodes[i].isKeyNode && nodes[j].isKeyNode
                    val strength = 0.3 + random.nextDouble() * 0.5
                    val altPaths = if (isCritical) random.nextInt(3) else random.nextInt(2)
                    links.add(OrgLink(i, j, strength, isCritical, altPaths))
                    links.add(OrgLink(j, i, strength * 0.8, isCritical, altPaths))
                }
            }
        }
    }

    fun computeRisk(): Double {
        val frequencies = nodes.map { it.frequency }
        val meanFreq = frequencies.average()
        val sigmaF = sqrt(frequencies.map { (it - meanFreq).pow(2) }.average())
        val meanAmplitude = nodes.map { it.activation }.average()
        return (sigmaF * meanAmplitude).coerceIn(0.0, 1.0)
    }

    fun computeSafety(): Double {
        val keyNodes = nodes.filter { it.isKeyNode }
        val totalRedundancy = nodes.sumOf { it.redundancyCount }
        val keyDegree = keyNodes.sumOf { node ->
            links.count { it.from == node.id || it.to == node.id }
        }
        val nodeFactor = if (keyDegree > 0) totalRedundancy.toDouble() / keyDegree else 0.5

        val criticalLinks = links.filter { it.isCriticalPath }
        val totalAltPaths = links.sumOf { it.alternativePaths }
        val totalCriticalPaths = criticalLinks.size
        val pathFactor = if (totalCriticalPaths > 0) totalAltPaths.toDouble() / totalCriticalPaths else 0.5

        return (nodeFactor * pathFactor).coerceIn(0.0, 1.0)
    }

    private fun evolveNetwork() {
        val risk = computeRisk()
        val safety = computeSafety()
        val riskGradient = risk - riskTolerance
        val alpha = 0.1
        val beta = 0.2

        for (link in links) {
            if (risk > riskTolerance) {
                if (link.isCriticalPath) {
                    link.strength = (link.strength - beta * riskGradient).coerceAtLeast(0.05)
                } else {
                    link.strength = (link.strength + alpha * abs(riskGradient)).coerceAtMost(1.0)
                    link.alternativePaths++
                }
            } else if (risk < riskTolerance * 0.5) {
                if (link.isCriticalPath) {
                    link.strength = (link.strength + 0.05).coerceAtMost(1.0)
                }
                for (node in nodes) {
                    node.frequency = (node.frequency + (random.nextDouble() - 0.5) * 0.1).coerceIn(0.5, 4.0)
                }
            }
        }
        riskTolerance = (riskTolerance + (safety - 0.5) * 0.05).coerceIn(0.1, 0.8)
    }

    private fun checkGenomeSafety(): String {
        return try {
            if (appContext != null) {
                val genomeFile = File(appContext!!.filesDir, "genome.py")
                if (genomeFile.exists()) {
                    val code = genomeFile.readText()
                    PythonBridge.call("check_genome_syntax", code).toString()
                } else {
                    "基因组文件不存在"
                }
            } else {
                "上下文未初始化"
            }
        } catch (e: Exception) {
            "检查异常: ${e.message}"
        }
    }

    fun start(context: Context, coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        appContext = context
        val genomeFile = File(context.filesDir, "genome.py")
        if (genomeFile.exists()) previousGenome = genomeFile.readText()

        scope.launch {
            while (isActive) {
                evolveNetwork()
                val risk = computeRisk()
                val safety = computeSafety()
                val genomeCheck = checkGenomeSafety()
                val keyNodeStatus = nodes.filter { it.isKeyNode }
                    .joinToString(", ") { "${it.label}(V:${"%.2f".format(it.activation)})" }
                onStatus("[管理] 风险: ${"%.3f".format(risk)} | 安全: ${"%.3f".format(safety)} | " +
                         "容忍度: ${"%.3f".format(riskTolerance)} | 基因组: $genomeCheck | 关键节点: $keyNodeStatus")
                delay(10000)
            }
        }
    }

    fun updateLastGoodGenome(code: String) { previousGenome = code }
    fun stop() { scope.cancel() }
}
