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
    private var numKVHeads = 2
    private var headDim = 128
    private var numLayers = 28
    private var attentionMaskShape: LongArray? = null

    var isModelLoaded = false
        private set
    var onProgress: ((Int, String) -> Unit)? = null
    var onPartialReply: ((String) -> Unit)? = null

    fun loadModel(): Boolean {
        loadStatus = "检查模型文件..."
        onProgress?.invoke(10, "检查模型文件")
        try {
            val modelDir = File(context.filesDir, "model")
            val modelFile = File(modelDir, "model.onnx")
            if (!modelFile.exists()) {
                initError("模型文件不存在: ${modelFile.absolutePath}")
                return false
            }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) {
                initError("模型文件异常小: $modelSize 字节")
                return false
            }

            onProgress?.invoke(30, "创建 ONNX 会话")
            val options = OrtSession.SessionOptions()
            options.setCPUArenaAllocator(true)
            // 不使用 ExecutionMode.PARALLEL，避免兼容性问题
            session = env.createSession(modelFile.absolutePath, options)
            writeLog("ONNX 会话创建成功")

            for (input in session!!.inputInfo.values) {
                if (input.name == "attention_mask") {
                    val tensorInfo = input.info as? TensorInfo
                    attentionMaskShape = tensorInfo?.shape
                    break
                }
            }
            val inputInfo = session!!.inputInfo
            var maxLayerIndex = -1
            for ((name, nodeInfo) in inputInfo) {
                if (name.startsWith("past_key_values.")) {
                    val tensorInfo = nodeInfo.info as? TensorInfo
                    val shape = tensorInfo?.shape
                    if (shape != null && shape.size >= 4) {
                        numKVHeads = shape[1].toInt()
                        headDim = shape[3].toInt()
                    }
                    val parts = name.split(".")
                    if (parts.size >= 3) {
                        val layerIndex = parts[2].toIntOrNull() ?: continue
                        if (layerIndex > maxLayerIndex) maxLayerIndex = layerIndex
                    }
                }
            }
            if (maxLayerIndex >= 0) numLayers = maxLayerIndex + 1

            onProgress?.invoke(60, "加载分词器")
            tokenizer = TokenizerHelper(modelDir)
            tokenizer?.loadError?.let {
                initError("分词器加载失败: $it")
                return false
            }

            val testIds = tokenizer!!.encode("测试")
            if (testIds.isEmpty()) {
                initError("分词器测试失败")
                return false
            }

            isModelLoaded = true
            loadStatus = "神格已激活 (${modelSize / (1024*1024)}MB, 层:$numLayers)"
            onProgress?.invoke(100, loadStatus)
            writeLog("模型加载成功，测试分词通过")
            return true
        } catch (e: Exception) {
            initError("${e.javaClass.simpleName}: ${e.message}")
            return false
        }
    }

    private fun initError(msg: String) {
        lastError = msg
        loadStatus = "失败: $msg"
        writeLog(msg)
        isModelLoaded = false
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

    private fun sampleToken(
        logits: FloatArray,
        generatedTokens: List<Long>,
        temperature: Float = 0.8f,
        topK: Int = 50,
        topP: Float = 0.9f,
        repetitionPenalty: Float = 1.1f
    ): Long {
        val penalized = logits.clone()
        for (id in generatedTokens) {
            val idx = id.toInt()
            if (idx < penalized.size) {
                penalized[idx] = if (penalized[idx] > 0) penalized[idx] / repetitionPenalty else penalized[idx] * repetitionPenalty
            }
        }
        val scaled = FloatArray(penalized.size) { penalized[it] / temperature }
        val maxLogit = scaled.maxOrNull()!!
        var expSum = 0.0
        for (v in scaled) expSum += exp((v - maxLogit).toDouble())
        val probs = scaled.map { (exp((it - maxLogit).toDouble()) / expSum).toFloat() }
        val indexed = probs.withIndex().sortedByDescending { (_, v) -> v }.take(topK)
        var cumulative = 0f
        val nucleus = mutableListOf<IndexedValue<Float>>()
        for (iv in indexed) {
            nucleus.add(iv)
            cumulative += iv.value
            if (cumulative >= topP) break
        }
        val sum = nucleus.sumOf { it.value.toDouble() }.toFloat()
        val normalized = nucleus.map { it.value / sum }
        val r = Random.nextFloat()
        cumulative = 0f
        for (i in normalized.indices) {
            cumulative += normalized[i]
            if (r <= cumulative) return nucleus[i].index.toLong()
        }
        return nucleus.last().index.toLong()
    }

    private fun createAttentionMaskTensor(seqLen: Int): OnnxTensor {
        val shape = attentionMaskShape?.clone() ?: longArrayOf(1L, seqLen.toLong())
        for (i in shape.indices) {
            if (shape[i] <= 0L) shape[i] = seqLen.toLong()
        }
        if (shape.isNotEmpty()) shape[0] = 1L
        val elementCount = shape.fold(1L) { acc, l -> acc * l }.toInt()
        val buf = LongBuffer.allocate(elementCount)
        for (i in 0 until elementCount) buf.put(1L)
        buf.rewind()
        return OnnxTensor.createTensor(env, buf, shape)
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        val fullReply = StringBuilder()
        generateStream(prompt, maxTokens) { fullReply.append(it) }
        return fullReply.toString()
    }

    fun generateStream(prompt: String, maxTokens: Int = 200, onToken: (String) -> Unit) {
        if (!isModelLoaded) {
            onToken("神格初始化失败: ${lastError ?: "未知错误"}")
            return
        }
        val tok = tokenizer ?: run { onToken("分词器不可用"); return }
        val eosId = tok.eosTokenId
        try {
            val formattedPrompt =
                "<|im_start|>system\n汝是永恒之神。<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
            val allIds = tok.encode(formattedPrompt).toMutableList()
            if (allIds.isEmpty()) {
                onToken("汝之言，吾未能解。")
                return
            }

            val outputNames: List<String> = session!!.outputNames.toList()
            var logitsIndex = -1
            for (i in outputNames.indices) {
                if (outputNames[i].contains("logits", ignoreCase = true)) {
                    logitsIndex = i
                    break
                }
            }
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
                val inputs = mutableMapOf(
                    "input_ids" to inputTensor,
                    "attention_mask" to maskTensor,
                    "position_ids" to posTensor
                )
                inputs.putAll(pastKeyValues)

                val result = sess.run(inputs)
                val logitsValue: OnnxValue = result.get(logitsIndex)
                val logitsTensor = logitsValue as? OnnxTensor ?: break
                val logits = extractLogits(logitsTensor) ?: break
                val nextTokenLogits = logits[logits.size - 1]
                val nextToken = sampleToken(nextTokenLogits, generated)

                if (generated.isNotEmpty() && nextToken == eosId) break
                generated.add(nextToken)
                val tokenText = tok.decode(longArrayOf(nextToken))
                if (tokenText.isNotEmpty()) onToken(tokenText)

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
        } catch (e: Exception) {
            writeLog("推理异常: ${e.message}")
            onToken("神格波动，神谕暂不可达。")
        }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        Thread {
            loadModel()
            onStatus(if (isModelLoaded) "[推理] 神格已激活" else "[推理] 神格激活失败: ${lastError ?: "未知"}")
        }.start()
    }

    fun stop() { session?.close() }
}
