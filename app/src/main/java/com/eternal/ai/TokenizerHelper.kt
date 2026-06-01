package com.eternal.ai

import com.chaquo.python.Python

class TokenizerHelper {
    private val module = Python.getInstance().getModule("tokenizer_helper")

    fun encode(text: String): LongArray {
        return try {
            val result = module.callAttr("encode", text)
            val list = result.asList()
            list.map { (it as Number).toLong() }.toLongArray()
        } catch (e: Exception) {
            e.printStackTrace()
            LongArray(0)
        }
    }

    fun decode(ids: LongArray): String {
        return try {
            module.callAttr("decode", ids.toList()).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    companion object {
        const val EOS_TOKEN_ID: Long = 151643L
    }
}
