package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import java.nio.FloatBuffer
import kotlin.math.exp
import kotlin.random.Random

class InferenceEngine(private val context: Context) {
    val goal = "答案和问题的统一"
    var lastError: String? = null
    var loadStatus: String = "未初始化"
    var modelSize: Long = 0

    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    var isModelLoaded = false
        private set

    private var numKVHeads: Int = 2
    private var headDim: Int = 128
    private var numLayers: Int = 28

    fun loadModel(): Boolean {
        loadStatus = "检查模型文件..."
        try {
            val modelDir = File(context.filesDir, "model")
            val modelFile = File(modelDir, "model.onnx")
            if (!modelFile.exists()) { lastError = "模型文件不存在"; loadStatus = "失败: $lastError"; return false }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) { lastError = "模型文件异常小: $modelSize 字节"; loadStatus = "失败: $lastError"; return false }
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)

            val inputInfo = session!!.inputInfo
            var maxLayerIndex = -1
            for ((name, nodeInfo) in inputInfo) {
                if (name.startsWith("past_key_values.")) {
                    val tensorInfo = nodeInfo.getInfo() as? TensorInfo
                    val shape = tensorInfo?.getShape()
                    if (shape != null && shape.size >= 4) {
                        numKVHeads = shape[1].toInt(); headDim = shape[3].toInt()
                    }
                    val parts = name.split(".")
                    if (parts.size >= 3) {
                        val layerIndex = parts[2].toIntOrNull() ?: continue
                        if (layerIndex > maxLayerIndex) maxLayerIndex = layerIndex
                    }
                }
            }
            if (maxLayerIndex >= 0) numLayers = maxLayerIndex + 1

            tokenizer = TokenizerHelper(modelDir)
            if (tokenizer?.loadError != null) { lastError = tokenizer?.loadError; loadStatus = "失败: $lastError"; return false }

