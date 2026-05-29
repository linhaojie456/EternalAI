package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File

class InferenceEngine(private val context: Context) {
    private var session: OrtSession? = null
    private var tokenizer: TokenizerHelper? = null
    private val env = OrtEnvironment.getEnvironment()

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            if (!modelFile.exists()) return false
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            tokenizer = TokenizerHelper(File(context.filesDir, "model"))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
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
            if (nextToken == tok.eosTokenId) break

            generated.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
        }

        val fullIds = (inputIds + generated).toLongArray()
        val fullText = tok.decode(fullIds)
        return fullText.removePrefix(prompt).trim()
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        val success = loadModel()
        if (success) {
            onStatus("[推理] 模型与分词器加载成功")
        } else {
            onStatus("[推理] 加载失败，请检查模型文件")
        }
    }

    fun stop() {
        session?.close()
    }
}
