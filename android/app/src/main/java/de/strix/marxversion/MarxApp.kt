package de.strix.marxversion

import android.app.Application
import kotlinx.coroutines.launch
import de.strix.marxversion.data.ContentRepo
import de.strix.marxversion.data.MarxDb
import de.strix.marxversion.data.Prefs
import de.strix.marxversion.notify.ensureChannel

/**
 * Application = lives as long as th process · holds th 3 singletons evy screen needs.
 * (= poor man's dependency injection — no Hilt/Koin needed at this size)
 */
open class MarxApp : Application() {  // open: test/ShotsTest's ShotsApp inits WorkManager 1st (Robolectric skips th startup provider)
    val content by lazy { ContentRepo(this) }
    val db by lazy { MarxDb.create(this) }
    val prefs by lazy { Prefs(this) }
    /** fire-and-forget writes fr non-composable spots (shareCard counts 4 th 🎖️ Orden) */
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
        de.strix.marxversion.notify.scheduleKreis(this)  // 🔔 Lesekreis msgs + evening nudge (no-op wo circles)
        de.strix.marxversion.notify.scheduleUpdate(this) // 🆕 ~daily "new version" check (no-op 4 store/Obtainium installs)
        // 🥚 D-066: launcher icon = today's egg · flips at 00:01 (worker) + whenever th last screen leaves (nvr while on screen)
        de.strix.marxversion.data.EggIcon.schedule(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStopped(a: android.app.Activity) {
                if (!a.isChangingConfigurations) scope.launch { de.strix.marxversion.data.EggIcon.sync(this@MarxApp) }
            }
            override fun onActivityCreated(a: android.app.Activity, b: android.os.Bundle?) {}
            override fun onActivityStarted(a: android.app.Activity) {}
            override fun onActivityResumed(a: android.app.Activity) {}
            override fun onActivityPaused(a: android.app.Activity) {}
            override fun onActivitySaveInstanceState(a: android.app.Activity, b: android.os.Bundle) {}
            override fun onActivityDestroyed(a: android.app.Activity) {}
        })
    }
}
