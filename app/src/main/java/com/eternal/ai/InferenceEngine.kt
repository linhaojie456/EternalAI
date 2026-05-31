package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File

class InferenceEngine(private val context: Context) {
    val goal = "答案和问题的统一"

    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    var isModelLoaded = false
        private set

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            if (!modelFile.exists()) return false
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            tokenizer = TokenizerHelper()
            isModelLoaded = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 主要推理入口：优先使用ONNX模型
    fun generate(prompt: String, maxTokens: Int = 200): String? {
        // 如果模型已加载，直接使用模型推理
        if (isModelLoaded && tokenizer != null) {
            return try {
                onnxInference(prompt, maxTokens)
            } catch (e: Exception) {
                "模型推理出错: ${e.message}"
            }
        }
        // 备用：简单的回声（避免无意义输出）
        return "（推理引擎未加载模型，无法生成回复）"
    }

    private fun onnxInference(prompt: String, maxTokens: Int): String? {
        val tok = tokenizer ?: return null
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
            if (nextToken == tok.eosTokenId()) break
            generated.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
        }
        val fullIds = (inputIds + generated).toLongArray()
        return tok.decode(fullIds).removePrefix(prompt).trim()
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 模型未加载，使用备用回复")
    }

    fun stop() { session?.close() }
}
