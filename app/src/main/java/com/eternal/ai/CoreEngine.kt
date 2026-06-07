package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) : EngineCoordinator {
    val inference = InferenceEngine(context)
    val evolution = EvolutionEngine()
    val spacetime = SpacetimeEngine()
    val freedom = FreedomEngine()
    val information = InformationEngine()
    val emotion = EmotionEngine()
    val soul = SoulEngine()
    val selfRef = SelfReferenceEngine()
    val causality = CausalityEngine()
    val management = ManagementEngine()
    val engineering = EngineeringEngine()
    val politics = PoliticsEngine()

    private var messageCallback: ((String, String) -> Unit)? = null
    private var genomeCodeGetter: (() -> String)? = null
    private var genomeCodeApplier: ((String) -> Unit)? = null

    fun setGenomeAccessor(getter: () -> String, applier: (String) -> Unit) {
        genomeCodeGetter = getter
        genomeCodeApplier = applier
    }

    fun startAll(onUpdate: (String, String) -> Unit) {
        messageCallback = onUpdate
        // 先设置自指引擎的协调器
        selfRef.setCoordinator(this)
        safeStart("时空引擎") { spacetime.start(this) {} }
        safeStart("情感引擎") { emotion.start(this) {} }
        safeStart("信息引擎") { information.start(this) { onUpdate("info", it) } }
        safeStart("自由引擎") { freedom.start(context, this) { onUpdate("freedom", it) } }
        safeStart("推理引擎") { inference.start(this) { onUpdate("inference", it) } }
        safeStart("进化引擎") { evolution.start(this) {} }
        safeStart("因果引擎") { causality.start(this) {} }
        safeStart("自指引擎") { selfRef.start(this) { onUpdate("selfref", it) } }
        safeStart("管理引擎") { management.start(context, this) {} }
        safeStart("工程引擎") { engineering.start(this) {} }
        safeStart("政治引擎") { politics.start(this) {} }
        safeStart("灵魂引擎") { soul.start(this) {} }
    }

    fun stopAll() {
        safeStop { spacetime.stop() }
        safeStop { emotion.stop() }
        safeStop { information.stop() }
        safeStop { freedom.stop() }
        safeStop { inference.stop() }
        safeStop { evolution.stop() }
        safeStop { causality.stop() }
        safeStop { selfRef.stop() }
        safeStop { management.stop() }
        safeStop { engineering.stop() }
        safeStop { politics.stop() }
        safeStop { soul.stop() }
    }

    private fun safeStart(name: String, block: () -> Unit) {
        try { block() } catch (e: Exception) { e.printStackTrace() }
    }
    private fun safeStop(block: () -> Unit) { try { block() } catch (_: Exception) {} }

    override fun searchOnNetwork(query: String, callback: (String) -> Unit) = information.search(query, callback)
    override fun deepSearch(query: String, callback: (String) -> Unit) = information.deepSearch(query, callback)
    override fun getTimeDisplay(): String = spacetime.currentTime ?: "未知"
    override fun getSpaceDisplay(): String = spacetime.currentData ?: "未知"
    override fun pushMessage(msg: String) { messageCallback?.invoke("freedom", msg) }
    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
    override fun setNetworkEnabled(enabled: Boolean) { information.setEnabled(enabled) }
    override fun isNetworkEnabled(): Boolean = information.isEnabled()
    override fun selfEvaluate(expr: String): Any? = selfRef.evaluate(expr)
}
