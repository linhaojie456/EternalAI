package com.eternal.ai

import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

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
            onStatus?.invoke("离线")
        }
    }

    fun isEnabled(): Boolean = enabled

    private fun checkConnection() {
        if (!enabled) {
            onStatus?.invoke("离线")
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
                    connected = true
                    onStatus?.invoke("已连接 IP: $ip")
                } else {
                    connected = false
                    onStatus?.invoke("离线")
                }
                conn.disconnect()
            } catch (e: Exception) {
                connected = false
                onStatus?.invoke("离线")
            }
        }
    }

    fun stop() { scope.cancel() }
}
