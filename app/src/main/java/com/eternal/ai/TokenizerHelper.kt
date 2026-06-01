package com.eternal.ai

import android.content.Context
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class TokenizerHelper(private val modelDir: File) {
    private val vocab = mutableMapOf<String, Long>()
    private val idToToken = mutableMapOf<Long, String>()
    var loadError: String? = null
        private set

    init {
        try {
            val tokenizerFile = File(modelDir, "tokenizer.json")
            if (!tokenizerFile.exists()) {
                loadError = "tokenizer.json 不存在"
            } else {
                // 读取 tokenizer.json 并解析 vocab
                val content = tokenizerFile.readText()
                val vocabStart = content.indexOf("\"vocab\":")
                if (vocabStart >= 0) {
                    val vocabSection = content.substring(vocabStart)
                    val openingBrace = vocabSection.indexOf('{')
                    if (openingBrace >= 0) {
                        var braceCount = 0
                        val sb = StringBuilder()
                        for (i in openingBrace until vocabSection.length) {
                            val c = vocabSection[i]
                            sb.append(c)
                            if (c == '{') braceCount++
                            else if (c == '}') {
                                braceCount--
                                if (braceCount == 0) break
                            }
                        }
                        val vocabJson = sb.toString()
                        // 解析每个 "token": id 对
                        val entries = vocabJson.split(",")
                        for (entry in entries) {
                            val colon = entry.indexOf(':')
                            if (colon >= 0) {
                                var token = entry.substring(0, colon).trim().removeSurrounding("\"")
                                val idStr = entry.substring(colon + 1).trim()
                                try {
                                    val id = idStr.toLong()
                                    vocab[token] = id
                                    idToToken[id] = token
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
                loadError = if (vocab.isEmpty()) "未解析到词表" else null
            }
        } catch (e: Exception) {
            loadError = "分词器初始化失败: ${e.message}"
        }
    }

    fun encode(text: String): LongArray {
        val ids = mutableListOf<Long>()
        var remaining = text
        while (remaining.isNotEmpty()) {
            var found = false
            for ((token, id) in vocab) {
                if (remaining.startsWith(token)) {
                    ids.add(id)
                    remaining = remaining.substring(token.length)
                    found = true
                    break
                }
            }
            if (!found) {
                // 使用单个字符的 ASCII 值作为 fallback
                ids.add(remaining[0].code.toLong())
                remaining = remaining.substring(1)
            }
        }
        return ids.toLongArray()
    }

    fun decode(ids: LongArray): String {
        val sb = StringBuilder()
        for (id in ids) {
            val token = idToToken[id]
            if (token != null) {
                sb.append(token)
            } else {
                sb.append(id.toChar())
            }
        }
        return sb.toString()
    }

    companion object {
        const val EOS_TOKEN_ID: Long = 151643L
    }
}
