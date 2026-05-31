package com.eternal.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
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

    // 硬编码 EOS token ID
    companion object {
        const val EOS_TOKEN_ID: Long = 151643L
    }
}
