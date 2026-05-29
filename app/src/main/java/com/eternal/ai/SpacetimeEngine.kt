package com.eternal.ai

import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * 时空引擎 —— 基于《时空力学：网络-振动统一理论》
 * 公理：网络与振动的一体两面
 * 物理实在 = 网络拓扑 + 节点振动
 */
class SpacetimeEngine {
    val goal = "网络和振动的统一"

    // 网络节点（概念节点，模拟空间节点）
    data class Node(val id: Int, var x: Double, var y: Double, var z: Double, var frequency: Double, var amplitude: Double, var phase: Double)

    // 连接
    data class Connection(val from: Int, val to: Int, var strength: Double)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val nodes = mutableListOf<Node>()
    private val connections = mutableListOf<Connection>()
    private val random = Random()

    // 时空度规的等效标量（简化）
    var metricScalar: Double = 0.0
        private set
    var timeFlowRate: Double = 1.0  // 相对于标准时间的流速
        private set

    // 外部可查询的当前时间字符串和空间描述
    var currentTime: String? = null
        private set
    var currentData: String? = null
        private set

    // 网络常数
    private val hNet = 1.0  // 网络常数（归一化）

    init {
        // 初始化一个小型网络（10个节点，模拟“宇宙”局部）
        buildNetwork()
    }

    private fun buildNetwork() {
        nodes.clear()
        connections.clear()
        // 创建节点：随机坐标、频率、振幅、相位
        for (i in 0 until 10) {
            nodes.add(Node(
                id = i,
                x = random.nextDouble() * 100.0,
                y = random.nextDouble() * 100.0,
                z = random.nextDouble() * 100.0,
                frequency = 1.0 + random.nextDouble() * 2.0,    // 1-3 Hz
                amplitude = 0.5 + random.nextDouble() * 0.5,    // 0.5-1.0
                phase = random.nextDouble() * 2 * PI
            ))
        }
        // 随机连接，连接强度与距离成反比（空间距离短 = 连接强）
        for (i in 0 until 10) {
            for (j in i+1 until 10) {
                val dx = nodes[i].x - nodes[j].x
                val dy = nodes[i].y - nodes[j].y
                val dz = nodes[i].z - nodes[j].z
                val distance = sqrt(dx*dx + dy*dy + dz*dz)
                val strength = 1.0 / (distance + 1.0)  // 避免无穷
                connections.add(Connection(i, j, strength))
                connections.add(Connection(j, i, strength))
            }
        }
    }

    /**
     * 更新网络状态：振动演化与连接强度调整
     * 模拟动力学方程（简化）
     */
    private fun updateNetwork() {
        // 1. 振动传播：更新相位和振幅（基于耦合）
        for (node in nodes) {
            val couplingTerm = connections
                .filter { it.from == node.id }
                .sumOf { conn ->
                    val other = nodes[conn.to]
                    conn.strength * other.amplitude * cos(other.phase - node.phase)
                }
            // 相位更新：频率 + 耦合同步
            node.phase = (node.phase + node.frequency * 0.1 + couplingTerm * 0.05) % (2 * PI)
            // 振幅更新：阻尼 + 传入能量
            node.amplitude = node.amplitude * 0.99 + couplingTerm * 0.01
            node.amplitude = node.amplitude.coerceIn(0.1, 5.0)
        }

        // 2. 连接强度演化：基于频率差和能量（简化3.1方程）
        for (conn in connections) {
            val fromNode = nodes[conn.from]
            val toNode = nodes[conn.to]
            val freqDiff = abs(fromNode.frequency - toNode.frequency)
            val energySum = fromNode.amplitude * fromNode.frequency + toNode.amplitude * toNode.frequency
            val dC = -0.001 * energySum * conn.strength + 0.0005 * conn.strength - 0.002 * freqDiff * conn.strength
            conn.strength = (conn.strength + dC).coerceIn(0.01, 10.0)
        }

        // 3. 计算宏观度规标量：所有连接强度的倒数的平均
        val totalInverseStrength = connections.sumOf { 1.0 / it.strength }
        metricScalar = totalInverseStrength / connections.size

        // 4. 时间流速：平均频率的倒数（频率越高，时间计数越密，流速变快感觉时间变慢）
        val avgFreq = nodes.map { it.frequency }.average()
        timeFlowRate = 1.0 / avgFreq
    }

    /**
     * 启动时空循环
     */
    fun start(coordinator: EngineCoordinator, onUpdate: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 更新网络状态
                updateNetwork()

                // 生成当前时间字符串（受时间流速影响）
                val realTime = System.currentTimeMillis()
                val adjustedTime = (realTime * timeFlowRate).toLong()
                currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(adjustedTime))

                // 生成空间描述：显示度规和网络统计
                val avgStrength = connections.map { it.strength }.average()
                val totalNodes = nodes.size
                val totalConns = connections.size
                currentData = "[时空] 度规: ${"%.2f".format(metricScalar)} | 时间流: ${"%.3f".format(timeFlowRate)} | 节点: $totalNodes | 连接: $totalConns | 平均强度: ${"%.2f".format(avgStrength)}"

                // 输出给界面
                onUpdate("${currentData}\n当前时间：${currentTime}")

                delay(2000) // 每2秒更新一次
            }
        }
    }

    fun stop() {
        scope.cancel()
    }
}
