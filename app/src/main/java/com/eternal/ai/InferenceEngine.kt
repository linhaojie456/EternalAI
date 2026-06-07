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
    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()
    private var numKVHeads = 2
    private var headDim = 128
    private var numLayers = 28
    var isModelLoaded = false; private set

    fun loadModel(): Boolean {
        loadStatus = "检查模型文件..."
        try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            if (!modelFile.exists() || modelFile.length() < 1_000_000) { lastError = "模型文件异常"; return false }
            session = env.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
            val info = session!!.inputInfo; var maxL = -1
            for ((name, node) in info) if (name.startsWith("past_key_values.")) {
                val s = (node.getInfo() as? TensorInfo)?.getShape() ?: continue
                if (s.size >= 4) { numKVHeads = s[1].toInt(); headDim = s[3].toInt() }
                val idx = name.split(".").getOrNull(2)?.toIntOrNull() ?: continue
                if (idx > maxL) maxL = idx
            }
            if (maxL >= 0) numLayers = maxL + 1
            tokenizer = TokenizerHelper(File(context.filesDir, "model"))
            if (tokenizer?.loadError != null) { lastError = tokenizer?.loadError; return false }
            isModelLoaded = true; loadStatus = "模型已加载 (${numLayers}层)"
            return true
        } catch (e: Exception) { lastError = e.message; loadStatus = "异常: $lastError"; return false }
    }

    private fun emptyPast() = (0 until numLayers).flatMap { i ->
        val s = longArrayOf(1, numKVHeads.toLong(), 0, headDim.toLong()); val b = FloatBuffer.allocate(0)
        listOf("past_key_values.$i.key" to OnnxTensor.createTensor(env, b, s), "past_key_values.$i.value" to OnnxTensor.createTensor(env, b, s))
    }.toMap()

    private fun extractLogits(t: OnnxTensor): Array<FloatArray>? {
        val s = t.info.shape; if (s.size != 3) return null
        return Array(s[1].toInt()) { FloatArray(s[2].toInt()).also { t.floatBuffer.get(it) } }
    }

    private fun sample(logits: FloatArray, temp: Float = 0.8f, topK: Int = 50): Long {
        val scaled = FloatArray(logits.size) { logits[it] / temp }
        val maxL = scaled.maxOrNull()!!; var sum = 0.0
        for (v in scaled) sum += exp((v - maxL).toDouble())
        val probs = scaled.map { (exp((it - maxL).toDouble()) / sum).toFloat() }
        val top = probs.withIndex().sortedByDescending { it.value }.take(topK)
        val norm = top.map { it.value / top.sumOf { p -> p.value } }
        val r = Random.nextFloat(); var cum = 0f
        norm.forEachIndexed { i, v -> cum += v; if (r <= cum) return top[i].index.toLong() }
        return top.last().index.toLong()
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded || tokenizer == null) return null
        val tok = tokenizer!!; val eos = tok.eosTokenId
        val fmt = "<|im_start|>system\n你是永恒。<|im_end|>\n<|im_start|>user\n$prompt<|im_end|>\n<|im_start|>assistant\n"
        val ids = tok.encode(fmt).toMutableList(); if (ids.isEmpty()) return null
        val mask = MutableList(ids.size) { 1L }; val pos = (0L until ids.size).toMutableList()
        val gen = mutableListOf<Long>(); var past = emptyPast()
        val outNames = session!!.outputNames.toList()
        val logitsIdx = outNames.indexOfFirst { "logits" in it.lowercase() }.let { if (it < 0) 0 else it }

        for (step in 0 until maxTokens) {
            val s = session ?: break
            val inputs = mutableMapOf("input_ids" to OnnxTensor.createTensor(env, arrayOf(ids.toLongArray())),
                "attention_mask" to OnnxTensor.createTensor(env, arrayOf(mask.toLongArray())),
                "position_ids" to OnnxTensor.createTensor(env, arrayOf(pos.toLongArray())))
            inputs.putAll(past)
            val res = s.run(inputs)
            val logitsT = res.get(logitsIdx) as? OnnxTensor ?: break
            val logits = extractLogits(logitsT) ?: break
            val next = sample(logits[logits.size - 1])
            if (gen.isNotEmpty() && next == eos) break
            gen.add(next); ids.add(next); mask.add(1L); pos.add(pos.size.toLong())
            val newPast = mutableMapOf<String, OnnxTensor>()
            outNames.forEachIndexed { i, name ->
                if (name.startsWith("present.")) {
                    val t = res.get(i) as? OnnxTensor ?: return@forEachIndexed
                    val parts = name.removePrefix("present.").split(".")
                    if (parts.size >= 2) {
                        val layer = parts[0].toIntOrNull() ?: return@forEachIndexed
                        if (name.endsWith(".key")) newPast["past_key_values.$layer.key"] = t
                        else if (name.endsWith(".value")) newPast["past_key_values.$layer.value"] = t
                    }
                }
            }
            if (newPast.isNotEmpty()) past = newPast
        }
        if (gen.isEmpty()) return null
        val full = ids + gen; val raw = tok.decode(full.toLongArray())
        val marker = "<|im_start|>assistant\n"
        val idx = raw.lastIndexOf(marker)
        return if (idx >= 0) raw.substring(idx + marker.length).trim() else raw.removePrefix(fmt).trim()
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 失败: ${lastError ?: "未知"}")
    }
    fun stop() { session?.close() }
}
