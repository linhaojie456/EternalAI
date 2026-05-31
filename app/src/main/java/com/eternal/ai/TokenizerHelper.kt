package com.eternal.ai

import ai.djl.huggingface.tokeners.HuggingFaceTokenizer
import java.io.File

class TokenizerHelper(modelDir: File) {
    var loadError: String? = null
    val tokenizer: HuggingFaceTokenizer? = try {
        HuggingFaceTokenizer.newInstance(modelDir.toPath())
    } catch (e: Exception) {
        loadError = "分词器加载失败: ${e.message}"
        null
    }

    fun encode(text: String): LongArray = tokenizer?.encode(text)?.ids ?: LongArray(0)
    fun decode(ids: LongArray): String = tokenizer?.decode(ids) ?: ""
    val eosTokenId: Long by lazy {
        try {
            tokenizer?.eosTokenId ?: 151643L
        } catch (e: Exception) {
            151643L
        }
    }
}
