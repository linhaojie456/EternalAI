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
                delay(60000) // 每分钟检查一次连接
                checkConnection()
            }
        }
    }

    fun setEnabled(e: Boolean) {
        enabled = e
        if (e) checkConnection()
        else {
            connected = false
            onStatus?.invoke("离线")
        }
    }

    fun isEnabled(): Boolean = enabled

    fun search(query: String, callback: (String) -> Unit) {
        if (!enabled || !connected) { callback("网络未连接"); return }
        scope.launch {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1"
                val response = URL(url).readText()
                val json = org.json.JSONObject(response)
                val abstract = json.optString("Abstract", "")
                callback(abstract.ifEmpty { "未找到相关信息" })
            } catch (e: Exception) { callback("搜索失败: ${e.message}") }
        }
    }

    private fun checkConnection() {
        if (!enabled) { onStatus?.invoke("离线"); return }
        scope.launch {
            try {
                val url = URL("https://api.ipify.org")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000; conn.readTimeout = 5000
                connected = conn.responseCode == 200
                onStatus?.invoke(if (connected) "已连接" else "离线")
                conn.disconnect()
            } catch (e: Exception) {
                connected = false
                onStatus?.invoke("离线")
            }
        }
    }

    fun stop() { scope.cancel() }
}
