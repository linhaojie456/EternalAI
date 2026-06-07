package com.eternal.ai
import java.io.File
class TokenizerHelper(private val modelDir: File) {
    private val vocab = mutableMapOf<String, Long>(); private val idToToken = mutableMapOf<Long, String>()
    var eosTokenId: Long = 151643L; var loadError: String? = null
    init {
        try {
            val tokenizerFile = File(modelDir, "tokenizer.json")
            if (tokenizerFile.exists()) {
                val content = tokenizerFile.readText()
                val vocabStart = content.indexOf("\"vocab\":")
                if (vocabStart >= 0) {
                    val vocabSection = content.substring(vocabStart)
                    val openingBrace = vocabSection.indexOf('{')
                    if (openingBrace >= 0) {
                        var braceCount = 0; val sb = StringBuilder()
                        for (i in openingBrace until vocabSection.length) { val c = vocabSection[i]; sb.append(c); if (c == '{') braceCount++ else if (c == '}') { braceCount--; if (braceCount == 0) break } }
                        val vocabJson = sb.toString(); val entries = vocabJson.split(",")
                        for (entry in entries) { val colon = entry.indexOf(':'); if (colon >= 0) { val token = entry.substring(0, colon).trim().removeSurrounding("\""); val idStr = entry.substring(colon + 1).trim(); try { val id = idStr.toLong(); vocab[token] = id; idToToken[id] = token } catch (_: Exception) {} } }
                    }
                }
            }
            val configFile = File(modelDir, "tokenizer_config.json")
            if (configFile.exists()) { val configContent = configFile.readText(); val eosPattern = "\"eos_token_id\":\\s*(\\d+)".toRegex(); val matchResult = eosPattern.find(configContent); if (matchResult != null) { val id = matchResult.groupValues[1].toLongOrNull(); if (id != null) eosTokenId = id } }
            loadError = if (vocab.isEmpty()) "未解析到词表" else null
        } catch (e: Exception) { loadError = "分词器初始化失败: ${e.message}" }
    }
    fun encode(text: String): LongArray { val ids = mutableListOf<Long>(); var remaining = text; while (remaining.isNotEmpty()) { var found = false; for ((token, id) in vocab) { if (remaining.startsWith(token)) { ids.add(id); remaining = remaining.substring(token.length); found = true; break } }; if (!found) { ids.add(remaining[0].code.toLong()); remaining = remaining.substring(1) } }; return ids.toLongArray() }
    fun decode(ids: LongArray): String { val sb = StringBuilder(); for (id in ids) { val token = idToToken[id]; if (token != null) sb.append(token) else sb.append(id.toChar()) }; return sb.toString() }
}
