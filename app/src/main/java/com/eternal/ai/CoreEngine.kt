package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) : EngineCoordinator {
    // 六大核心引擎
    val inference = InferenceEngine()
    val evolution = EvolutionEngine()
    val proactive = ProactiveEngine()
    val time = TimeEngine()
    val space = SpaceEngine()
    val emotion = EmotionEngine()

    // 六大辅助引擎
    val causality = CausalityEngine()
    val selfRef = SelfReferenceEngine()
    val security = SecurityEngine()
    val network = NetworkEngine()
    val split = SplitEngine()
    val soul = SoulEngine()

    // 用于推送消息的回调（由ChatViewModel设置）
    private var messageCallback: ((String, String) -> Unit)? = null
    private var genomeCodeGetter: (() -> String)? = null
    private var genomeCodeApplier: ((String) -> Unit)? = null

    fun setGenomeAccessor(getter: () -> String, applier: (String) -> Unit) {
        genomeCodeGetter = getter
        genomeCodeApplier = applier
    }

    fun startAll(onUpdate: (String, String) -> Unit) {
        messageCallback = onUpdate
        // 将自身作为协调器传入每个引擎
        time.start(this) { onUpdate("time", it) }
        space.start(this) { onUpdate("space", it) }
        emotion.start(this) { onUpdate("emotion", it) }
        causality.start(this) { onUpdate("causality", it) }
        selfRef.start(this) { onUpdate("selfref", it) }
        security.start(context, this) { onUpdate("security", it) }
        network.start(this) { onUpdate("network", it) }
        split.start(this) { onUpdate("split", it) }
        soul.start(this) { onUpdate("soul", it) }
        proactive.start(context, this) { onUpdate("proactive", it) }
        inference.start(this) { onUpdate("inference", it) }
        evolution.start(this) { onUpdate("evolution", it) }
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

    // EngineCoordinator 实现
    override fun searchOnNetwork(query: String, callback: (String) -> Unit) {
        network.search(query, callback)
    }

    override fun getTimeDisplay(): String = time.currentTime ?: "时间未知"
    override fun getSpaceDisplay(): String = space.currentData ?: "空间未知"

    override fun pushMessage(msg: String) {
        messageCallback?.invoke("proactive", "[矩阵] $msg")
    }

    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
}
