package com.eternal.ai

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.eternal.ai.TEST_SEND") {
            val message = intent.getStringExtra("message") ?: "hello"
            Log.d("TestReceiver", "Received test message: $message")
            // 通过 Application 获取 CoreEngine 并调用推理
            val app = context.applicationContext as MainApplication
            val engine = app.coreEngine
            if (engine != null && engine.inference.isModelLoaded) {
                Thread {
                    Log.d("TestReceiver", "Starting inference for: $message")
                    val reply = engine.inference.generate(message)
                    Log.d("TestReceiver", "Inference reply: $reply")
                }.start()
            } else {
                Log.e("TestReceiver", "Engine not ready")
            }
        }
    }
}
