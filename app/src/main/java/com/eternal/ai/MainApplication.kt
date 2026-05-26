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

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // 复制模型文件
        try {
            val modelDir = File(filesDir, "model")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
                copyAssets("model", modelDir)
            }
        } catch (_: Exception) {}
    }

    private fun copyAssets(assetPath: String, destDir: File) {
        val assetManager: AssetManager = assets
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            try {
                val inStream = assetManager.open("$assetPath/$filename")
                val outFile = File(destDir, filename)
                FileOutputStream(outFile).use { out -> inStream.copyTo(out) }
                inStream.close()
            } catch (_: Exception) {}
        }
    }
}
