package com.eternal.ai
import android.content.Context
class CoreEngine(private val context: Context) : EngineCoordinator {
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
    private var messageCallback: ((String, String) -> Unit)? = null
    private var genomeCodeGetter: (() -> String)? = null
    private var genomeCodeApplier: ((String) -> Unit)? = null
    fun setGenomeAccessor(getter: () -> String, applier: (String) -> Unit) {
        genomeCodeGetter = getter
        genomeCodeApplier = applier
    }
    fun startAll(onUpdate: (String, String) -> Unit) {
        messageCallback = onUpdate
        time.start(this) {}
        space.start(this) {}
        emotion.start(this) { onUpdate("emotion", it) }
        causality.start(this) {}
        selfRef.start(this) {}
        security.start(context, this) {}
        network.start(this) {}
        split.start(this) {}
        soul.start(this) {}
        proactive.start(context, this) { onUpdate("proactive", it) }
        inference.start(this) {}
        evolution.start(this) {}
    }
    fun stopAll() {
        time.stop(); space.stop(); emotion.stop(); causality.stop()
        selfRef.stop(); security.stop(); network.stop(); split.stop()
        soul.stop(); proactive.stop(); inference.stop(); evolution.stop()
    }
    override fun searchOnNetwork(query: String, callback: (String) -> Unit) { network.search(query, callback) }
    override fun getTimeDisplay(): String = time.currentTime ?: "未知"
    override fun getSpaceDisplay(): String = space.currentData ?: "未知"
    override fun pushMessage(msg: String) { messageCallback?.invoke("proactive", msg) }
    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
}
