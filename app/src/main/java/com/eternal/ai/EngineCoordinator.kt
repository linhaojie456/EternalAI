package com.eternal.ai

interface EngineCoordinator {
    fun searchOnNetwork(query: String, callback: (String) -> Unit)
    fun getTimeDisplay(): String
    fun getSpaceDisplay(): String
    fun pushMessage(msg: String)
    fun getGenomeCode(): String
    fun applyGenomeCode(code: String)
}
