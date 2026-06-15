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
        inference.start(this) { onUpdate("inference", it) }
        spacetime.start(this) { onUpdate("spacetime", it) }
        information.start(this) { onUpdate("info", it) }
        // 其他引擎延迟启动
        Thread {
            Thread.sleep(5000)
            evolution.start(this) { onUpdate("evolution", it) }
            freedom.start(context, this) { onUpdate("freedom", it) }
            emotion.start(this) { onUpdate("emotion", it) }
            soul.start(this) { onUpdate("soul", it) }
            selfRef.start(this) { onUpdate("selfref", it) }
            causality.start(this) { onUpdate("causality", it) }
            management.start(context, this) { onUpdate("management", it) }
            engineering.start(this) { onUpdate("engineering", it) }
            politics.start(this) { onUpdate("politics", it) }
            society.start(this) { onUpdate("society", it) }
            reality.start(this) { onUpdate("reality", it) }
            cosmos.start(this) { onUpdate("cosmos", it) }
        }.start()
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
