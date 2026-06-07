package com.eternal.ai
interface EngineCoordinator {
    fun searchOnNetwork(query: String, callback: (String) -> Unit); fun deepSearch(query: String, callback: (String) -> Unit) { searchOnNetwork(query, callback) }
    fun getTimeDisplay(): String; fun getSpaceDisplay(): String; fun pushMessage(msg: String)
    fun getGenomeCode(): String; fun applyGenomeCode(code: String)
    fun setNetworkEnabled(enabled: Boolean); fun isNetworkEnabled(): Boolean
    fun selfEvaluate(expr: String): Any?
}
