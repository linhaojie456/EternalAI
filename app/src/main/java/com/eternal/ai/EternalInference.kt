package com.eternal.ai

import android.content.Context
import ai.onnxruntime.*
import java.io.File

class EternalInference private constructor(
    private val session: OrtSession,
    private val eosTokenId: Long
) {
    companion object {
        fun create(context: Context): EternalInference {
            val modelFile = File(context.filesDir, "model/model.onnx")
            val env = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setSessionLogVerbosityLevel(0)
            val session = env.createSession(modelFile.absolutePath, sessionOptions)
            return EternalInference(session, 151643L)
        }
    }

    fun generate(prompt: String, maxTokens: Int = 200): String {
        val inputIds = prompt.map { it.code.toLong() }.toMutableList()
        val attentionMask = MutableList(inputIds.size) { 1L }
        // 生成 position_ids，形状与 input_ids 相同，从 0 递增
        val positionIds = (0L until inputIds.size.toLong()).toList().toMutableList()

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
            val positionTensor = OnnxTensor.createTensor(
                OrtEnvironment.getEnvironment(),
                arrayOf(positionIds.toLongArray())
            )

            val inputs = mapOf(
                "input_ids" to inputTensor,
                "attention_mask" to maskTensor,
                "position_ids" to positionTensor
            )

            val outputs = session.run(inputs)
            val logits = (outputs["logits"].get().value as Array<Array<FloatArray>>)
            val nextTokenLogits = logits[0][logits[0].size - 1]

            val nextToken = nextTokenLogits.indices.maxByOrNull { nextTokenLogits[it] }?.toLong() ?: break

            if (nextToken == eosTokenId) break

            generatedIds.add(nextToken)
            inputIds.add(nextToken)
            attentionMask.add(1L)
            positionIds.add(positionIds.size.toLong())

            inputTensor.close()
            maskTensor.close()
            positionTensor.close()
            outputs.close()
        }

        return generatedIds.map { it.toChar() }.joinToString("")
    }
}
