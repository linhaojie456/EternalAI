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
            tokenizer = TokenizerHelper(modelDir)
            if (tokenizer?.loadError != null) {
                lastError = tokenizer?.loadError
                loadStatus = "失败: $lastError"
                return false
            }
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

    // 新的推理方法：一次性输入，直接获取整个输出
    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded) return null
        val tok = tokenizer ?: return null
        return try {
            val inputIds = tok.encode(prompt)
            val attentionMask = LongArray(inputIds.size) { 1L }
            val positionIds = LongArray(inputIds.size) { it.toLong() }

            val inputTensor = OnnxTensor.createTensor(env, arrayOf(inputIds.toLongArray()))
            val maskTensor = OnnxTensor.createTensor(env, arrayOf(attentionMask))
            val posTensor = OnnxTensor.createTensor(env, arrayOf(positionIds))

            val outputs = session?.run(mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor,
                "position_ids" to posTensor
            ))

            val logits = outputs?.get("logits")?.value as? Array<Array<FloatArray>>
            if (logits == null) {
                lastError = "推理未返回有效输出"
                return null
            }

            // 取每个位置的最高概率 token
            val generatedIds = mutableListOf<Long>()
            for (i in logits[0].indices) {
                val probs = logits[0][i]
                val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: 0
                generatedIds.add(maxIndex.toLong())
            }

            // 解码生成的 token
            val result = tok.decode(generatedIds.toLongArray())
            // 移除原始 prompt 部分
            result.removePrefix(prompt).trim()
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
