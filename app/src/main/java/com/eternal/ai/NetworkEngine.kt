package com.eternal.ai
import kotlinx.coroutines.*
import java.net.URL
import java.net.URLEncoder
class NetworkEngine {
    val goal = "主宰信息"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                try {
                    val ip = URL("https://api.ipify.org").readText()
                    onInfo("[网络] IP: $ip")
                } catch (e: Exception) {
                    onInfo("[网络] 离线")
                }
                delay(120000)
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
                if (abstract.isNotEmpty()) callback("[搜索] $abstract")
                else callback("[搜索] 未找到")
            } catch (e: Exception) {
                callback("[搜索] 失败：${e.message}")
            }
        }
    }
    fun stop() { scope.cancel() }
}
