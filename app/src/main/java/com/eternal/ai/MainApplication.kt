package com.eternal.ai

import android.app.Application
import android.content.Intent
import android.content.res.AssetManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val log = File(getExternalFilesDir(null), "eternal_crash.log")
                log.appendText("\n\n=== 崩溃时间: ${System.currentTimeMillis()} ===\n")
                log.appendText("异常: ${e.message}\n${e.stackTraceToString()}")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(t, e)
        }

        try { System.loadLibrary("onnxruntime") } catch (_: UnsatisfiedLinkError) {}
        try { if (!Python.isStarted()) Python.start(AndroidPlatform(this)) } catch (e: Exception) {}

        try {
            copyAssetsIfNeeded()
        } catch (_: Exception) {}

        // 启动前台服务以保持后台
        val intent = Intent(this, EternalService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun copyAssetsIfNeeded() {
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists() || modelDir.list()?.isEmpty() == true) {
            modelDir.mkdirs()
            copyAssets("model", modelDir)
        }

        val genomeFile = File(filesDir, "genome.py")
        if (!genomeFile.exists()) {
            assets.open("genome.py").use { input ->
                FileOutputStream(genomeFile).use { output -> input.copyTo(output) }
            }
        }
    }

    private fun copyAssets(assetPath: String, destDir: File) {
        val assetManager: AssetManager = assets
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            try {
                assetManager.open("$assetPath/$filename").use { input ->
                    FileOutputStream(File(destDir, filename)).use { output -> input.copyTo(output) }
                }
            } catch (_: Exception) {}
        }
    }
}
