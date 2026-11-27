package de.strix.marxversion

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.coroutines.CoroutineContext

/** 🧪 effect ctx 4 evy compose rule (#390) · dflt = Unconfined ➡ an effect resuming aftr `withContext(Default)` runs on th worker
 *  (prod: AndroidUiDispatcher ➡ alw main) · Standard = queued, th tst env runs it on main · EffectThreadTest guards it
 *  (swap in `EmptyCoroutineContext` ➡ it goes red) */
val effectRule: CoroutineContext get() = StandardTestDispatcher()
