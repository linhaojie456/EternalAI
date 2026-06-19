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
        // 立即在后台线程加载推理模型
        Thread {
            Log.d("CoreEngine", "Loading inference model on background thread")
            inference.loadModel()
            onUpdate("inference", if (inference.isModelLoaded) "[推理] 神格已激活" else "[推理] 神格激活失败: ${inference.lastError ?: "未知"}")
        }.start()
        // 其他引擎延迟启动
        spacetime.start(this) { onUpdate("spacetime", it) }
        information.start(this) { onUpdate("info", it) }
        // 其余引擎...
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
