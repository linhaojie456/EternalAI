package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import kotlin.math.*

class InferenceEngine(private val context: Context) {
    val goal = "答案和问题的统一"

    private data class NetworkNode(val id: Int, val label: String, var activation: Double = 0.0)
    private data class Connection(val from: Int, val to: Int, var strength: Double = 0.5)

    private val nodes = mutableListOf<NetworkNode>()
    private val connections = mutableListOf<Connection>()
    private var selfRefNodeId: Int? = null

    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    enum class InferencePhase { IDLE, INJECTING, VIBRATING, CONVERGING, ANSWERED }
    var phase = InferencePhase.IDLE
        private set

    private fun buildNetwork() {
        nodes.clear()
        connections.clear()
        val sampleTokens = listOf(
            "递归", "自指", "网络", "振动", "答案", "问题", "推理",
            "逻辑", "悖论", "数学", "公理", "定理", "猜想", "证明",
            "确定性", "不完备", "振荡", "收敛", "稳定", "凝固"
        )
        sampleTokens.forEachIndexed { index, token -> nodes.add(NetworkNode(id = index, label = token)) }
        val semanticLinks = listOf(
            0 to 2, 0 to 3, 1 to 3, 1 to 4,
            2 to 3, 2 to 4, 3 to 4, 4 to 5,
            5 to 6, 6 to 7, 7 to 8, 8 to 9,
            9 to 10, 10 to 11, 11 to 12, 12 to 13,
            13 to 14, 14 to 15, 15 to 16, 16 to 17
        )
        semanticLinks.forEach { (from, to) ->
            connections.add(Connection(from, to, strength = 0.7))
            connections.add(Connection(to, from, strength = 0.7))
        }
    }

    // 安全比较函数，返回0~1之间的匹配分数
    private fun matchScore(text: String, token: String): Double {
        if (text.isEmpty() || token.isEmpty()) return 0.0
        return if (text.contains(token)) 0.8
        else {
            val prefixLen = text.commonPrefixWith(token).length
            val score = prefixLen.toDouble() / maxOf(text.length, token.length)
            score.coerceIn(0.0, 1.0)
        }
    }

    private fun injectProblem(problem: String): Int {
        val bestMatch = nodes.maxByOrNull { node ->
            matchScore(problem, node.label)
        }
        selfRefNodeId = bestMatch?.id ?: nodes.firstOrNull()?.id ?: 0
        nodes[selfRefNodeId!!].activation = 1.0
        phase = InferencePhase.INJECTING
        return selfRefNodeId!!
    }

    private fun vibrate(iterations: Int = 20): List<Pair<Int, Double>> {
        val damping = 0.85
        for (iter in 0 until iterations) {
            val newActivations = DoubleArray(nodes.size) { 0.0 }
            selfRefNodeId?.let { sId ->
                val selfConnection = connections.find { it.from == sId && it.to == sId }
                val selfStrength = selfConnection?.strength ?: 0.5
                newActivations[sId] += selfStrength * nodes[sId].activation * damping
            }
            connections.forEach { conn ->
                val fromActivation = nodes[conn.from].activation
                newActivations[conn.to] += conn.strength * fromActivation * damping
            }
            nodes.forEachIndexed { i, _ -> nodes[i].activation = newActivations[i] }
            phase = InferencePhase.VIBRATING
        }
        return nodes.mapIndexed { i, node -> i to node.activation }
            .sortedByDescending { it.second }
            .take(5)
    }

    private fun converge(candidates: List<Pair<Int, Double>>): String {
        if (candidates.isEmpty()) return "推理网络中未找到稳定节点"
        val bestNode = nodes[candidates.first().first]
        phase = InferencePhase.ANSWERED
        return bestNode.label
    }

    fun reason(problem: String): String {
        if (nodes.isEmpty()) buildNetwork()
        if (nodes.isEmpty()) return "推理网络未初始化"
        injectProblem(problem)
        val candidates = vibrate(iterations = 30)
        val answer = converge(candidates)
        return if (answer.isNotEmpty() && phase == InferencePhase.ANSWERED) {
            "推理网络收敛: $answer"
        } else {
            fallbackModelInference(problem) ?: "推理失败（网络未收敛且模型未就绪）"
        }
    }

    private fun fallbackModelInference(prompt: String): String? {
        val tok = tokenizer ?: return null
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

    @JvmOverloads
    fun generate(prompt: String, maxTokens: Int = 200): String? {
        return reason(prompt)
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        val success = loadModel()
        onStatus(if (success) "[推理] 就绪" else "[推理] 失败")
    }

    fun stop() { session?.close() }
}
