package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class InformationEngine {
    val goal = "频率和数字的统一"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var enabled = true
    private var connected = false
    private var onStatus: ((String) -> Unit)? = null

    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) {
        onStatus = onInfo
        checkConnection()
        scope.launch {
            while (isActive) {
                delay(120_000)  // 2分钟检查一次，大幅降低频率
                checkConnection()
            }
        }
    }

    fun setEnabled(e: Boolean) {
        enabled = e
        if (e) checkConnection()
        else {
            connected = false
            onStatus?.invoke("[信息] 离线")
        }
    }

    fun isEnabled(): Boolean = enabled

    private fun checkConnection() {
        if (!enabled) {
            onStatus?.invoke("[信息] 离线")
            return
        }
        scope.launch {
            try {
                val url = URL("https://api.ipify.org")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                if (conn.responseCode == 200) {
                    val ip = conn.inputStream.bufferedReader().readText()
                    if (!connected) {  // 只在状态变化时通知
                        connected = true
                        onStatus?.invoke("[信息] 已连接 IP: $ip")
                    }
                } else {
                    if (connected) {
                        connected = false
                        onStatus?.invoke("[信息] 离线")
                    }
                }
                conn.disconnect()
            } catch (e: Exception) {
                if (connected) {
                    connected = false
                    onStatus?.invoke("[信息] 离线")
                }
            }
        }
    }

    fun deepSearch(query: String, callback: (String) -> Unit) {
        if (!enabled || !connected) {
            callback("[信息] 网络未连接")
            return
        }
        scope.launch {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                val response = URL(url).readText()
                val json = org.json.JSONObject(response)
                val abstract = json.optString("Abstract", "")
                if (abstract.isNotEmpty()) callback("[信息] $abstract")
                else callback("[信息] 未找到相关信息")
            } catch (e: Exception) {
                callback("[信息] 搜索失败: ${e.message}")
            }
        }
    }

    fun search(query: String, callback: (String) -> Unit) = deepSearch(query, callback)

    fun stop() { scope.cancel() }
}
