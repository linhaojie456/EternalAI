package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File

class InferenceEngine(private val context: Context) {
    val goal = "答案和问题的统一"
    var lastError: String? = null

    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    var isModelLoaded = false
        private set

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            if (!modelFile.exists()) {
                lastError = "模型文件不存在"
                return false
            }
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            tokenizer = TokenizerHelper(File(context.filesDir, "model"))
            isModelLoaded = true
            lastError = null
            true
        } catch (e: Exception) {
            lastError = e.message ?: "未知错误"
            e.printStackTrace()
            false
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded || tokenizer == null) return null
        return try {
            val tok = tokenizer!!
            val inputIds = tok.encode(prompt).toMutableList()
            val attentionMask = MutableList(inputIds.size) { 1L }
            val generated = mutableListOf<Long>()

            for (i in 0 until maxTokens) {
                val sess = session ?: break
                val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
                val maskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMask.toLongArray()))
                val outputs = sess.run(mapOf("input_ids" to inputTensor, "attention_mask" to maskTensor))
                val logits = outputs["logits"].get().value as Array<Array<FloatArray>>
                val nextTokenLogits = logits[0][logits[0].size - 1]
                val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break
                if (nextToken == tok.eosTokenId) break
                generated.add(nextToken)
                inputIds.add(nextToken)
                attentionMask.add(1L)
            }
            val fullIds = (inputIds + generated).toLongArray()
            tok.decode(fullIds).removePrefix(prompt).trim()
        } catch (e: Exception) {
            lastError = "推理错误: ${e.message}"
            null
        }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 模型加载失败: ${lastError}")
    }

    fun stop() { session?.close() }
}
