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

        try {
            System.loadLibrary("tokenizers")
        } catch (e: UnsatisfiedLinkError) {
            e.printStackTrace()
        }

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        try {
            val modelDir = File(filesDir, "model")
            if (!modelDir.exists()) {
                modelDir.mkdirs()
                copyAssets("model", modelDir)
            }
            val genomeFile = File(filesDir, "genome.py")
            if (!genomeFile.exists()) {
                val inputStream = assets.open("genome.py")
                FileOutputStream(genomeFile).use { out -> inputStream.copyTo(out) }
                inputStream.close()
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
