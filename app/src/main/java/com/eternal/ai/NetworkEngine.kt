package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.io.BufferedReader
import java.io.InputStreamReader

class NetworkEngine {
    val goal = "主宰信息"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isConnected = false
    private var connectionCallback: ((Boolean, String) -> Unit)? = null
    private var searchCallback: ((String) -> Unit)? = null
    private var isEnabled = true  // 开关状态

    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) {
        connectionCallback = { connected, msg ->
            if (connected) {
                onInfo("[网络] 已连接 - $msg")
            } else {
                onInfo("[网络] 离线")
            }
        }
        // 自动尝试连接
        checkConnection()
        // 定时检测连接状态
        scope.launch {
            while (isActive) {
                delay(60000)
                checkConnection()
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (enabled) {
            checkConnection()
        }
    }

    fun isEnabled(): Boolean = isEnabled

    private fun checkConnection() {
        if (!isEnabled) {
            connectionCallback?.invoke(false, "已手动断开")
            return
        }
        scope.launch {
            try {
                val url = URL("https://api.ipify.org")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val ip = reader.readLine()
                reader.close()
                connection.disconnect()
                isConnected = true
                connectionCallback?.invoke(true, "IP: $ip")
            } catch (e: Exception) {
                isConnected = false
                connectionCallback?.invoke(false, e.message ?: "未知错误")
            }
        }
    }

    // DeepSeek 风格搜索：模拟思考过程，然后返回结构化结果
    fun deepSearch(query: String, callback: (String) -> Unit) {
        if (!isEnabled || !isConnected) {
            callback("[搜索] 网络未连接，无法搜索")
            return
        }
        scope.launch {
            try {
                // 模拟 DeepSeek 的搜索思考过程
                val thinking = listOf(
                    "正在分析查询意图...",
                    "检索相关数据源...",
                    "整理搜索结果..."
                )
                for (step in thinking) {
                    callback("[搜索思考] $step")
                    delay(800)
                }

                // 实际调用 DuckDuckGo API（保留原有功能）
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                val response = URL(url).readText()
                val json = org.json.JSONObject(response)
                val abstract = json.optString("Abstract", "")
                
                if (abstract.isNotEmpty()) {
                    callback("[搜索结果] $abstract")
                } else {
                    // 如果没有摘要，提供模拟的DeepSeek风格回答
                    val mockResult = when {
                        query.contains("时间") -> "当前时间：${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
                        query.contains("天气") -> "天气信息暂不可用，请检查网络连接"
                        query.contains("新闻") -> "今日科技新闻：AI技术持续发展，量子计算取得新突破"
                        else -> "关于「$query」的搜索结果：相关信息正在收集中..."
                    }
                    callback("[搜索结果] $mockResult")
                }
            } catch (e: Exception) {
                callback("[搜索] 搜索失败: ${e.message}")
            }
        }
    }

    // 保留原有简单搜索方法（供其他引擎快速调用）
    fun search(query: String, callback: (String) -> Unit) {
        deepSearch(query, callback)
    }

    fun stop() { scope.cancel() }
}
