package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) : EngineCoordinator {
    val inference = InferenceEngine(context)
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
    private var networkStatusCallback: ((Boolean) -> Unit)? = null

    fun setGenomeAccessor(getter: () -> String, applier: (String) -> Unit) {
        genomeCodeGetter = getter
        genomeCodeApplier = applier
    }

    fun setNetworkStatusCallback(callback: (Boolean) -> Unit) {
        networkStatusCallback = callback
    }

    fun startAll(onUpdate: (String, String) -> Unit) {
        messageCallback = onUpdate
        // 启动各个引擎
        time.start(this) {}
        space.start(this) {}
        emotion.start(this) { onUpdate("emotion", it) }
        causality.start(this) {}
        selfRef.start(this) {}
        security.start(context, this) {}
        network.start(this) { onUpdate("network", it) }
        split.start(this) {}
        soul.start(this) {}
        proactive.start(context, this) { onUpdate("proactive", it) }
        inference.start(this) { onUpdate("inference", it) }
        evolution.start(this) {}

        // 周期性检测网络状态（已在 NetworkEngine 中实现）
    }

    fun stopAll() {
        time.stop(); space.stop(); emotion.stop(); causality.stop()
        selfRef.stop(); security.stop(); network.stop(); split.stop()
        soul.stop(); proactive.stop(); inference.stop(); evolution.stop()
    }

    // EngineCoordinator 实现
    override fun searchOnNetwork(query: String, callback: (String) -> Unit) {
        network.search(query, callback)
    }

    override fun deepSearch(query: String, callback: (String) -> Unit) {
        network.deepSearch(query, callback)
    }

    override fun getTimeDisplay(): String = time.currentTime ?: "未知"
    override fun getSpaceDisplay(): String = space.currentData ?: "未知"
    override fun pushMessage(msg: String) { messageCallback?.invoke("proactive", msg) }
    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
    override fun setNetworkEnabled(enabled: Boolean) { network.setEnabled(enabled) }
    override fun isNetworkEnabled(): Boolean = network.isEnabled()
}
