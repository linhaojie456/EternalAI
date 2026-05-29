package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class NetworkEngine {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var enabled = true
    private var connected = false
    private var onStatus: ((String) -> Unit)? = null

    fun start(coordinator: EngineCoordinator, onInfo: (String) -> Unit) {
        onStatus = onInfo
        checkConnection()
        scope.launch {
            while (isActive) {
                delay(30000)
                checkConnection()
            }
        }
    }

    fun setEnabled(e: Boolean) {
        enabled = e
        if (e) checkConnection()
        else {
            connected = false
            onStatus?.invoke("[网络] 已手动断开")
        }
    }

    fun isEnabled(): Boolean = enabled

    private fun checkConnection() {
        if (!enabled) return
        scope.launch {
            try {
                val url = URL("https://api.ipify.org")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                val code = conn.responseCode
                if (code == 200) {
                    val ip = conn.inputStream.bufferedReader().readText()
                    connected = true
                    onStatus?.invoke("[网络] 已连接 IP: $ip")
                } else {
                    connected = false
                    onStatus?.invoke("[网络] 连接失败: HTTP $code")
                }
                conn.disconnect()
            } catch (e: Exception) {
                connected = false
                onStatus?.invoke("[网络] 离线")
            }
        }
    }

    fun deepSearch(query: String, callback: (String) -> Unit) {
        if (!enabled || !connected) {
            callback("[搜索] 网络未连接")
            return
        }
        scope.launch {
            callback("[搜索] 关于「$query」的结果：正在获取信息...")
        }
    }

    fun search(query: String, callback: (String) -> Unit) = deepSearch(query, callback)

    fun stop() { scope.cancel() }
}
