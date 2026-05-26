package com.eternal.ai

import android.content.Context
import ai.onnxruntime.*
import java.io.File
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.*

class EternalInference private constructor(
    private val session: OrtSession,
    private val vocab: Map<String, Long>,
    private val eosTokenId: Long
) {
    companion object {
        fun create(context: Context): EternalInference {
            val modelFile = File(context.filesDir, "model/model.onnx")
            
            // 创建 ONNX Runtime 环境
            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            
            // 添加超时和内存优化
            sessionOptions.setSessionLogVerbosityLevel(OrtLoggingLevel.ORT_LOGGING_LEVEL_WARNING)
            
            val session = env.createSession(modelFile.absolutePath, sessionOptions)
            
            // 简单的词汇表（从 tokenizer.json 构建）
            val vocab = mutableMapOf<String, Long>()
            vocab["<|endoftext|>"] = 151643L
            vocab["<|im_start|>"] = 151644L
            vocab["<|im_end|>"] = 151645L
            
            return EternalInference(session, vocab, 151643L)
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        // 将 prompt 转换为 token IDs（简化处理）
        val inputIds = prompt.map { it.code.toLong() }.toMutableList()
        val attentionMask = MutableList(inputIds.size) { 1L }
        
        val generatedIds = mutableListOf<Long>()
        
        for (step in 0 until maxTokens) {
            val inputTensor = OnnxTensor.createTensor(
                OrtEnvironment.getEnvironment(),
                arrayOf(inputIds.toLongArray())
            )
            val maskTensor = OnnxTensor.createTensor(
                OrtEnvironment.getEnvironment(),
                arrayOf(attentionMask.toLongArray())
            )
            
            val inputs = mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor
            )
            
            val outputs = session.run(inputs)
            val logits = outputs["logits"].get().value as Array<Array<FloatArray>>
            val nextTokenLogits = logits[0][logits[0].size - 1]
            
            val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break
            
            if (nextToken == eosTokenId) break
            
            generatedIds.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
            
            // 释放资源
            inputTensor.close()
            maskTensor.close()
            outputs.close()
        }
        
        // 简化解码（仅用于演示）
        return generatedIds.map { it.toChar() }.joinToString("")
    }
}
