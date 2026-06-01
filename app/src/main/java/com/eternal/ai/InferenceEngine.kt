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

    // 创建空的 past_key_values 张量
    private fun createEmptyPastKeyValues(): Map<String, OnnxTensor> {
        // 根据模型结构，通常每层有 key 和 value，形状 (batch, num_kv_heads, seq_len, head_dim)
        val numLayers = 24  // Qwen 1.5B 有 24 层
        val numKVHeads = 4  // GQA: 4 KV heads
        val headDim = 128   // 1536 / 12 = 128
        val emptyShape = longArrayOf(1L, numKVHeads.toLong(), 0L, headDim.toLong())
        val emptyTensor = OnnxTensor.createTensor(env, FloatArray(0), emptyShape)
        
        val map = mutableMapOf<String, OnnxTensor>()
        for (i in 0 until numLayers) {
            map["past_key_values.$i.key"] = emptyTensor
            map["past_key_values.$i.value"] = emptyTensor
        }
        return map
    }

    fun generate(prompt: String, maxTokens: Int = 200): String? {
        if (!isModelLoaded) return null
        val tok = tokenizer ?: return null
        return try {
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

                val inputs = mutableMapOf<String, OnnxTensor>(
                    "input_ids" to inputTensor,
                    "attention_mask" to maskTensor,
                    "position_ids" to posTensor
                )
                inputs.putAll(pastKeyValues)

                val outputs = sess.run(inputs)
                val logits = outputs["logits"].get().value as Array<Array<FloatArray>>
                val nextTokenLogits = logits[0][logits[0].size - 1]
                val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break

                if (nextToken == TokenizerHelper.EOS_TOKEN_ID) break
                generated.add(nextToken)
                inputIds.add(nextToken)
                attentionMask.add(1L)
                positionIds.add(positionIds.size.toLong())

                // 更新 past_key_values（如果模型输出 present 张量）
                val newPast = mutableMapOf<String, OnnxTensor>()
                for ((key, value) in outputs) {
                    if (key.startsWith("present")) {
                        val parts = key.removePrefix("present.").split(".")
                        val layerIndex = parts[0].toIntOrNull() ?: continue
                        if (key.endsWith(".key")) {
                            newPast["past_key_values.$layerIndex.key"] = value
                        } else if (key.endsWith(".value")) {
                            newPast["past_key_values.$layerIndex.value"] = value
                        }
                    }
                }
                if (newPast.isNotEmpty()) {
                    pastKeyValues = newPast
                } else {
                    // 如果模型不输出 present，手动扩展缓存（简化）
                    for (layer in 0 until 24) {
                        val keyName = "past_key_values.$layer.key"
                        val valueName = "past_key_values.$layer.value"
                        val oldKey = pastKeyValues[keyName]
                        val oldValue = pastKeyValues[valueName]
                        if (oldKey != null && oldValue != null) {
                            val oldShape = oldKey.info.shape
                            // 无法直接修改形状，这里先保持原样
                        }
                    }
                }
            }

            val fullIds = (inputIds + generated).toLongArray()
            tok.decode(fullIds).removePrefix(prompt).trim()
        } catch (e: Exception) {
            lastError = "推理异常: ${e.message}"
            e.printStackTrace()
            null
        }
    }

    fun start(coordinator: EngineCoordinator, onStatus: (String) -> Unit) {
        loadModel()
        onStatus(if (isModelLoaded) "[推理] 模型已加载" else "[推理] 加载失败: ${lastError ?: "未知"}")
    }

    fun stop() { session?.close() }
}
