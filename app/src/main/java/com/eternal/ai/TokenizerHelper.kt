package com.eternal.ai

import android.content.Context
import java.io.File

class TokenizerHelper(private val modelDir: File) {
    private val vocab = mutableMapOf<String, Long>()
    private val idToToken = mutableMapOf<Long, String>()
    var eosTokenId: Long = 151643L  // 默认值，将从配置文件中读取
    var loadError: String? = null
        private set

    init {
        try {
            val tokenizerFile = File(modelDir, "tokenizer.json")
            val configFile = File(modelDir, "tokenizer_config.json")
            
            // 解析 tokenizer.json 获取词表
            if (tokenizerFile.exists()) {
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
            }
            
            // 从 tokenizer_config.json 读取 eos_token_id
            if (configFile.exists()) {
                val configContent = configFile.readText()
                val eosPattern = "\"eos_token_id\":\\s*(\\d+)".toRegex()
                val matchResult = eosPattern.find(configContent)
                if (matchResult != null) {
                    val id = matchResult.groupValues[1].toLongOrNull()
                    if (id != null) eosTokenId = id
                }
            }
            
            loadError = if (vocab.isEmpty()) "未解析到词表" else null
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
}
