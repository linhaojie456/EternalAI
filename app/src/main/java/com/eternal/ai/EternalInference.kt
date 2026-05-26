package com.eternal.ai

import android.content.Context
import ai.onnxruntime.*
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.*

class EternalInference private constructor(private val session: OrtSession, private val tokenizer: EternalTokenizer) {
    
    companion object {
        fun create(context: Context): EternalInference {
            val modelFile = context.filesDir.resolve("model").resolve("model.onnx")
            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            val session = env.createSession(modelFile.absolutePath, sessionOptions)
            val tokenizer = EternalTokenizer(context.filesDir.resolve("model"))
            return EternalInference(session, tokenizer)
        }
    }

    fun generate(prompt: String, maxTokens: Int = 100): String {
        val inputIds = tokenizer.encode(prompt)
        val attentionMask = LongArray(inputIds.size) { 1 }

        val generated = mutableListOf<Long>()

        for (i in 0 until maxTokens) {
            val inputTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), arrayOf(longArrayOf(*inputIds.toLongArray())))
            val maskTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), arrayOf(longArrayOf(attentionMask)))

            val inputs = mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor
            )

            val outputs = session.run(inputs)
            val logits = outputs.get("logits").get().value as Array<Array<FloatArray>>
            val nextTokenLogits = logits[0][logits[0].size - 1]

            // 贪婪采样
            val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break

            if (nextToken == tokenizer.eosTokenId) break

            generated.add(nextToken)

            // 更新输入
            inputIds.add(nextToken)
            attentionMask += 1L
        }

        val text = tokenizer.decode(inputIds.toLongArray())
        // 移除 prompt 部分
        return text.removePrefix(prompt).trim()
    }
}
