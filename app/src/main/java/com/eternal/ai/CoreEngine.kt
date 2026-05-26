package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) {
    val time = TimeEngine()
    val space = SpaceEngine()
    val causality = CausalityEngine()
    val selfRef = SelfReferenceEngine()
    val security = SecurityEngine()
    val network = NetworkEngine()
    val split = SplitEngine()
    val proactive = ProactiveEngine()

    fun startAll(onUpdate: (String, String) -> Unit) {
        time.start { onUpdate("time", it) }
        space.start { onUpdate("space", it) }
        causality.start { onUpdate("causality", it) }
        selfRef.start { onUpdate("selfref", it) }
        security.start { onUpdate("security", it) }
        network.start { onUpdate("network", it) }
        split.start { onUpdate("split", it) }
        proactive.start(context) { onUpdate("proactive", it) }
    }

    fun stopAll() {
        time.stop()
        space.stop()
        causality.stop()
        selfRef.stop()
        security.stop()
        network.stop()
        split.stop()
        proactive.stop()
    }
}
