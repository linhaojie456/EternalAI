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
            Log.d("InferenceEngine", "Checking model file: ${modelFile.absolutePath}, exists=${modelFile.exists()}, size=${modelFile.length()}")
            if (!modelFile.exists()) {
                initError("模型文件不存在: ${modelFile.absolutePath}")
                Log.e("InferenceEngine", "Model file not found!")
                return false
            }
            modelSize = modelFile.length()
            if (modelSize < 1_000_000) {
                initError("模型文件异常小: $modelSize 字节")
                Log.e("InferenceEngine", "Model file too small: $modelSize")
                return false
            }

            onProgress?.invoke(30, "创建 ONNX 会话")
            Log.d("InferenceEngine", "Creating ONNX session...")
            val options = OrtSession.SessionOptions()
            options.setCPUArenaAllocator(true)
            val startTime = System.currentTimeMillis()
            try {
                session = env.createSession(modelFile.absolutePath, options)
                val elapsed = System.currentTimeMillis() - startTime
                Log.d("InferenceEngine", "ONNX session created in ${elapsed}ms")
                writeLog("ONNX 会话创建成功 (${elapsed}ms)")
            } catch (e: Exception) {
                Log.e("InferenceEngine", "ONNX session creation failed", e)
                initError("ONNX 会话创建失败: ${e.javaClass.simpleName}: ${e.message}")
                return false
            }

            // 解析输入信息
            onProgress?.invoke(50, "解析模型结构")
            for (input in session!!.inputInfo.values) {
                if (input.name == "attention_mask") {
                    val tensorInfo = input.info as? TensorInfo
                    attentionMaskShape = tensorInfo?.shape
                    Log.d("InferenceEngine", "attention_mask shape: ${attentionMaskShape?.joinToString()}")
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
            Log.d("InferenceEngine", "numLayers=$numLayers, numKVHeads=$numKVHeads, headDim=$headDim")

            onProgress?.invoke(70, "加载分词器")
            Log.d("InferenceEngine", "Loading tokenizer...")
            tokenizer = TokenizerHelper(modelDir)
            tokenizer?.loadError?.let {
                initError("分词器加载失败: $it")
                Log.e("InferenceEngine", "Tokenizer load error: $it")
                return false
            }
            Log.d("InferenceEngine", "Tokenizer loaded, eosId=${tokenizer?.eosTokenId}")

            val testIds = tokenizer!!.encode("测试")
            if (testIds.isEmpty()) {
                initError("分词器测试失败")
                Log.e("InferenceEngine", "Tokenizer test failed")
                return false
            }
            Log.d("InferenceEngine", "Tokenizer test passed, sample ids count: ${testIds.size}")

            isModelLoaded = true
            loadStatus = "神格已激活 (${modelSize / (1024*1024)}MB, 层:$numLayers)"
            onProgress?.invoke(100, loadStatus)
            writeLog("模型加载成功，测试分词通过")
            Log.d("InferenceEngine", "神格已激活")   // 关键检测点
            return true
        } catch (e: Exception) {
            initError("${e.javaClass.simpleName}: ${e.message}")
            Log.e("InferenceEngine", "Load exception: ${e.javaClass.simpleName}: ${e.message}", e)
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
        Log.d("InferenceEngine", msg)
    }

    // 以下推理函数保持不变（省略完整实现，实际脚本中必须包含之前的所有生成函数）
    // 请确保以下函数存在：createEmptyPastKeyValues, extractLogits, sampleToken, createAttentionMaskTensor, generate, generateStream, start, stop
    // 为节省篇幅此处不重复，实际执行时务必使用之前完整的代码。
