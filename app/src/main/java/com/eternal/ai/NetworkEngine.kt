package com.eternal.ai

import kotlinx.coroutines.*
import java.net.URL

class NetworkEngine {
    val goal = "主宰信息"
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun start(onInfo: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                try {
                    val ip = URL("https://api.ipify.org").readText()
                    onInfo("[网络] 目标：$goal | 公网IP: $ip")
                } catch (e: Exception) {
                    onInfo("[网络] 目标：$goal | 离线模式")
                }
                delay(120000)
            }
        }
        scope.launch {
            delay(5000)
            while (isActive) {
                try {
                    URL("https://wttr.in/?format=j1").readText()
                    onInfo("[网络] 目标：$goal | 天气数据已更新")
                } catch (_: Exception) {}
                delay(600000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
