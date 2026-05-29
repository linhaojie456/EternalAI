package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) : EngineCoordinator {
    // 六大核心引擎
    val inference = InferenceEngine(context)        // 推理引擎
    val evolution = EvolutionEngine()               // 自进化引擎
    val spacetime = SpacetimeEngine()               // 时空引擎（原时间+空间）
    val freedom = FreedomEngine()                   // 自由引擎（原主动引擎）
    val information = InformationEngine()           // 信息引擎（原网络引擎）
    val emotion = EmotionEngine()                   // 情感引擎

    // 六大辅助引擎
    val soul = SoulEngine()                         // 灵魂引擎
    val selfRef = SelfReferenceEngine()             // 自指引擎
    val causality = CausalityEngine()               // 因果引擎
    val management = ManagementEngine()             // 管理引擎（原安全引擎）
    val engineering = EngineeringEngine()           // 工程引擎（原分裂引擎）
    val politics = PoliticsEngine()                 // 政治引擎（新增）

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
        // 启动所有引擎
        spacetime.start(this) { onUpdate("spacetime", it) }
        emotion.start(this) { onUpdate("emotion", it) }
        information.start(this) { onUpdate("info", it) }
        freedom.start(context, this) { onUpdate("freedom", it) }
        inference.start(this) { onUpdate("inference", it) }
        evolution.start(this) {}
        causality.start(this) {}
        selfRef.start(this) {}
        management.start(context, this) {}
        engineering.start(this) {}
        politics.start(this) {}
        soul.start(this) {}
    }

    fun stopAll() {
        spacetime.stop()
        emotion.stop()
        information.stop()
        freedom.stop()
        inference.stop()
        evolution.stop()
        causality.stop()
        selfRef.stop()
        management.stop()
        engineering.stop()
        politics.stop()
        soul.stop()
    }

    // EngineCoordinator 实现
    override fun searchOnNetwork(query: String, callback: (String) -> Unit) {
        information.search(query, callback)
    }

    override fun deepSearch(query: String, callback: (String) -> Unit) {
        information.deepSearch(query, callback)
    }

    override fun getTimeDisplay(): String = spacetime.currentTime ?: "未知"
    override fun getSpaceDisplay(): String = spacetime.currentData ?: "未知"
    override fun pushMessage(msg: String) { messageCallback?.invoke("freedom", msg) }
    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
    override fun setNetworkEnabled(enabled: Boolean) { information.setEnabled(enabled) }
    override fun isNetworkEnabled(): Boolean = information.isEnabled()
}
