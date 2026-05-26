package com.eternal.ai

import android.content.Context
import ai.onnxruntime.*
import java.io.File

class EternalInference private constructor(
    private val session: OrtSession,
    private val tokenizer: TokenizerHelper
) {
    companion object {
        fun create(context: Context): EternalInference {
            val modelFile = File(context.filesDir, "model/model.onnx")
            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            val session = env.createSession(modelFile.absolutePath, sessionOptions)
            val tokenizerHelper = TokenizerHelper(File(context.filesDir, "model"))
            return EternalInference(session, tokenizerHelper)
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        val inputIds = tokenizer.encode(prompt).toMutableList()
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
            val logits = (outputs["logits"].get().value as Array<Array<FloatArray>>)
            val nextTokenLogits = logits[0][logits[0].size - 1]

            val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break

            if (nextToken == tokenizer.eosTokenId()) break

            generatedIds.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
        }

        val fullIds = (inputIds + generatedIds).toLongArray()
        val fullText = tokenizer.decode(fullIds)
        // 移除 prompt 部分，返回纯回复
        return fullText.removePrefix(prompt).trim()
    }
}
