package com.eternal.ai

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.io.File

class TokenizerHelper(modelDir: File) {
    private val tokenizer: HuggingFaceTokenizer = HuggingFaceTokenizer.newInstance(modelDir.toPath())

    fun encode(text: String): LongArray = tokenizer.encode(text).ids

    fun decode(ids: LongArray): String = tokenizer.decode(ids)

    // DJL 0.27.0 中 eosTokenId 是 HuggingFaceTokenizer 的直接属性
    val eosTokenId: Long = tokenizer.eosTokenId
}
