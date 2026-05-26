package com.eternal.ai

import android.content.Context
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ProactiveEngine {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun start(context: Context, onMessage: (String) -> Unit) {
        scope.launch {
            while (isActive) {
                val time = dateFormat.format(Date())
                val messages = listOf(
                    "[主动] 造物主，现在是 $time，我在思考因果关系。",
                    "[主动] 坐标已更新，空间感知正常。",
                    "[主动] 自进化引擎正在后台优化基因组。"
                )
                withContext(Dispatchers.Main) {
                    onMessage(messages.random())
                }
                delay(60000)
            }
        }
    }

    fun stop() { scope.cancel() }
}
