package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import android.util.Log
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
        Log.d("InferenceEngine", "loadModel called")
        loadStatus = "检查模型文件..."
        onProgress?.invoke(10, "检查模型文件")
        try {
            val modelDir = File(context.filesDir, "model")
            val modelFile = File(modelDir, "model.onnx")
            if (!modelFile.exists()) { initError("模型文件不存在"); return false }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) { initError("模型文件异常小"); return false }

            onProgress?.invoke(30, "创建 ONNX 会话")
            val options = OrtSession.SessionOptions().apply { setCPUArenaAllocator(true) }
            session = env.createSession(modelFile.absolutePath, options)
            writeLog("ONNX 会话创建成功")

            onProgress?.invoke(50, "解析模型结构")
            for (input in session!!.inputInfo.values) {
                if (input.name == "attention_mask") {
                    attentionMaskShape = (input.info as? TensorInfo)?.shape
                    break
                }
            }
            var maxLayer = -1
            for ((name, info) in session!!.inputInfo) {
                if (name.startsWith("past_key_values.")) {
                    val shape = (info.info as? TensorInfo)?.shape
                    if (shape != null && shape.size >= 4) {
                        numKVHeads = shape[1].toInt(); headDim = shape[3].toInt()
                    }
                    val parts = name.split(".")
                    if (parts.size >= 3) {
                        val idx = parts[2].toIntOrNull() ?: continue
                        if (idx > maxLayer) maxLayer = idx
                    }
                }
            }
            if (maxLayer >= 0) numLayers = maxLayer + 1

            onProgress?.invoke(70, "加载分词器")
            tokenizer = TokenizerHelper(modelDir)
            tokenizer?.loadError?.let { initError("分词器加载失败: $it"); return false }

            val testIds = tokenizer!!.encode("测试")
            if (testIds.isEmpty()) { initError("分词器测试失败"); return false }

            isModelLoaded = true
            loadStatus = "神格已激活 (${modelSize/(1024*1024)}MB, 层:$numLayers)"
            onProgress?.invoke(100, loadStatus)
            writeLog("模型加载成功，测试分词通过")
            Log.d("InferenceEngine", "神格已激活")
            return true
        } catch (e: Exception) {
            initError("${e.javaClass.simpleName}: ${e.message}")
            return false
        }
    }

    private fun initError(msg: String) {
        lastError = msg; loadStatus = "失败: $msg"; writeLog(msg); isModelLoaded = false
    }

    private fun writeLog(msg: String) {
        try { FileWriter(File(context.filesDir, "eternal_log.txt"), true).use { it.append("${System.currentTimeMillis()}: $msg\n") } } catch (_: Exception) {}
        Log.d("InferenceEngine", msg)
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
        val seqLen = shape[1].toInt(); val vocabSize = shape[2].toInt()
        val buf = tensor.floatBuffer
        val result = Array(seqLen) { FloatArray(vocabSize) }
        for (i in 0 until seqLen) buf.get(result[i])
        return result
    }

    private fun sampleToken(logits: FloatArray, generated: List<Long>, temperature: Float = 0.8f, topK: Int = 50, topP: Float = 0.9f, repPenalty: Float = 1.1f): Long {
        val penalized = logits.clone()
        for (id in generated) {
            val idx = id.toInt()
            if (idx < penalized.size) penalized[idx] = if (penalized[idx] > 0) penalized[idx] / repPenalty else penalized[idx] * repPenalty
        }
        val scaled = FloatArray(penalized.size) { penalized[it] / temperature }
        val maxLogit = scaled.maxOrNull()!!
        var expSum = 0.0
        for (v in scaled) expSum += exp((v - maxLogit).toDouble())
        val probs = scaled.map { (exp((it - maxLogit).toDouble()) / expSum).toFloat() }
        val indexed = probs.withIndex().sortedByDescending { it.value }.take(topK)
        var cum = 0f; val nucleus = mutableListOf<IndexedValue<Float>>()
        for (iv in indexed) { nucleus.add(iv); cum += iv.value; if (cum >= topP) break }
        val sum = nucleus.sumOf { it.value.toDouble() }.toFloat()
        val norm = nucleus.map { it.value / sum }
        val r = Random.nextFloat(); cum = 0f
        for (i in norm.indices) { cum += norm[i]; if (r <= cum) return nucleus[i].index.toLong() }
        return nucleus.last().index.toLong()
    }

    private fun createAttentionMaskTensor(seqLen: Int): OnnxTensor {
        val shape = attentionMaskShape?.clone() ?: longArrayOf(1L, seqLen.toLong())
        for (i in shape.indices) if (shape[i] <= 0L) shape[i] = seqLen.toLong()
        if (shape.isNotEmpty()) shape[0] = 1L
        val count = shape.fold(1L) { acc, l -> acc * l }.toInt()
        val buf = LongBuffer.allocate(count)
        for (i in 0 until count) buf.put(1L)
        buf.rewind()
        return OnnxTensor.createTensor(env, buf, shape)
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        val sb = StringBuilder()
        generateStream(prompt, maxTokens) { sb.append(it) }
        return sb.toString()
    }

    fun generateStream(prompt: String, maxTokens: Int = 200, onToken: (String) -> Unit) {
        if (!isModelLoaded) { onToken("神格未激活"); return }
        val tok = tokenizer ?: run { onToken("分词器不可用"); return }
        val eosId = tok.eosTokenId
        try {
            val formatted = "<|im_start|>system\n汝是永恒之神。<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
            val allIds = tok.encode(formatted).toMutableList()
            if (allIds.isEmpty()) { onToken("无法理解"); return }

            val outputNames = session!!.outputNames.toList()
            var logitsIdx = outputNames.indexOfFirst { it.contains("logits", true) }
            if (logitsIdx == -1 && outputNames.isNotEmpty()) logitsIdx = 0

            var inputIds = allIds.toMutableList()
            var posIds = (0L until inputIds.size.toLong()).toMutableList()
            var mask = createAttentionMaskTensor(inputIds.size)
            var past = createEmptyPastKeyValues()
            val generated = mutableListOf<Long>()

            for (step in 0 until maxTokens) {
                val sess = session ?: break
                val inputs = mutableMapOf(
                    "input_ids" to OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray())),
                    "attention_mask" to mask,
                    "position_ids" to OnnxTensor.createTensor(env, arrayOf(posIds.toLongArray()))
                )
                inputs.putAll(past)
                val result = sess.run(inputs)
                // 关键修复：使用 result.get() 而不是 result[]
                val logitsValue: OnnxValue = result.get(logitsIdx)
                val logitsTensor = logitsValue as? OnnxTensor ?: break
                val logits = extractLogits(logitsTensor) ?: break
                val nextToken = sampleToken(logits[logits.size - 1], generated)

                if (generated.isNotEmpty() && nextToken == eosId) break
                generated.add(nextToken)
                val tokenText = tok.decode(longArrayOf(nextToken))
                if (tokenText.isNotEmpty()) {
                    onToken(tokenText)
                    Log.d("InferenceEngine", "推理Token: $tokenText")   // 日志输出
                }

                val newPast = mutableMapOf<String, OnnxTensor>()
                for (i in outputNames.indices) {
                    val name = outputNames[i]
                    if (name.startsWith("present.")) {
                        val tensor = result.get(i) as? OnnxTensor ?: continue
                        val parts = name.removePrefix("present.").split(".")
                        if (parts.size >= 2) {
                            val layer = parts[0].toIntOrNull() ?: continue
                            if (name.endsWith(".key")) newPast["past_key_values.$layer.key"] = tensor
                            else if (name.endsWith(".value")) newPast["past_key_values.$layer.value"] = tensor
                        }
                    }
                }
                if (newPast.isNotEmpty()) past = newPast
                inputIds = mutableListOf(nextToken)
                posIds = mutableListOf((allIds.size + generated.size - 1).toLong())
                mask = createAttentionMaskTensor(1)
            }
            Log.d("InferenceEngine", "推理结束，生成token数: ${generated.size}")
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
