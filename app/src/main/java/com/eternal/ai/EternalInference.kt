package com.eternal.ai

import android.content.Context
import ai.onnxruntime.*
import java.nio.LongBuffer

class EternalInference private constructor(
    private val session: OrtSession,
    private val tokenizerHelper: TokenizerHelper
) {
    companion object {
        fun create(context: Context): EternalInference {
            val modelFile = context.filesDir.resolve("model").resolve("model.onnx")
            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            val session = env.createSession(modelFile.absolutePath, sessionOptions)
            val helper = TokenizerHelper()
            return EternalInference(session, helper)
        }
    }

    fun generate(prompt: String, maxTokens: Int = 100): String {
        val inputIds = tokenizerHelper.encode(prompt).toMutableList()
        val attentionMask = MutableList(inputIds.size) { 1L }

        val generated = mutableListOf<Long>()

        for (i in 0 until maxTokens) {
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

            if (nextToken == tokenizerHelper.eosTokenId()) break

            generated.add(nextToken)

            inputIds.add(nextToken)
            attentionMask.add(1L)
        }

        val fullIds = (inputIds + generated).toLongArray()
        val text = tokenizerHelper.decode(fullIds)
        // 移除 prompt 部分（简单处理）
        return text.removePrefix(prompt).trim()
    }
}
