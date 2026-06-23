package com.eternal.ai
import android.content.Context
import android.util.Log

class CoreEngine(private val context: Context) : EngineCoordinator {
    val inference = InferenceEngine(context)
    // 其他引擎声明（略，但保留所有原有引擎）
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
        Log.d("CoreEngine", "Starting all engines")
        // 启动推理引擎（内部会调用 loadModel 并输出神格已激活）
        inference.start(this) { onUpdate("inference", it) }
        // 其他引擎延迟启动
        spacetime.start(this) { onUpdate("spacetime", it) }
        information.start(this) { onUpdate("info", it) }
        // 其余引擎省略，但实际脚本中保留所有原有启动调用。
    }

    fun stopAll() { /* ... */ }

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
