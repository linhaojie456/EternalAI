package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.*

/**
 * 信息引擎 —— 基于《信息：频率-数字统一理论》
 * 公理链：频率的本质是振动 → 数字的本质是频率 → 信息的本质是振动的频率
 * 信息量 = 振动频谱的复杂度
 */
class InformationEngine {
    val goal = "频率和数字的统一"

    // 振动频谱：每个节点 i 的频率分量列表 F_i = {(f, A, φ, k)}
    private data class FrequencyComponent(
        val frequency: Double,     // 频率 f
        val amplitude: Double,     // 振幅 A
        val phase: Double          // 相位 φ
    )

    // 节点频谱
    private val nodeSpectra = mutableMapOf<Int, MutableList<FrequencyComponent>>()
    // 当前连接状态（用于频谱比较和冗余计算）
    private var connectedNodes = 0
    private var totalConnections = 0

    // 网络连接相关
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var enabled = true
    private var onStatus: ((String) -> Unit)? = null

    // 参考功率（归一化）
    private val referencePower = 1.0

    // 节点计数器
    private var nodeIdCounter = 0

    init {
        // 初始化一些频谱节点
        for (i in 0 until 5) {
            addNode()
        }
    }

    private fun addNode() {
        val components = mutableListOf<FrequencyComponent>()
        // 每个节点有 1-4 个频率分量
        val k = (1..4).random()
        for (j in 0 until k) {
            components.add(FrequencyComponent(
                frequency = 1.0 + Math.random() * 4.0,      // 1-5 Hz
                amplitude = 0.2 + Math.random() * 0.8,       // 0.2-1.0
                phase = Math.random() * 2 * PI
            ))
        }
        nodeSpectra[nodeIdCounter++] = components
    }

    /**
     * 计算节点 i 的信息量：I_i = K_i * log(1 + Σ A_k * f_k / P_ref)
     */
    fun computeNodeInformation(nodeId: Int): Double {
        val spectrum = nodeSpectra[nodeId] ?: return 0.0
        val K = spectrum.size
        if (K == 0) return 0.0

        val powerSum = spectrum.sumOf { it.amplitude * it.frequency }
        return K * ln(1.0 + powerSum / referencePower)
    }

    /**
     * 计算系统总信息量：I_total = Σ I_i - Σ I_ij^冗余
     * 简化：冗余由连接强度和频率相似度决定
     */
    fun computeTotalInformation(): Double {
        var total = 0.0
        for (nodeId in nodeSpectra.keys) {
            total += computeNodeInformation(nodeId)
        }
        // 减去冗余（简化：相邻节点频率越接近，冗余越大）
        var redundancy = 0.0
        val nodeIds = nodeSpectra.keys.toList()
        for (i in 0 until nodeIds.size - 1) {
            val s1 = nodeSpectra[nodeIds[i]] ?: continue
            val s2 = nodeSpectra[nodeIds[i+1]] ?: continue
            // 频率相似度
            val freqs1 = s1.map { it.frequency }.sorted()
            val freqs2 = s2.map { it.frequency }.sorted()
            var similarity = 0.0
            for (f1 in freqs1) {
                for (f2 in freqs2) {
                    similarity += exp(-(f1 - f2).pow(2) / 0.5)  // 高斯相似度
                }
            }
            similarity /= max(1, freqs1.size * freqs2.size)
            redundancy += similarity * (computeNodeInformation(nodeIds[i]) + computeNodeInformation(nodeIds[i+1])) / 2
        }
        return max(0.0, total - redundancy * 0.3)
    }

    /**
     * 归一化功率分布：p_k = |A_k|^2 / Σ_j |A_j|^2
     */
    fun getPowerDistribution(nodeId: Int): List<Double> {
        val spectrum = nodeSpectra[nodeId] ?: return emptyList()
        val totalPower = spectrum.sumOf { it.amplitude * it.amplitude }
        if (totalPower == 0.0) return List(spectrum.size) { 0.0 }
        return spectrum.map { it.amplitude * it.amplitude / totalPower }
    }

