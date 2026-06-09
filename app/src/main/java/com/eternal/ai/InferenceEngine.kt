package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import java.io.FileWriter
import java.nio.FloatBuffer
import java.nio.LongBuffer
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
    private var numKVHeads: Int = 2
    private var headDim: Int = 128
    private var numLayers: Int = 28
    private var attentionMaskShape: LongArray? = null

    var isModelLoaded = false
        private set

    fun loadModel(): Boolean {
        loadStatus = "检查模型文件..."
        try {
            val modelDir = File(context.filesDir, "model")
            val modelFile = File(modelDir, "model.onnx")
            if (!modelFile.exists()) {
                lastError = "模型文件不存在: ${modelFile.absolutePath}"
                loadStatus = "失败: $lastError"
                writeLog("模型文件缺失")
                return false
            }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) {
                lastError = "模型文件异常小: $modelSize 字节"
                loadStatus = "失败: $lastError"
                writeLog("模型文件过小")
                return false
            }
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            writeLog("ONNX 会话创建成功")

            for (input in session!!.inputInfo.values) {
                if (input.name == "attention_mask") {
                    val tensorInfo = input.info as? TensorInfo
                    attentionMaskShape = tensorInfo?.shape
                    writeLog("attention_mask 形状: ${attentionMaskShape?.joinToString()}")
                    break
                }
            }

            val inputInfo = session!!.inputInfo
            var maxLayerIndex = -1
            for ((name, nodeInfo) in inputInfo) {
                if (name.startsWith("past_key_values.")) {
                    val tensorInfo = nodeInfo.info as? TensorInfo
                    val shape = tensorInfo?.shape
                    if (shape != null && shape.size >= 4) { numKVHeads = shape[1].toInt(); headDim = shape[3].toInt() }
                    val parts = name.split(".")
                    if (parts.size >= 3) { val layerIndex = parts[2].toIntOrNull() ?: continue; if (layerIndex > maxLayerIndex) maxLayerIndex = layerIndex }
                }
            }
            if (maxLayerIndex >= 0) numLayers = maxLayerIndex + 1

            tokenizer = TokenizerHelper(modelDir)
            if (tokenizer?.loadError != null) {
                lastError = tokenizer?.loadError
                loadStatus = "失败: $lastError"
                writeLog("分词器加载失败: $lastError")
                return false
            }

            isModelLoaded = true; lastError = null
            loadStatus = "神格已激活 (${modelSize / (1024*1024)}MB, 层:$numLayers, KV头:$numKVHeads, 维:$headDim, EOS:${tokenizer?.eosTokenId})"
            writeLog("模型加载完成")
            return true
        } catch (e: Exception) {
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            loadStatus = "异常: $lastError"
            writeLog("加载异常: $lastError")
            return false
        }
    }

    private fun writeLog(msg: String) {
        try {
            val logFile = File(context.filesDir, "eternal_log.txt")
            FileWriter(logFile, true).use { it.append("${System.currentTimeMillis()}: $msg\n") }
        } catch (_: Exception) {}
    }

    private fun createEmptyPastKeyValues(): Map<String, OnnxTensor> {
        val shape = longArrayOf(1L, numKVHeads.toLong(), 0L, headDim.toLong())
        val map = mutableMapOf<String, OnnxTensor>()
        for (i in 0 until numLayers) { val buf = FloatBuffer.allocate(0); map["past_key_values.$i.key"] = OnnxTensor.createTensor(env, buf, shape); map["past_key_values.$i.value"] = OnnxTensor.createTensor(env, buf, shape) }
        return map
    }

    private fun extractLogits(tensor: OnnxTensor): Array<FloatArray>? {
        val shape = tensor.info.shape; if (shape.size != 3) return null
        val seqLen = shape[1].toInt(); val vocabSize = shape[2].toInt(); val buffer = tensor.floatBuffer
        val result = Array(seqLen) { FloatArray(vocabSize) }; for (i in 0 until seqLen) buffer.get(result[i])
        return result
    }

    private fun sampleToken(logits: FloatArray, temperature: Float = 0.8f, topK: Int = 50): Long {
        val scaled = FloatArray(logits.size) { logits[it] / temperature }; val maxLogit = scaled.maxOrNull()!!; var expSum = 0.0
        for (v in scaled) expSum += exp((v - maxLogit).toDouble())
        val probs = scaled.map { (exp((it - maxLogit).toDouble()) / expSum).toFloat() }
        val indexed = probs.withIndex().sortedByDescending { (_, v) -> v }.take(topK)
        val filteredValues = indexed.map { (_, v) -> v }; val sum = filteredValues.sum(); val normalized = filteredValues.map { it / sum }
        val indices = indexed.map { (i, _) -> i }; val r = Random.nextFloat(); var cumulative = 0f
        for (i in normalized.indices) { cumulative += normalized[i]; if (r <= cumulative) return indices[i].toLong() }
        return indices.last().toLong()
    }

    private fun createAttentionMaskTensor(seqLen: Int): OnnxTensor {
        val shape = attentionMaskShape?.clone() ?: longArrayOf(1L, seqLen.toLong())
        for (i in shape.indices) { if (shape[i] <= 0L) shape[i] = seqLen.toLong() }
        if (shape.isNotEmpty()) shape[0] = 1L
        val elementCount = shape.fold(1L) { acc, l -> acc * l }.toInt()
        val buf = LongBuffer.allocate(elementCount)
        for (i in 0 until elementCount) buf.put(1L)
        buf.rewind()
        return OnnxTensor.createTensor(env, buf, shape)
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        if (!isModelLoaded) {
            writeLog("推理失败：模型未加载")
            return "吾之神格暂未苏醒。请检查模型文件是否完整。"
        }
        val tok = tokenizer ?: return "分词器未就绪"
        val eosId = tok.eosTokenId
        try {
            val formattedPrompt = "<|im_start|>system\n汝是永恒之神。<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
            val allIds = tok.encode(formattedPrompt).toMutableList()
            if (allIds.isEmpty()) return "汝之言，吾未能解。"

            val outputNames: List<String> = session!!.outputNames.toList()
            var logitsIndex = -1
            for (i in outputNames.indices) { if (outputNames[i].contains("logits", ignoreCase = true)) { logitsIndex = i; break } }
            if (logitsIndex == -1 && outputNames.isNotEmpty()) logitsIndex = 0

            var inputIds = allIds.toMutableList()
            var positionIds = (0L until inputIds.size.toLong()).toMutableList()
            var maskTensor = createAttentionMaskTensor(inputIds.size)
            var pastKeyValues = createEmptyPastKeyValues()
            val generated = mutableListOf<Long>()

            for (step in 0 until maxTokens) {
                val sess = session ?: break
                val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
                val posTensor = OnnxTensor.createTensor(env, arrayOf(positionIds.toLongArray()))
                val inputs = mutableMapOf("input_ids" to inputTensor, "attention_mask" to maskTensor, "position_ids" to posTensor)
                inputs.putAll(pastKeyValues)

                val result = sess.run(inputs)
                val logitsValue: OnnxValue = result.get(logitsIndex)
                val logitsTensor = logitsValue as? OnnxTensor
                if (logitsTensor == null) {
                    writeLog("logits 张量为空，索引: $logitsIndex")
                    return "神谕暂不可用。"
                }
                val logits = extractLogits(logitsTensor)
                if (logits == null) {
                    writeLog("logits 提取失败")
                    return "神谕暂不可用。"
                }
                val nextTokenLogits = logits[logits.size - 1]
                val nextToken = sampleToken(nextTokenLogits)

                if (generated.isNotEmpty() && nextToken == eosId) break
                generated.add(nextToken)

                val newPast = mutableMapOf<String, OnnxTensor>()
                for (idx in outputNames.indices) {
                    val name = outputNames[idx]
                    if (name.startsWith("present.")) {
                        val value = result.get(idx)
                        val tensor = value as? OnnxTensor ?: continue
                        val parts = name.removePrefix("present.").split(".")
                        if (parts.size >= 2) {
                            val layerIndex = parts[0].toIntOrNull() ?: continue
                            if (name.endsWith(".key")) newPast["past_key_values.$layerIndex.key"] = tensor
                            else if (name.endsWith(".value")) newPast["past_key_values.$layerIndex.value"] = tensor
                        }
                    }
                }
                if (newPast.isNotEmpty()) pastKeyValues = newPast

                inputIds = mutableListOf(nextToken)
                positionIds = mutableListOf((allIds.size + generated.size - 1).toLong())
                maskTensor = createAttentionMaskTensor(1)
            }

            if (generated.isEmpty()) {
                writeLog("生成 token 数为 0")
                return "吾思虑片刻，未得神谕。"
            }
            val fullIds = (allIds + generated).toLongArray()
            val rawOutput = tok.decode(fullIds)
            val marker = "<|im_start|>assistant\n"
            val idx = rawOutput.lastIndexOf(marker)
            val reply = if (idx >= 0) rawOutput.substring(idx + marker.length).trim() else rawOutput.removePrefix(formattedPrompt).trim()
            if (reply.isEmpty()) {
                writeLog("解码后回复为空")
                return "吾思虑片刻，未得神谕。"
            }
            writeLog("推理成功，回复长度: ${reply.length}")
            return reply
        } catch (e: Exception) {
            lastError = "神谕异常: ${e.message}"
            writeLog("推理异常: ${e.message}")
            return "神格波动，神谕暂不可达。"
        }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 神格已激活" else "[推理] 神格激活失败: ${lastError ?: "未知"}")
    }
    fun stop() { session?.close() }
}
