package com.eternal.ai
import android.app.Application
import android.content.Intent
import android.content.res.AssetManager
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainApplication : Application() {
    lateinit var coreEngine: CoreEngine

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

        // 异步加载模型文件和初始化引擎
        val appScope = CoroutineScope(Dispatchers.IO)
        appScope.launch {
            try { System.loadLibrary("onnxruntime") } catch (_: UnsatisfiedLinkError) {}
            try { if (!Python.isStarted()) Python.start(AndroidPlatform(this@MainApplication)) } catch (e: Exception) {}
            try { copyAssetsIfNeeded() } catch (_: Exception) {}
            coreEngine = CoreEngine(this@MainApplication)
            val intent = Intent(this@MainApplication, EternalService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                startForegroundService(intent)
            else
                startService(intent)
        }
    }

    private fun copyAssetsIfNeeded() {
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists() || modelDir.list()?.isEmpty() == true) {
            modelDir.mkdirs()
            copyAssets("model", modelDir)
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
