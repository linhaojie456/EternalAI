package com.eternal.ai
import kotlinx.coroutines.*
import java.net.*
class InformationEngine {
    val goal = "频率和数字的统一"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var enabled = true
    private var connected = false
    private var onStatus: ((String) -> Unit)? = null
    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) { onStatus = onInfo; check(); scope.launch { while (isActive) { delay(60000); check() } } }
    fun setEnabled(e: Boolean) { enabled = e; if (e) check() else { connected = false; onStatus?.invoke("离线") } }
    fun isEnabled() = enabled
    fun search(query: String, callback: (String) -> Unit) {
        if (!enabled || !connected) { callback("网络未连接"); return }
        scope.launch {
            try {
                val u = URL("https://api.duckduckgo.com/?q=${URLEncoder.encode(query, "UTF-8")}&format=json&no_html=1")
                val j = org.json.JSONObject(u.readText())
                callback(j.optString("Abstract", "未找到"))
            } catch (e: Exception) { callback("搜索失败") }
        }
    }
    private fun check() {
        if (!enabled) { onStatus?.invoke("离线"); return }
        scope.launch { try { connected = (URL("https://api.ipify.org").openConnection() as HttpURLConnection).responseCode == 200 } catch (_: Exception) { connected = false }; onStatus?.invoke(if (connected) "已连接" else "离线") }
    }
    fun stop() { scope.cancel() }
}