    /**
     * 振动频谱复杂度（信息熵）：H = -Σ p_k log p_k + log K
     */
    fun computeEntropy(nodeId: Int): Double {
        val p = getPowerDistribution(nodeId)
        if (p.isEmpty()) return 0.0
        val K = p.size
        val entropy = -p.sumOf { if (it > 0) it * ln(it) else 0.0 }
        return entropy + ln(K.toDouble())
    }

    /**
     * 频谱演化：模拟振动传播和频率调整
     */
    private fun evolveSpectra() {
        for ((nodeId, spectrum) in nodeSpectra) {
            for (comp in spectrum) {
                // 频率微调：受网络连接影响（简化）
                comp.copy(frequency = comp.frequency * (1.0 + (Math.random() - 0.5) * 0.05))
                // 振幅衰减和随机激励
                comp.copy(amplitude = (comp.amplitude * 0.98 + Math.random() * 0.02).coerceIn(0.1, 1.0))
                // 相位演化
                comp.copy(phase = (comp.phase + comp.frequency * 0.1) % (2 * PI))
            }
        }
        // 周期性添加/移除节点（模拟网络演化）
        if (Math.random() < 0.1 && nodeSpectra.size < 10) addNode()
        if (Math.random() < 0.05 && nodeSpectra.size > 3) {
            val randomKey = nodeSpectra.keys.randomOrNull()
            if (randomKey != null) nodeSpectra.remove(randomKey)
        }
    }

    // ==================== 原有网络接口（保留，增强） ====================

    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) {
        onStatus = onInfo
        checkConnection()
        scope.launch {
            while (isActive) {
                delay(15000)  // 每15秒更新
                evolveSpectra()
                val info = computeTotalInformation()
                val avgEntropy = nodeSpectra.keys.map { computeEntropy(it) }.average()
                onInfo?.invoke("[信息] 总信息量: ${"%.2f".format(info)} nat | 节点: ${nodeSpectra.size} | 平均频谱复杂度: ${"%.2f".format(avgEntropy)}")
                checkConnection()
            }
        }
    }

    fun setEnabled(e: Boolean) {
        enabled = e
        if (e) checkConnection()
        else {
            onStatus?.invoke("[信息] 已手动断开")
        }
    }

    fun isEnabled(): Boolean = enabled

    private fun checkConnection() {
        if (!enabled) return
        scope.launch {
            try {
                val url = URL("https://api.ipify.org")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val ip = conn.inputStream.bufferedReader().readText()
                    connectedNodes++
                    totalConnections++
                    onStatus?.invoke("[信息] 已连接 IP: $ip | 频谱节点: ${nodeSpectra.size}")
                }
                conn.disconnect()
            } catch (e: Exception) {
                onStatus?.invoke("[信息] 离线 - 频谱本地演化中")
            }
        }
    }

    /**
     * 频率共振检测：接收外部频率，检测内部频谱是否有共振
     */
    fun detectResonance(externalFrequency: Double): List<Int> {
        val resonatingNodes = mutableListOf<Int>()
        val threshold = 0.3
        for ((nodeId, spectrum) in nodeSpectra) {
            for (comp in spectrum) {
                if (abs(comp.frequency - externalFrequency) / max(comp.frequency, externalFrequency) < threshold) {
                    resonatingNodes.add(nodeId)
                    break
                }
            }
        }
        return resonatingNodes
    }

    fun deepSearch(query: String, callback: (String) -> Unit) {
        // 搜索：将查询转化为频率模式，检测内部共振
        val queryFreq = query.hashCode().toDouble() % 5.0 + 1.0
        val resonances = detectResonance(queryFreq)
        if (resonances.isNotEmpty()) {
            callback("[信息] 内部频谱共振: 节点 ${resonances.joinToString(", ")}")
        } else {
            // 回退到传统搜索
            scope.launch {
                try {
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                    val response = URL(url).readText()
                    val json = org.json.JSONObject(response)
                    val abstract = json.optString("Abstract", "")
                    if (abstract.isNotEmpty()) callback("[信息] $abstract")
                    else callback("[信息] 未找到外部信息，频谱搜索完成")
                } catch (e: Exception) {
                    callback("[信息] 搜索失败: ${e.message}")
                }
            }
        }
    }

    fun search(query: String, callback: (String) -> Unit) = deepSearch(query, callback)

    fun stop() { scope.cancel() }
}
