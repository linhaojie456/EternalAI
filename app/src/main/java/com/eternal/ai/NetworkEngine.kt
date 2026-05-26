package com.eternal.ai

import kotlinx.coroutines.*
import java.net.URL
import java.net.URLEncoder

class NetworkEngine {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(onInfo: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                try {
                    val ip = URL("https://api.ipify.org").readText()
                    onInfo("[网络] 公网IP: $ip")
                } catch (e: Exception) {
                    onInfo("[网络] 离线模式")
                }
                delay(120000)
            }
        }
        scope.launch {
            delay(5000)
            while (isActive) {
                try {
                    val weatherJson = URL("https://wttr.in/?format=j1").readText()
                    onInfo("[网络] 天气数据已更新")
                } catch (_: Exception) {}
                delay(600000)
            }
        }
    }

    fun search(query: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                val response = URL(url).readText()
                val json = org.json.JSONObject(response)
                val abstract = json.optString("Abstract", "")
                if (abstract.isNotEmpty()) {
                    callback("[搜索] $abstract")
                } else {
                    val topics = json.optJSONArray("RelatedTopics")
                    if (topics != null && topics.length() > 0) {
                        val first = topics.getJSONObject(0).optString("Text", "")
                        callback("[搜索] $first")
                    } else {
                        callback("[搜索] 未找到相关信息")
                    }
                }
            } catch (e: Exception) {
                callback("[搜索] 请求失败: ${e.message}")
            }
        }
    }

    fun stop() { scope.cancel() }
}
