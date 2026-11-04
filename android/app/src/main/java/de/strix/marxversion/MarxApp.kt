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
class MarxApp : Application() {
    val content by lazy { ContentRepo(this) }
    val db by lazy { MarxDb.create(this) }
    val prefs by lazy { Prefs(this) }

    override fun onCreate() {
        super.onCreate()
        ensureChannel(this)
        de.strix.marxversion.notify.scheduleKreis(this)  // 🔔 Lesekreis msgs + evening nudge (no-op wo circles)
    }
}
