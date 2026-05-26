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
            // 将 assets/model 目录复制到 filesDir/model，以便 Python 访问
            copyAssets("model", File(filesDir, "model"))
        }
    }

    private fun copyAssets(assetPath: String, destDir: File) {
        if (destDir.exists()) return  // 已经复制过
        destDir.mkdirs()
        val assetManager: AssetManager = assets
        val files = assetManager.list(assetPath) ?: return
        for (filename in files) {
            val inStream = assetManager.open("$assetPath/$filename")
            val outFile = File(destDir, filename)
            FileOutputStream(outFile).use { out ->
                inStream.copyTo(out)
            }
            inStream.close()
        }
    }
}
