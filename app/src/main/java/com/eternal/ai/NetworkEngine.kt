package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class NetworkEngine {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(onInfo: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                // 周期性获取本机公网IP
                try {
                    val ip = URL("https://api.ipify.org").readText()
                    onInfo("[网络] 公网IP: $ip")
                } catch (e: Exception) {
                    onInfo("[网络] 无法连接网络")
                }
                delay(120000) // 每两分钟
            }
        }
        // 启动一个后台任务，定期获取天气示例
        scope.launch {
            delay(5000)
            while (isActive) {
                try {
                    val weatherJson = URL("https://wttr.in/?format=j1").readText()
                    onInfo("[网络] 天气数据已更新")
                } catch (_: Exception) {}
                delay(600000) // 10分钟
            }
        }
    }

    // 公开的搜索方法（可由其他组件调用）
    fun search(query: String, callback: (String) -> Unit) {
        scope.launch {
            try {
                // 使用 DuckDuckGo Instant Answer API (非官方，但简单)
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                val response = URL(url).readText()
                // 提取 Abstract 或 RelatedTopics
                val json = org.json.JSONObject(response)
                val abstract = json.optString("Abstract", "")
                if (abstract.isNotEmpty()) {
                    callback("[搜索] $abstract")
                } else {
                    // 如果没摘要，返回相关主题的第一个
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
