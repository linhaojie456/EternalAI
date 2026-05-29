package com.eternal.ai

import com.chaquo.python.Python

object PythonBridge {
    private var py: Python? = null
    private var evoModule: com.chaquo.python.PyObject? = null

    @Synchronized
    private fun init() {
        if (py == null) {
            try {
                py = Python.getInstance()
                evoModule = py?.getModule("evo_core")
            } catch (e: Exception) {
                // 初始化失败，保持 null
                e.printStackTrace()
            }
        }
    }

    fun call(func: String, vararg args: Any?): Any? {
        init()
        return try {
            evoModule?.callAttr(func, *args)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
