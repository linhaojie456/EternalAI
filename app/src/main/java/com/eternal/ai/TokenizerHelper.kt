package com.eternal.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.io.File

class TokenizerHelper(private val modelDir: File) {
    private val tokenizer: HuggingFaceTokenizer = HuggingFaceTokenizer.newInstance(modelDir.toPath())

    fun encode(text: String): LongArray = tokenizer.encode(text).ids
    fun decode(ids: LongArray): String = tokenizer.decode(ids)
    fun eosTokenId(): Long = tokenizer.getTokenizer().eosTokenId
}
