package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) {
    val inference = InferenceEngine()
    val evolution = EvolutionEngine()
    val proactive = ProactiveEngine()
    val time = TimeEngine()
    val space = SpaceEngine()
    val emotion = EmotionEngine()
    val causality = CausalityEngine()
    val selfRef = SelfReferenceEngine()
    val security = SecurityEngine()
    val network = NetworkEngine()
    val split = SplitEngine()
    val soul = SoulEngine()

    fun startAll(onUpdate: (String, String) -> Unit) {
        time.start { onUpdate("time", it) }
        space.start { onUpdate("space", it) }
        emotion.start { onUpdate("emotion", it) }
        causality.start { onUpdate("causality", it) }
        selfRef.start { onUpdate("selfref", it) }
        security.start(context) { onUpdate("security", it) }
        network.start { onUpdate("network", it) }
        split.start { onUpdate("split", it) }
        soul.start { onUpdate("soul", it) }
        proactive.start(context) { onUpdate("proactive", it) }
        inference.start { onUpdate("inference", it) }
        evolution.start { onUpdate("evolution", it) }
    }

    fun stopAll() {
        time.stop()
        space.stop()
        emotion.stop()
        causality.stop()
        selfRef.stop()
        security.stop()
        network.stop()
        split.stop()
        soul.stop()
        proactive.stop()
        inference.stop()
        evolution.stop()
    }
}
