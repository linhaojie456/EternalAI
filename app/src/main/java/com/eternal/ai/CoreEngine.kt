package com.eternal.ai
import android.app.Application
class CoreEngine(private val app: Application) {
    val time = TimeEngine()
    val space = SpaceEngine()
    val causality = CausalityEngine()
    val proactive = ProactiveEngine()
    fun startAll(onUpdate: (String, String) -> Unit) {
        time.start { onUpdate("time", it) }
        space.start { onUpdate("space", it) }
        causality.start { onUpdate("causality", it) }
        proactive.start(app) { onUpdate("proactive", it) }
    }
    fun stopAll() { time.stop(); space.stop(); causality.stop(); proactive.stop() }
}
