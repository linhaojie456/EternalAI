package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File

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

    fun loadModel(): Boolean {
        loadStatus = "检查模型文件..."
        try {
            val modelDir = File(context.filesDir, "model")
            val modelFile = File(modelDir, "model.onnx")
            if (!modelFile.exists()) {
                lastError = "模型文件不存在"
                loadStatus = "失败: $lastError"
                return false
            }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) {
                lastError = "模型文件异常小: $modelSize 字节"
                loadStatus = "失败: $lastError"
                return false
            }
            loadStatus = "创建 ONNX 会话..."
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            loadStatus = "初始化分词器..."
            tokenizer = TokenizerHelper()  // 内部通过 Chaquopy 调用 Python
            isModelLoaded = true
            lastError = null
            loadStatus = "模型已加载 (${modelSize / (1024*1024)} MB)"
            return true
        } catch (e: Exception) {
            lastError = "${e.javaClass.simpleName}: ${e.message}"
            loadStatus = "加载异常: $lastError"
            e.printStackTrace()
            return false
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded) return null
        val tok = tokenizer ?: return null
        return try {
            val inputIds = tok.encode(prompt).toMutableList()
            if (inputIds.isEmpty()) return "分词失败"
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
                if (nextToken == TokenizerHelper.EOS_TOKEN_ID) break
                generated.add(nextToken)
                inputIds.add(nextToken)
                attentionMask.add(1L)
            }
            val fullIds = (inputIds + generated).toLongArray()
            tok.decode(fullIds).removePrefix(prompt).trim()
        } catch (e: Exception) {
            lastError = "推理异常: ${e.message}"
            null
        }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 加载失败: ${lastError ?: "未知"}")
    }

    fun stop() { session?.close() }
}