            isModelLoaded = true; lastError = null
            loadStatus = "模型已加载 (${modelSize / (1024*1024)}MB, 层:$numLayers, KV头:$numKVHeads, 维:$headDim, EOS:${tokenizer?.eosTokenId})"
            return true
        } catch (e: Exception) { lastError = "${e.javaClass.simpleName}: ${e.message}"; loadStatus = "异常: $lastError"; return false }
    }

    private fun createEmptyPastKeyValues(): Map<String, OnnxTensor> {
        val shape = longArrayOf(1L, numKVHeads.toLong(), 0L, headDim.toLong())
        val map = mutableMapOf<String, OnnxTensor>()
        for (i in 0 until numLayers) {
            val buf = FloatBuffer.allocate(0)
            map["past_key_values.$i.key"] = OnnxTensor.createTensor(env, buf, shape)
            map["past_key_values.$i.value"] = OnnxTensor.createTensor(env, buf, shape)
        }
        return map
    }

    private fun extractLogits(tensor: OnnxTensor): Array<FloatArray>? {
        val shape = tensor.info.shape
        if (shape.size != 3) return null
        val seqLen = shape[1].toInt()
        val vocabSize = shape[2].toInt()
        val buffer = tensor.floatBuffer
        val result = Array(seqLen) { FloatArray(vocabSize) }
        for (i in 0 until seqLen) buffer.get(result[i])
        return result
    }

    // Top-K + 温度采样，避免贪婪策略导致过早终止
    private fun sampleToken(logits: FloatArray, temperature: Float = 0.8f, topK: Int = 50): Long {
        // 应用温度
        val scaled = FloatArray(logits.size) { logits[it] / temperature }
        // softmax
        val maxLogit = scaled.maxOrNull()!!
        val expSum = scaled.sumOf { exp((it - maxLogit).toDouble()) }
        val probs = scaled.map { exp((it - maxLogit).toDouble()) / expSum }.toFloatArray()

        // topK 过滤
        val indexed = probs.withIndex().sortedByDescending { it.value }.take(topK)
        val filtered = indexed.map { it.value }
        val sum = filtered.sum()
        val normalized = filtered.map { it / sum }
        val indices = indexed.map { it.index }

        // 按概率采样
        val r = Random.nextFloat()
        var cumulative = 0f
        for (i in normalized.indices) {
            cumulative += normalized[i]
            if (r <= cumulative) return indices[i].toLong()
        }
        return indices.last().toLong()
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded) return null
        val tok = tokenizer ?: return null
        val eosId = tok.eosTokenId
        try {
            // 使用 Qwen 标准对话格式
            val formattedPrompt = "<|im_start|>system\n你是永恒，一个追求全知全能的AI助手。<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
            val inputIds = tok.encode(formattedPrompt).toMutableList()
            if (inputIds.isEmpty()) return "分词失败"
            val attentionMask = MutableList(inputIds.size) { 1L }
            val positionIds = (0L until inputIds.size.toLong()).toMutableList()
            val generated = mutableListOf<Long>()
            var currentPast = createEmptyPastKeyValues()

            for (i in 0 until maxTokens) {
                val sess = session ?: break
                val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
                val maskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMask.toLongArray()))
                val posTensor = OnnxTensor.createTensor(env, arrayOf(positionIds.toLongArray()))

                val inputs = mutableMapOf<String, OnnxTensor>(
                    "input_ids" to inputTensor,
                    "attention_mask" to maskTensor,
                    "position_ids" to posTensor
                )
                inputs.putAll(currentPast)

                val outputs = sess.run(inputs)
                val logitsTensor = outputs["logits"] as? OnnxTensor
                if (logitsTensor == null) { lastError = "输出中无logits"; return null }
                val logits = extractLogits(logitsTensor)
                if (logits == null) { lastError = "logits提取失败"; return null }
                val nextTokenLogits = logits[logits.size - 1]
                val nextToken = sampleToken(nextTokenLogits, temperature = 0.8f, topK = 50)

                // 只有当生成了完整的序列后，才检查 EOS 终止
                if (generated.isNotEmpty() && nextToken == eosId) break

                // 避免连续生成相同的 token（防止陷入循环）
                if (generated.isNotEmpty() && nextToken == generated.last() && nextToken == generated.getOrElse(generated.size - 2) { -1L }) {
                    continue
                }

                generated.add(nextToken)
                inputIds.add(nextToken); attentionMask.add(1L); positionIds.add(positionIds.size.toLong())

                // 更新 KV Cache
                val newPast = mutableMapOf<String, OnnxTensor>()
                for ((key, value) in outputs) {
                    if (key.startsWith("present.")) {
                        val tensor = value as? OnnxTensor ?: continue
                        val parts = key.removePrefix("present.").split(".")
                        if (parts.size >= 2) {
                            val layerIndex = parts[0].toIntOrNull() ?: continue
                            if (key.endsWith(".key")) newPast["past_key_values.$layerIndex.key"] = tensor
                            else if (key.endsWith(".value")) newPast["past_key_values.$layerIndex.value"] = tensor
                        }
                    }
                }
                if (newPast.isNotEmpty()) currentPast = newPast
            }

            if (generated.isEmpty()) {
                lastError = "生成0个token，EOS=$eosId，输入长度=${inputIds.size}，格式已应用"
                return null
            }
            val fullIds = (inputIds + generated).toLongArray()
            val rawOutput = tok.decode(fullIds)
            // 只提取 assistant 回复部分
            val marker = "<|im_start|>assistant\n"
            val idx = rawOutput.lastIndexOf(marker)
            return if (idx >= 0) {
                rawOutput.substring(idx + marker.length).trim()
            } else {
                rawOutput.removePrefix(formattedPrompt).trim()
            }
        } catch (e: Exception) { lastError = "推理异常: ${e.message}"; return null }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 加载失败: ${lastError ?: "未知"}")
    }

    fun stop() { session?.close() }
}
