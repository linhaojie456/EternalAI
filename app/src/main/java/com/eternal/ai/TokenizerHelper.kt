package com.eternal.ai

import com.chaquo.python.Python

class TokenizerHelper {
    private val pyModule = Python.getInstance().getModule("tokenizer_helper")

    fun encode(text: String): LongArray {
        val result = pyModule.callAttr("encode", text) as List<*>
        return result.map { (it as Number).toLong() }.toLongArray()
    }

    fun decode(ids: LongArray): String {
        // 转换为 Python 列表
        val pyList = listOf(*ids.toTypedArray())
        return pyModule.callAttr("decode", pyList) as String
    }

    fun eosTokenId(): Long {
        return (pyModule.callAttr("eos_token_id") as Number).toLong()
    }
}
