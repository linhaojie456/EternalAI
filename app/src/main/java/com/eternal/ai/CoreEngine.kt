package com.eternal.ai
import android.content.Context
class CoreEngine(private val context: Context) : EngineCoordinator {
    // 原有12引擎
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
    // 新增三大引擎
    val society = SocietyEngine()
    val reality = RealityEngine()
    val cosmos = CosmosEngine()

    private var messageCallback: ((String, String) -> Unit)? = null
    private var genomeCodeGetter: (() -> String)? = null
    private var genomeCodeApplier: ((String) -> Unit)? = null

    fun setGenomeAccessor(getter: () -> String, applier: (String) -> Unit) { genomeCodeGetter = getter; genomeCodeApplier = applier }

    fun startAll(onUpdate: (String, String) -> Unit) {
        messageCallback = onUpdate
        selfRef.setCoordinator(this)
        spacetime.start(this) {}
        emotion.start(this) {}
        information.start(this) { onUpdate("info", it) }
        freedom.start(context, this) { onUpdate("freedom", it) }
        inference.start(this) { onUpdate("inference", it) }
        evolution.start(this) {}
        causality.start(this) {}
        selfRef.start(this) { onUpdate("selfref", it) }
        management.start(context, this) {}
        engineering.start(this) {}
        politics.start(this) {}
        soul.start(this) {}
        society.start(this) { onUpdate("society", it) }
        reality.start(this) { onUpdate("reality", it) }
        cosmos.start(this) { onUpdate("cosmos", it) }
    }

    fun stopAll() {
        spacetime.stop(); emotion.stop(); information.stop(); freedom.stop(); inference.stop()
        evolution.stop(); causality.stop(); selfRef.stop(); management.stop(); engineering.stop()
        politics.stop(); soul.stop(); society.stop(); reality.stop(); cosmos.stop()
    }

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
