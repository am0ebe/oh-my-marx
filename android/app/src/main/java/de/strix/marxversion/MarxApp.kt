package de.strix.marxversion

import android.app.Application
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
    /** 🔊 AI voice packs (D-085) · files/voice/<lang>/ */
    open val voice by lazy { de.strix.marxversion.data.VoicePacks(java.io.File(filesDir, "voice")) }
    /** fire-and-forget writes fr non-composable spots (shareCard counts 4 th 🎖️ Orden) */
    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
        de.strix.marxversion.notify.scheduleKreis(this)  // 🔔 Lesekreis msgs + evening nudge (no-op wo circles)
        appkit.selfupdate.Update.init(de.strix.marxversion.notify.ommUpdate())   // 🆕 appkit updater config (b4 anything checks)
        appkit.selfupdate.scheduleUpdate(this) // 🆕 ~daily "new version" check (no-op 4 store/Obtainium installs)
    }
}
