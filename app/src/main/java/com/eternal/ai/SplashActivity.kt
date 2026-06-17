package com.eternal.ai
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 等待引擎初始化完成（最长等待30秒）
        Thread {
            (application as MainApplication).waitForEngineReady()
            runOnUiThread {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }.start()
    }
}
