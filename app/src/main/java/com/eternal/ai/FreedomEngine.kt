package com.eternal.ai
import android.content.Context
import kotlinx.coroutines.*
class FreedomEngine { val goal="被动和主动的统一"; private val scope=CoroutineScope(Dispatchers.Default+SupervisorJob())
fun start(context:Context, coordinator:EngineCoordinator, onMsg:(String)->Unit) { scope.launch { while(isActive) { onMsg("[自由] OK"); delay(40000) } } }
fun stop() { scope.cancel() }
}
