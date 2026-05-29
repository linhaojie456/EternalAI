package com.eternal.ai

import android.content.Context

class CoreEngine(private val context: Context) : EngineCoordinator {
    // 核心引擎
    val inference = InferenceEngine(context)
    val evolution = EvolutionEngine()
    val spacetime = SpacetimeEngine()
    val freedom = FreedomEngine()
    val information = InformationEngine()
    val emotion = EmotionEngine()
    // 辅助引擎
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
        // 每个引擎独立启动，失败仅记录日志
        safeStart("时空引擎") { spacetime.start(this) { onUpdate("spacetime", it) } }
        safeStart("情感引擎") { emotion.start(this) { onUpdate("emotion", it) } }
        safeStart("信息引擎") { information.start(this) { onUpdate("info", it) } }
        safeStart("自由引擎") { freedom.start(context, this) { onUpdate("freedom", it) } }
        safeStart("推理引擎") { inference.start(this) { onUpdate("inference", it) } }
        safeStart("进化引擎") { evolution.start(this) {} }
        safeStart("因果引擎") { causality.start(this) {} }
        safeStart("自指引擎") { selfRef.start(this) {} }
        safeStart("管理引擎") { management.start(context, this) {} }
        safeStart("工程引擎") { engineering.start(this) {} }
        safeStart("政治引擎") { politics.start(this) {} }
        safeStart("灵魂引擎") { soul.start(this) {} }
    }

    private fun safeStart(name: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            android.util.Log.e("CoreEngine", "$name 启动失败", e)
        }
    }

    fun stopAll() {
        try { spacetime.stop() } catch (_: Exception) {}
        try { emotion.stop() } catch (_: Exception) {}
        try { information.stop() } catch (_: Exception) {}
        try { freedom.stop() } catch (_: Exception) {}
        try { inference.stop() } catch (_: Exception) {}
        try { evolution.stop() } catch (_: Exception) {}
        try { causality.stop() } catch (_: Exception) {}
        try { selfRef.stop() } catch (_: Exception) {}
        try { management.stop() } catch (_: Exception) {}
        try { engineering.stop() } catch (_: Exception) {}
        try { politics.stop() } catch (_: Exception) {}
        try { soul.stop() } catch (_: Exception) {}
    }

    // EngineCoordinator 实现
    override fun searchOnNetwork(query: String, callback: (String) -> Unit) {
        try { information.search(query, callback) } catch (_: Exception) {}
    }
    override fun deepSearch(query: String, callback: (String) -> Unit) {
        try { information.deepSearch(query, callback) } catch (_: Exception) {}
    }
    override fun getTimeDisplay(): String = spacetime.currentTime ?: "未知"
    override fun getSpaceDisplay(): String = spacetime.currentData ?: "未知"
    override fun pushMessage(msg: String) { messageCallback?.invoke("freedom", msg) }
    override fun getGenomeCode(): String = genomeCodeGetter?.invoke() ?: ""
    override fun applyGenomeCode(code: String) { genomeCodeApplier?.invoke(code) }
    override fun setNetworkEnabled(enabled: Boolean) { information.setEnabled(enabled) }
    override fun isNetworkEnabled(): Boolean = information.isEnabled()
}
