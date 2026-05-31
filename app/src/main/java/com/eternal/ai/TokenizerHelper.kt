package com.eternal.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.io.File

class TokenizerHelper(modelDir: File) {
    private val tokenizer: HuggingFaceTokenizer = HuggingFaceTokenizer.newInstance(modelDir.toPath())

    fun encode(text: String): LongArray = tokenizer.encode(text).ids

    fun decode(ids: LongArray): String = tokenizer.decode(ids)

    // 尝试获取 eosTokenId，失败则返回默认值 151643
    val eosTokenId: Long by lazy {
        try {
            javaClass.getMethod("getEosTokenId").invoke(this) as? Long ?: 151643L
        } catch (e: Exception) {
            151643L
        }
    }
}
