package com.eternal.ai
import kotlinx.coroutines.*
import java.net.*
class InformationEngine { val goal="频率和数字的统一"; private var enabled=true; private val scope=CoroutineScope(Dispatchers.IO+SupervisorJob())
fun start(coordinator:EngineCoordinator, onInfo:(String)->Unit) { scope.launch { while(isActive) { onInfo(if(enabled)"[信息] 在线" else "[信息] 离线"); delay(60000) } } }
fun setEnabled(e:Boolean) { enabled=e }
fun isEnabled()=enabled
fun search(query:String, callback:(String)->Unit) { callback("结果") }
fun stop() { scope.cancel() }
}
