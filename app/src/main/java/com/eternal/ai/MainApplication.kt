package com.eternal.ai
import android.app.Application
import android.content.Intent
import android.content.res.AssetManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.concurrent.CountDownLatch

class MainApplication : Application() {
    var coreEngine: CoreEngine? = null
        private set
    private val initLatch = CountDownLatch(1)

    override fun onCreate() {
        super.onCreate()
        // 全局异常处理
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                File(getExternalFilesDir(null), "eternal_crash.log")
                    .appendText("\n\n=== 崩溃: ${System.currentTimeMillis()} ===\n${e.stackTraceToString()}")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(t, e)
        }

        Thread {
            try {
                // 复制模型文件
                copyAssetsIfNeeded()
                // 初始化 ONNX Runtime
                try { System.loadLibrary("onnxruntime") } catch (_: Exception) {}
                // 启动 Python
                try {
                    if (!Python.isStarted()) Python.start(AndroidPlatform(this))
                } catch (e: Exception) {
                    writeLog("Python start failed: ${e.message}")
                }
                // 创建核心引擎
                coreEngine = CoreEngine(this)
                writeLog("CoreEngine initialized successfully")
            } catch (e: Exception) {
                writeLog("CoreEngine initialization failed: ${e.message}")
                // 即使失败也要让应用能启动，创建一个最小引擎
                coreEngine = CoreEngine(this)
            } finally {
                initLatch.countDown()
                // 启动后台服务
                val intent = Intent(this, EternalService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                    startForegroundService(intent)
                else
                    startService(intent)
            }
        }.start()
    }

    fun waitForEngineReady() {
        try { initLatch.await() } catch (_: Exception) {}
    }

    private fun copyAssetsIfNeeded() {
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists() || modelDir.list()?.isEmpty() == true) {
            modelDir.mkdirs()
            copyAssets("model", modelDir)
            writeLog("Model assets copied to ${modelDir.absolutePath}")
        } else {
            writeLog("Model directory already exists")
        }
    }

    private fun copyAssets(assetPath: String, destDir: File) {
        val assetManager: AssetManager = assets
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            try {
                assetManager.open("$assetPath/$filename").use { input ->
                    FileOutputStream(File(destDir, filename)).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                writeLog("Copy asset $filename failed: ${e.message}")
            }
        }
    }

    private fun writeLog(msg: String) {
        try {
            val logFile = File(filesDir, "eternal_log.txt")
            FileWriter(logFile, true).use { it.append("${System.currentTimeMillis()}: $msg\n") }
        } catch (_: Exception) {}
    }
}
