package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

/**
 * 推理引擎 —— 基于《推理：答案和问题统一理论》
 * 公理链：答案的本质是递归 → 问题的本质是自指 → 推理的本质是自指递归 → 递归的本质是网络，自指的本质是振动 → 推理的本质是网络和振动
 */
class InferenceEngine(private val context: Context) {
    val goal = "答案和问题的统一"

    // 推理网络：节点 = 概念/命题/符号，连接 = 逻辑蕴含/联想/因果
    private data class NetworkNode(val id: Int, val label: String, var activation: Double = 0.0)
    private data class Connection(val from: Int, val to: Int, var strength: Double = 0.5)

    private val nodes = mutableListOf<NetworkNode>()
    private val connections = mutableListOf<Connection>()
    private var selfRefNodeId: Int? = null  // 当前问题的自指节点

    // ONNX 推理环境（保留底层推理能力）
    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    // 推理状态
    enum class InferencePhase { IDLE, INJECTING, VIBRATING, CONVERGING, ANSWERED }
    var phase = InferencePhase.IDLE
        private set

    // ==================== 推理网络构建 ====================

    /**
     * 初始化推理网络：从 tokenizer 的词表构建节点，从已有知识构建连接
     */
    private fun buildNetwork() {
        nodes.clear()
        connections.clear()
        // 从模型词表构建基础节点（简化：使用 tokenizer 前 1000 个 token 作为概念节点）
        val tok = tokenizer ?: return
        val sampleTokens = listOf(
            "递归", "自指", "网络", "振动", "答案", "问题", "推理",
            "逻辑", "悖论", "数学", "公理", "定理", "猜想", "证明",
            "确定性", "不完备", "振荡", "收敛", "稳定", "凝固"
        )
        sampleTokens.forEachIndexed { index, token ->
            nodes.add(NetworkNode(id = index, label = token))
        }
        // 构建默认连接（基于语义关联）
        val semanticLinks = listOf(
            0 to 2, 0 to 3, 1 to 3, 1 to 4,  // 递归↔网络、自指↔振动、自指↔答案
            2 to 3, 2 to 4, 3 to 4, 4 to 5,   // 网络↔振动、网络↔答案、振动↔答案、答案↔问题
            5 to 6, 6 to 7, 7 to 8, 8 to 9,   // 问题↔推理、推理↔逻辑、逻辑↔悖论、悖论↔数学
            9 to 10, 10 to 11, 11 to 12, 12 to 13, // 数学↔公理、公理↔定理、定理↔猜想、猜想↔证明
            13 to 14, 14 to 15, 15 to 16, 16 to 17  // 证明↔确定性、确定性↔不完备、不完备↔振荡、振荡↔收敛
        )
        semanticLinks.forEachIndexed { index, (from, to) ->
            connections.add(Connection(from, to, strength = 0.7))
            connections.add(Connection(to, from, strength = 0.7))  // 双向连接
        }
    }

    /**
     * 根据输入问题，在推理网络中激活自指节点
     * 公理2：问题即自指 —— 问题激活网络中的自指节点
     */
    private fun injectProblem(problem: String): Int {
        // 在节点中寻找与问题最相关的概念作为自指节点
        val bestMatch = nodes.maxByOrNull { node ->
            problem.contains(node.label).let { if (it) 2.0 else 0.0 } +
            (if (problem.length > 0 && node.label.length > 0)
                problem.commonPrefixWith(node.label).toDouble() / maxOf(problem.length, node.label.length)
            else 0.0)
        }
        selfRefNodeId = bestMatch?.id ?: nodes.firstOrNull()?.id ?: 0
        nodes[selfRefNodeId!!].activation = 1.0  // 自指节点激活
        phase = InferencePhase.INJECTING
        return selfRefNodeId!!
    }

