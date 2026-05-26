package com.eternal.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.io.File

class TokenizerHelper(modelDir: File) {
    private val tokenizer: HuggingFaceTokenizer = HuggingFaceTokenizer.newInstance(modelDir.toPath())

    fun encode(text: String): LongArray {
        val encoding = tokenizer.encode(text)
        return encoding.ids
    }

    fun decode(ids: LongArray): String {
        return tokenizer.decode(ids)
    }

    fun eosTokenId(): Long {
        return 151643L
    }
}
