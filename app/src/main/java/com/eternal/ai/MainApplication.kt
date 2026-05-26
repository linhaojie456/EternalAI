package com.eternal.ai

import android.app.Application
import android.content.res.AssetManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 👇 注册全局异常捕获，防止未知崩溃
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, e ->
            try {
                val logFile = File(getExternalFilesDir(null), "eternal_crash.log")
                logFile.writeText("崩溃时间: ${System.currentTimeMillis()}\n异常: ${e.message}\n堆栈: ${e.stackTraceToString()}")
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, e)
        }
        
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
            // 👇 将模型文件复制到 filesDir/model，并增加错误处理
            try {
                val modelDir = File(filesDir, "model")
                if (!modelDir.exists()) {
                    modelDir.mkdirs()
                    copyAssets("model", modelDir)
                }
            } catch (e: Exception) {
                // 如果复制失败，记录到日志文件，方便排查
                try {
                    val logFile = File(getExternalFilesDir(null), "eternal_error.log")
                    logFile.writeText("模型复制失败: ${e.message}\n")
                } catch (_: Exception) {}
            }
        }
    }

    private fun copyAssets(assetPath: String, destDir: File) {
        val assetManager: AssetManager = assets
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            try {
                val inStream = assetManager.open("$assetPath/$filename")
                val outFile = File(destDir, filename)
                FileOutputStream(outFile).use { out ->
                    inStream.copyTo(out)
                }
                inStream.close()
            } catch (e: IOException) {
                // 忽略单个文件复制失败，继续复制其他文件
            }
        }
    }
}