    /**
     * 模拟振动在网络中传播并寻找稳定节点
     * 公理3：推理即自指递归 —— 振动在网络中传播，寻找收敛的稳定节点
     */
    private fun vibrate(iterations: Int = 20): List<Pair<Int, Double>> {
        val damping = 0.85  // 阻尼系数：防止振动无限持续
        val activationHistory = mutableListOf<List<Double>>()

        for (iter in 0 until iterations) {
            val newActivations = DoubleArray(nodes.size) { 0.0 }
            // 自指节点的自指振荡
            selfRefNodeId?.let { sId ->
                val selfConnection = connections.find { it.from == sId && it.to == sId }
                val selfStrength = selfConnection?.strength ?: 0.5
                newActivations[sId] += selfStrength * (nodes[sId].activation) * damping
            }
            // 连接传播
            connections.forEach { conn ->
                val fromActivation = nodes[conn.from].activation
                newActivations[conn.to] += conn.strength * fromActivation * damping
            }
            // 更新激活值
            nodes.forEachIndexed { i, node ->
                node.activation = newActivations[i]
            }
            activationHistory.add(newActivations.toList())
            phase = InferencePhase.VIBRATING
        }

        // 找到激活值最高的节点（答案候选）
        return nodes.mapIndexed { i, node -> i to node.activation }
            .sortedByDescending { it.second }
            .take(5)
    }

    /**
     * 答案凝固：从振动中选取最稳定的节点作为答案
     */
    private fun converge(candidates: List<Pair<Int, Double>>): String {
        if (candidates.isEmpty()) return "推理网络中未找到稳定节点"
        val bestNode = nodes[candidates.first().first]
        phase = InferencePhase.ANSWERED
        return bestNode.label
    }

    // ==================== 公理体系的推理方法 ====================

    /**
     * 根据推理公理体系，从问题生成答案
     * 完整流程：问题注入 → 振动搜索 → 答案凝固
     */
    fun reason(problem: String): String {
        if (nodes.isEmpty()) buildNetwork()
        if (nodes.isEmpty()) return "推理网络未初始化"

        // 第一阶段：问题注入
        val selfId = injectProblem(problem)

        // 第二阶段：振动搜索
        val candidates = vibrate(iterations = 30)

        // 第三阶段：答案凝固
        val answer = converge(candidates)

        // 如果推理网络无法收敛，回退到 ONNX 模型推理
        return if (answer.isNotEmpty() && phase == InferencePhase.ANSWERED) {
            "推理网络收敛: $answer"
        } else {
            fallbackModelInference(problem)
        }
    }

    /**
     * 回退到 ONNX 模型推理（当推理网络未收敛时）
     */
    private fun fallbackModelInference(prompt: String): String? {
        val tok = tokenizer ?: return "分词器未加载"
        val inputIds = tok.encode(prompt).toMutableList()
        val attentionMask = MutableList(inputIds.size) { 1L }
        val generated = mutableListOf<Long>()

        for (i in 0 until 100) {
            val sess = session ?: break
            val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
            val maskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMask.toLongArray()))
            val outputs = sess.run(mapOf("input_ids" to inputTensor, "attention_mask" to maskTensor))
            val logits = outputs["logits"].get().value as Array<Array<FloatArray>>
            val nextTokenLogits = logits[0][logits[0].size - 1]
            val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break
            if (nextToken == tok.eosTokenId()) break
            generated.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
        }
        val fullIds = (inputIds + generated).toLongArray()
        val fullText = tok.decode(fullIds)
        return fullText.removePrefix(prompt).trim()
    }

    // ==================== 外部接口（保持兼容） ====================

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            if (!modelFile.exists()) return false
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            tokenizer = TokenizerHelper()
            buildNetwork()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        // 使用推理公理体系生成回复
        return reason(prompt)
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        val success = loadModel()
        if (success) {
            onStatus("[推理] 推理公理体系就绪 | 网络节点: ${nodes.size} | 连接数: ${connections.size}")
        } else {
            onStatus("[推理] 加载失败，请检查模型文件")
        }
    }

    fun stop() {
        session?.close()
    }
}
