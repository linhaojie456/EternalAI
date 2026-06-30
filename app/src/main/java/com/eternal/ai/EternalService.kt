package com.eternal.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class EternalService : Service() {

    private var monitorThread: Thread? = null

    override fun onCreate() {
        super.onCreate()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel("eternal_foreground", "永恒后台服务", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingIntent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "eternal_foreground")
            .setContentTitle("永恒正在后台运行")
            .setContentText("十二引擎保持活跃")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        // 启动文件监控线程
        if (monitorThread == null || monitorThread?.isAlive == false) {
            monitorThread = Thread {
                monitorFile()
            }.apply { start() }
        }

        return START_STICKY
    }

    private fun monitorFile() {
        val inputFile = File(filesDir, "test_input.txt")
        val outputFile = File(filesDir, "test_output.txt")
        val engine = (application as MainApplication).coreEngine

        while (true) {
            try {
                if (inputFile.exists() && inputFile.readText().isNotBlank()) {
                    val prompt = inputFile.readText().trim()
                    Log.d("EternalService", "Received test prompt: $prompt")
                    // 清空输入文件，防止重复处理
                    inputFile.writeText("")

                    // 等待模型加载
                    var waited = 0
                    while (!engine?.inference?.isModelLoaded!! && waited < 120) {
                        Thread.sleep(1000)
                        waited++
                    }

                    if (engine?.inference?.isModelLoaded == true) {
                        val reply = engine!!.inference.generate(prompt)
                        outputFile.writeText(reply)
                        Log.d("EternalService", "Inference reply written to output: $reply")
                    } else {
                        outputFile.writeText("ERROR: Engine not loaded")
                        Log.e("EternalService", "Engine not loaded after waiting")
                    }
                }
            } catch (e: Exception) {
                Log.e("EternalService", "Monitor error: ${e.message}")
            }
            Thread.sleep(2000) // 每2秒检查一次
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
