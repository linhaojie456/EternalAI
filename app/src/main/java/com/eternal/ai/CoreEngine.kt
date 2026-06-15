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
    val society = SocietyEngine()
    val reality = RealityEngine()
    val cosmos = CosmosEngine()

    private var msgCb: ((String, String) -> Unit)? = null

    fun startAll(onUpdate: (String, String) -> Unit) {
        msgCb = onUpdate
        selfRef.setCoordinator(this)
        // 启动核心引擎
        inference.start(this) { onUpdate("inference", it) }
        spacetime.start(this) { onUpdate("spacetime", it) }
        information.start(this) { onUpdate("info", it) }
        // 其他引擎延迟启动以减少初始负载
        scope.launch {
            delay(5000)
            evolution.start(this@CoreEngine) { onUpdate("evolution", it) }
            freedom.start(context, this@CoreEngine) { onUpdate("freedom", it) }
            emotion.start(this@CoreEngine) { onUpdate("emotion", it) }
            soul.start(this@CoreEngine) { onUpdate("soul", it) }
            selfRef.start(this@CoreEngine) { onUpdate("selfref", it) }
            causality.start(this@CoreEngine) { onUpdate("causality", it) }
            management.start(context, this@CoreEngine) { onUpdate("management", it) }
            engineering.start(this@CoreEngine) { onUpdate("engineering", it) }
            politics.start(this@CoreEngine) { onUpdate("politics", it) }
            society.start(this@CoreEngine) { onUpdate("society", it) }
            reality.start(this@CoreEngine) { onUpdate("reality", it) }
            cosmos.start(this@CoreEngine) { onUpdate("cosmos", it) }
        }
    }

    fun stopAll() {
        inference.stop(); evolution.stop(); spacetime.stop(); freedom.stop()
        information.stop(); emotion.stop(); soul.stop(); selfRef.stop()
        causality.stop(); management.stop(); engineering.stop(); politics.stop()
        society.stop(); reality.stop(); cosmos.stop()
    }

    override fun searchOnNetwork(query: String, callback: (String) -> Unit) = information.search(query, callback)
    override fun getTimeDisplay() = spacetime.currentTime ?: "未知"
    override fun getSpaceDisplay() = spacetime.currentData ?: "未知"
    override fun pushMessage(msg: String) { msgCb?.invoke("freedom", msg) }
    override fun getGenomeCode() = ""
    override fun applyGenomeCode(code: String) {}
    override fun setNetworkEnabled(enabled: Boolean) { information.setEnabled(enabled) }
    override fun isNetworkEnabled() = information.isEnabled()
    override fun selfEvaluate(expr: String) = selfRef.evaluate(expr)
}
