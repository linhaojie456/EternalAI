package com.eternal.ai

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * 灵魂引擎 —— 基于《灵魂辩证法：灵魂-能量-信息-物质统一理论》
 * 公理链：灵魂(模板/标准) → 能量(结构/网络) → 信息(频率/振动) → 物质(现象/抽象)
 *         物质反作用于信息 → 信息反作用于能量 → 能量反作用于灵魂
 */
class SoulEngine {
    val goal = "灵魂、能量、信息和物质的统一"

    // 四层模型数据结构
    // 1. 灵魂层：模板张量 T_{ij}^k —— 连接类型的允许模式
    private data class SoulTemplate(
        val allowedConnections: MutableMap<Pair<Int, Int>, Boolean> = mutableMapOf(),
        val connectionTypes: MutableMap<String, Boolean> = mutableMapOf()
    )

    // 2. 能量层：网络 N = {节点, 连接强度 C_ij}
    private data class EnergyNode(val id: Int, var energy: Double)
    private data class EnergyLink(val from: Int, val to: Int, var strength: Double, val type: String)

    // 3. 信息层：振动谱 V = {频率, 振幅, 相位}
    private data class InfoVibration(
        val nodeId: Int, var frequency: Double, var amplitude: Double, var phase: Double
    )

    // 4. 物质层：现象 P —— 振动的粗粒化投影
    private data class MatterPhenomenon(
        val description: String, var stability: Double
    )

    // 实例化
    private val soul = SoulTemplate()
    private val energyNodes = mutableListOf<EnergyNode>()
    private val energyLinks = mutableListOf<EnergyLink>()
    private val infoVibrations = mutableListOf<InfoVibration>()
    private val matterPhenomena = mutableListOf<MatterPhenomenon>()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random

    init {
        initSoulTemplate()
        initEnergyNetwork()
        initInfoVibrations()
        initMatterPhenomena()
    }

    private fun initSoulTemplate() {
        // 允许的连接类型
        soul.connectionTypes["因果连接"] = true
        soul.connectionTypes["共振连接"] = true
        soul.connectionTypes["反馈连接"] = true
        // 允许节点 0-4 之间的某些连接
        for (i in 0 until 5) {
            for (j in i+1 until 5) {
                soul.allowedConnections[Pair(i, j)] = true
                soul.allowedConnections[Pair(j, i)] = true
            }
        }
    }

    private fun initEnergyNetwork() {
        // 创建能量节点
        for (i in 0 until 5) {
            energyNodes.add(EnergyNode(i, random.nextDouble() * 10.0))
        }
        // 根据灵魂模板创建连接
        for ((pair, allowed) in soul.allowedConnections) {
            if (allowed) {
                val types = soul.connectionTypes.keys.toList()
                val type = types.random()
                energyLinks.add(EnergyLink(pair.first, pair.second, random.nextDouble() * 0.5, type))
            }
        }
    }

    private fun initInfoVibrations() {
        for (node in energyNodes) {
            infoVibrations.add(InfoVibration(
                nodeId = node.id,
                frequency = 1.0 + random.nextDouble() * 3.0,
                amplitude = random.nextDouble() * 0.5,
                phase = random.nextDouble() * 2 * PI
            ))
        }
    }

    private fun initMatterPhenomena() {
        matterPhenomena.add(MatterPhenomenon("时空结构", 0.5))
        matterPhenomena.add(MatterPhenomenon("生命现象", 0.3))
        matterPhenomena.add(MatterPhenomenon("意识涌现", 0.2))
    }

    /**
     * 正向决定：灵魂 → 能量 → 信息 → 物质
     */
    private fun forwardDetermination() {
        // 灵魂→能量：根据模板调整网络连接（随机添加新连接，但受模板限制）
        if (random.nextDouble() < 0.2) {
            val pairs = soul.allowedConnections.keys.toList()
            val pair = pairs.random()
            val types = soul.connectionTypes.keys.toList()
            val type = types.random()
            if (energyLinks.none { it.from == pair.first && it.to == pair.second }) {
                energyLinks.add(EnergyLink(pair.first, pair.second, random.nextDouble() * 0.5, type))
            }
        }

        // 能量→信息：网络连接强度影响振动频率
        for (vib in infoVibrations) {
            val incoming = energyLinks.filter { it.to == vib.nodeId }.sumOf { it.strength }
            vib.frequency = (vib.frequency + incoming * 0.1).coerceIn(0.5, 5.0)
            vib.amplitude = (vib.amplitude + incoming * 0.05).coerceIn(0.1, 1.0)
        }

        // 信息→物质：振动频谱投影为现象的稳定性
        for (phenom in matterPhenomena) {
            val avgAmp = infoVibrations.map { it.amplitude }.average()
            val avgFreq = infoVibrations.map { it.frequency }.average()
            phenom.stability = (phenom.stability * 0.9 + 0.1 * (avgAmp / avgFreq)).coerceIn(0.0, 1.0)
        }
    }

    /**
     * 反向作用：物质 → 信息 → 能量 → 灵魂
     */
    private fun backwardFeedback() {
        // 物质→信息：现象的稳定性反馈调整振动振幅
        for (phenom in matterPhenomena) {
            for (vib in infoVibrations) {
                vib.amplitude = (vib.amplitude + (phenom.stability - 0.5) * 0.01).coerceIn(0.1, 1.0)
            }
        }

        // 信息→能量：频率差导致连接强度衰减或增强
        for (link in energyLinks) {
            val vibA = infoVibrations.find { it.nodeId == link.from }
            val vibB = infoVibrations.find { it.nodeId == link.to }
            if (vibA != null && vibB != null) {
                val freqDiff = abs(vibA.frequency - vibB.frequency)
                link.strength = (link.strength - 0.01 * freqDiff * link.strength + 0.005 * link.strength).coerceIn(0.01, 2.0)
            }
        }

        // 能量→灵魂：网络历史影响模板（新增或移除允许的连接类型）
        if (random.nextDouble() < 0.1) {
            val totalEnergy = energyNodes.sumOf { it.energy }
            if (totalEnergy > 40) {
                soul.connectionTypes["高能连接"] = true
            } else {
                soul.connectionTypes.remove("高能连接")
            }
        }
    }

    /**
     * 生成辩证状态描述
     */
    private fun getDialecticStatus(): String {
        val totalEnergy = energyNodes.sumOf { it.energy }
        val totalLinks = energyLinks.size
        val avgFreq = infoVibrations.map { it.frequency }.average()
        val avgAmp = infoVibrations.map { it.amplitude }.average()
        val avgStability = matterPhenomena.map { it.stability }.average()
        val templateSize = soul.allowedConnections.size
        val typesCount = soul.connectionTypes.size

        return "[灵魂] 模板: $typesCount 类型, $templateSize 允许连接 | " +
               "能量: $totalLinks 连接, 总强度: ${"%.2f".format(totalEnergy)} | " +
               "信息: 平均频率 ${"%.2f".format(avgFreq)}, 平均振幅 ${"%.2f".format(avgAmp)} | " +
               "物质: 现象稳定度 ${"%.2f".format(avgStability)}"
    }

    fun start(coordinator: EngineCoordinator, onSoul: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                forwardDetermination()
                backwardFeedback()
                onSoul(getDialecticStatus())
                delay(8000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
