package com.eternal.ai

import com.chaquo.python.Python

class TokenizerHelper {
    private val pyModule = Python.getInstance().getModule("tokenizer_helper")

    fun encode(text: String): LongArray {
        val result = pyModule.callAttr("encode", text)
        val list = result.asList() ?: return LongArray(0)
        return list.map { (it as Number).toLong() }.toLongArray()
    }

    fun decode(ids: LongArray): String {
        return pyModule.callAttr("decode", ids.toList()).toString()
    }

    fun eosTokenId(): Long {
        val result = pyModule.callAttr("eos_token_id")
        return (result as? Number)?.toLong() ?: 151643L
    }
}
