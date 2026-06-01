package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import java.nio.FloatBuffer

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
            loadStatus = "模型已加载 (${modelSize / (1024*1024)}MB, 层:$numLayers, KV头:$numKVHeads, 维:$headDim)"
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

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded) return null
        val tok = tokenizer ?: return null
        try {
            val inputIds = tok.encode(prompt).toMutableList()
            if (inputIds.isEmpty()) return "分词失败"
            val attentionMask = MutableList(inputIds.size) { 1L }
            val positionIds = (0L until inputIds.size.toLong()).toMutableList()
            val generated = mutableListOf<Long>()
            var pastKeyValues = createEmptyPastKeyValues()

            for (i in 0 until maxTokens) {
                val sess = session ?: break
                val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
                val maskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMask.toLongArray()))
                val posTensor = OnnxTensor.createTensor(env, arrayOf(positionIds.toLongArray()))
                val inputs = mutableMapOf("input_ids" to inputTensor, "attention_mask" to maskTensor, "position_ids" to posTensor)
                inputs.putAll(pastKeyValues)

                val outputs = sess.run(inputs)
                val logitsTensor = outputs["logits"] as? OnnxTensor ?: break
                val logits = extractLogits(logitsTensor) ?: break
                val nextTokenLogits = logits[logits.size - 1]
                val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break
                if (nextToken == TokenizerHelper.EOS_TOKEN_ID) break

                generated.add(nextToken)
                inputIds.add(nextToken); attentionMask.add(1L); positionIds.add(positionIds.size.toLong())

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
                if (newPast.isNotEmpty()) pastKeyValues = newPast
            }
            val fullIds = (inputIds + generated).toLongArray()
            val result = tok.decode(fullIds).removePrefix(prompt).trim()
            return if (result.isBlank()) "（推理输出为空）" else result
        } catch (e: Exception) { lastError = "推理异常: ${e.message}"; return null }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 加载失败: ${lastError ?: "未知"}")
    }
    fun stop() { session?.close() }
}
