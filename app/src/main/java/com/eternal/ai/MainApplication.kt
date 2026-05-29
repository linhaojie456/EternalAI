package com.eternal.ai

import android.app.Application
import android.content.res.AssetManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 全局异常捕获，防止崩溃
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val log = File(getExternalFilesDir(null), "eternal_crash.log")
                log.appendText("\n\n=== 崩溃时间: ${System.currentTimeMillis()} ===\n")
                log.appendText("线程: ${t.name}\n异常: ${e.message}\n${e.stackTraceToString()}")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(t, e)
        }

        // 尝试加载 ONNX Runtime 原生库（非必需，失败不崩溃）
        try {
            System.loadLibrary("onnxruntime")
        } catch (_: UnsatisfiedLinkError) {}

        // 启动 Python 环境（非必需，失败不崩溃）
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 复制模型和基因组文件（失败不崩溃）
        try {
            copyAssetsIfNeeded()
        } catch (_: Exception) {}
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
                FileOutputStream(genomeFile).use { output ->
                    input.copyTo(output)
                }
            }
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
            } catch (_: Exception) {}
        }
    }
}
