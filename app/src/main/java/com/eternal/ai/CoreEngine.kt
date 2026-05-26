package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) {
    val time = TimeEngine()
    val space = SpaceEngine()
    val causality = CausalityEngine()
    val proactive = ProactiveEngine()
    val selfRef = SelfReferenceEngine()

    fun startAll(onUpdate: (String, String) -> Unit) {
        time.start { onUpdate("time", it) }
        space.start { onUpdate("space", it) }
        causality.start { onUpdate("causality", it) }
        selfRef.start { onUpdate("selfref", it) }
        proactive.start(context) { onUpdate("proactive", it) }
    }

    fun stopAll() {
        time.stop()
        space.stop()
        causality.stop()
        selfRef.stop()
        proactive.stop()
    }
}
