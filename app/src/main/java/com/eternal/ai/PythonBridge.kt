package com.eternal.ai
import com.chaquo.python.Python

object PythonBridge {
    val instance = PythonBridge
    private val py = Python.getInstance()
    private val evo = py.getModule("evo_core")
    fun call(func: String, vararg args: Any?): Any? = evo.callAttr(func, *args)
}
