package com.eternal.ai

import ai.onnxruntime.*
import android.content.Context
import java.io.File
import java.nio.LongBuffer

class InferenceEngine(private val context: Context) {
    private var session: OrtSession? = null
    private val env = OrtEnvironment.getEnvironment()

    fun loadModel(): Boolean {
        return try {
            val modelFile = File(context.filesDir, "model/model.onnx")
            val options = OrtSession.SessionOptions()
            session = env.createSession(modelFile.absolutePath, options)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun generate(inputIds: LongArray, attentionMask: LongArray, maxTokens: Int = 200): LongArray? {
        val sess = session ?: return null
        try {
            val generated = mutableListOf<Long>()
            var currentIds = inputIds.copyOf()
            var currentMask = attentionMask.copyOf()

            for (i in 0 until maxTokens) {
                val inputTensor = OnnxTensor.createTensor(env, arrayOf(currentIds))
                val maskTensor = OnnxTensor.createTensor(env, arrayOf(currentMask))

                val outputs = sess.run(mapOf("input_ids" to inputTensor, "attention_mask" to maskTensor))
                val logits = outputs["logits"].get().value as Array<Array<FloatArray>>
                val nextToken = argmax(logits[0].last())
                if (nextToken == 151643L) break // eos token

                generated.add(nextToken)
                currentIds = currentIds + nextToken
                currentMask = currentMask + 1L
            }
            return generated.toLongArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun argmax(array: FloatArray): Long = array.indices.maxByOrNull { array[it] }?.toLong() ?: 0
}
