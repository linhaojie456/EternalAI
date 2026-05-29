package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.io.File
import kotlin.math.*
import kotlin.random.Random

/**
 * 管理引擎 —— 基于《演变：风险-安全统一理论》
 * 公理链：风险的本质是振动 → 安全的本质是网络 → 演变的本质是振动的网络
 */
class ManagementEngine {
    val goal = "风险和安全的统一"

    // 组织网络节点（部门、职能、个体）
    private data class OrgNode(
        val id: Int,
        val label: String,
        var activation: Double = 0.0,        // 振动强度
        var frequency: Double = 1.0,          // 振动频率
        var isKeyNode: Boolean = false,       // 是否为关键节点
        var redundancyCount: Int = 1          // 冗余备份数量
    )

    // 组织连接（流程、汇报关系、协作）
    private data class OrgLink(
        val from: Int,
        val to: Int,
        var strength: Double,                 // 连接强度
        var isCriticalPath: Boolean = false,   // 是否为必经路径
        var alternativePaths: Int = 0          // 备选路径数
    )

    private val nodes = mutableListOf<OrgNode>()
    private val links = mutableListOf<OrgLink>()
    private val random = Random
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // 风险容忍度
    private var riskTolerance = 0.4
    // 基因组文件（兼容原有安全功能）
    private var previousGenome: String = ""
    private var appContext: Context? = null

    init {
        buildOrganizationNetwork()
    }

    private fun buildOrganizationNetwork() {
        // 创建组织节点
        val departments = listOf(
            "推理核心", "进化中心", "时空监测站", "自由意志部",
            "信息情报局", "情感调控室", "因果分析所", "自指实验室",
            "灵魂哲学部", "工程实施组", "政治协调会", "管理中枢"
        )
        departments.forEachIndexed { i, label ->
            val isKey = i < 4  // 前4个为核心节点
            nodes.add(OrgNode(
                id = i,
                label = label,
                activation = random.nextDouble() * 0.3,
                frequency = 1.0 + random.nextDouble() * 2.0,
                isKeyNode = isKey,
                redundancyCount = if (isKey) 1 + random.nextInt(3) else 1
            ))
        }

        // 构建组织连接
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

    /**
     * 风险度量：R(t) = σ_f(t) · A_平均(t)
     * σ_f(t)：频率散布度
     * A_平均(t)：平均振幅
     */
    fun computeRisk(): Double {
        val frequencies = nodes.map { it.frequency }
        val meanFreq = frequencies.average()
        val sigmaF = sqrt(frequencies.map { (it - meanFreq).pow(2) }.average())
        val meanAmplitude = nodes.map { it.activation }.average()
        return (sigmaF * meanAmplitude).coerceIn(0.0, 1.0)
    }

    /**
     * 安全度量：S = (Σ deg_冗余 / Σ deg_关键) · (Σ 备选路径 / Σ 必经路径)
     */
    fun computeSafety(): Double {
        // 节点冗余比
        val keyNodes = nodes.filter { it.isKeyNode }
        val totalRedundancy = nodes.sumOf { it.redundancyCount }
        val keyDegree = keyNodes.sumOf { node ->
            links.count { it.from == node.id || it.to == node.id }
        }
        val nodeFactor = if (keyDegree > 0) totalRedundancy.toDouble() / keyDegree else 0.5

        // 路径冗余比
        val criticalLinks = links.filter { it.isCriticalPath }
        val totalAltPaths = links.sumOf { it.alternativePaths }
        val totalCriticalPaths = criticalLinks.size
        val pathFactor = if (totalCriticalPaths > 0) totalAltPaths.toDouble() / totalCriticalPaths else 0.5

        return (nodeFactor * pathFactor).coerceIn(0.0, 1.0)
    }

    /**
     * 演变方程：
     * dN/dt = α · ∇²R - β · (R - R_容忍)
     */
    private fun evolveNetwork() {
        val risk = computeRisk()
        val safety = computeSafety()

        // 风险梯度：当前风险与容忍度的差距
        val riskGradient = risk - riskTolerance

        // 网络演变系数
        val alpha = 0.1  // 风险响应系数
        val beta = 0.2   // 风险纠正系数

        for (link in links) {
            // 如果风险过高，削弱关键连接，增强冗余路径
            if (risk > riskTolerance) {
                if (link.isCriticalPath) {
                    link.strength = (link.strength - beta * riskGradient).coerceAtLeast(0.05)
                } else {
                    link.strength = (link.strength + alpha * abs(riskGradient)).coerceAtMost(1.0)
                    link.alternativePaths++
                }
            }
            // 如果风险过低，增强核心连接，鼓励创新振动
            else if (risk < riskTolerance * 0.5) {
                if (link.isCriticalPath) {
                    link.strength = (link.strength + 0.05).coerceAtMost(1.0)
                }
                // 注入新振动
                for (node in nodes) {
                    node.frequency = (node.frequency + (random.nextDouble() - 0.5) * 0.1).coerceIn(0.5, 4.0)
                }
            }
        }

        // 安全度反馈调整风险容忍度
        riskTolerance = (riskTolerance + (safety - 0.5) * 0.05).coerceIn(0.1, 0.8)
    }

    /**
     * 兼容原有基因组检查功能
     */
    private fun checkGenomeSafety(): String {
        return try {
            if (appContext != null) {
                val genomeFile = File(appContext!!.filesDir, "genome.py")
                if (genomeFile.exists()) {
                    val code = genomeFile.readText()
                    PythonBridge.instance.call("check_genome_syntax", code).toString()
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
                // 1. 演变网络
                evolveNetwork()

                // 2. 计算当前风险与安全
                val risk = computeRisk()
                val safety = computeSafety()

                // 3. 检查基因组安全
                val genomeCheck = checkGenomeSafety()

                // 4. 获取关键节点状态
                val keyNodeStatus = nodes
                    .filter { it.isKeyNode }
                    .joinToString(", ") { "${it.label}(V:${"%.2f".format(it.activation)})" }

                // 5. 输出管理状态
                val status = "[管理] 风险: ${"%.3f".format(risk)} | 安全: ${"%.3f".format(safety)} | " +
                             "容忍度: ${"%.3f".format(riskTolerance)} | " +
                             "基因组: $genomeCheck | " +
                             "关键节点: $keyNodeStatus"

                onStatus(status)

                delay(10000) // 每10秒更新
            }
        }
    }

    fun updateLastGoodGenome(code: String) { previousGenome = code }
    fun stop() { scope.cancel() }
}
