package de.strix.marxversion

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import appkit.selfupdate.NOTIF_UPDATE
import appkit.selfupdate.Outcome
import appkit.selfupdate.Update
import appkit.selfupdate.checkAndNotify
import de.strix.marxversion.data.Settings
import de.strix.marxversion.notify.UpdateWorker
import de.strix.marxversion.notify.ommUpdate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 🆕 bg update check end 2 end vs a fake version.json (D-021 + D-023): right channel url · per installer:
 * browser/adb ➡ notification (no daily spam) · we installed ourselves on 12+ + ⚙️ auto ON ➡ silent · auto OFF (dflt) ➡ ask ·
 * Obtainium ➡ notification at once (grace dropped @1114), tap = obtainium://refresh · store ➡ nothing · older/offline = silent
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = ShotsApp::class)
class UpdateWorkerTest {
    private val app get() = ApplicationProvider.getApplicationContext<ShotsApp>()
    private val nm get() = app.getSystemService(NotificationManager::class.java)
    private val me get() = app.packageName
    private val asked = mutableListOf<String>()
    private val newer = BuildConfig.VERSION_CODE + 1
    private fun fake(code: Int) = { url: String -> asked += url; """{"version":"9.9.9","code":$code,"apk":"OhMyMarx-9.9.9.apk","sha256":"00"}""" }
    private var silentRuns = 0
    private val noSilent: suspend (Update.Latest) -> Unit = { silentRuns++ }
    private val notif get() = shadowOf(nm).getNotification(NOTIF_UPDATE)
    private fun run(code: Int, installer: String? = null, sdk: Int = 35, now: Long = 0L, auto: Boolean = true) = runBlocking {
        checkAndNotify(app, fake(code), installer, sdk, now, noSilent, auto)
    }

    @Before fun setUp() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        app.getSharedPreferences("omm_update", 0).edit().clear().commit()
        nm.cancelAll()
    }

    @Test fun browserInstallNotifiesOnce() {
        assertEquals(Outcome.NOTIFIED, run(newer, "com.google.android.packageinstaller"))
        // test build = .dev ➡ DEV channel
        assertEquals("https://scagent.de/oh-my-marx/dev/version.json", asked.single())
        assertNotNull(notif)
        assertEquals("update", notif.channelId)
        // same version, same day ➡ no 2nd ping · an even newer one ➡ at once
        nm.cancelAll()
        assertEquals(Outcome.NONE, run(newer, "com.google.android.packageinstaller"))
        assertNull(notif)
        assertEquals(Outcome.NOTIFIED, run(newer + 1, "com.google.android.packageinstaller"))
        assertEquals(0, silentRuns)
    }

    @Test fun weInstalledOurselvesIsSilent() {
        assertEquals(Outcome.SILENT, run(newer, me))
        assertEquals(1, silentRuns)
        assertNull(notif)
        assertEquals("9.9.9", app.getSharedPreferences("omm_update", 0).getString("silent", null))   // ➡ UpdatedReceiver
    }

    @Test fun autoOffAsksEvenIfWeCouldGoSilent() {
        assertEquals(Outcome.NOTIFIED, run(newer, me, auto = false))
        assertEquals(0, silentRuns)
        assertEquals("update", shadowOf(notif.contentIntent).savedIntent.getStringExtra(MainActivity.EXTRA_POP))   // tap = UpdateDialog
    }

    @Test fun silentFailsFallsBackToNotification() = runBlocking {
        val o = checkAndNotify(app, fake(newer), me, 35, 0L, { throw java.io.IOException("sha256 mismatch") }, auto = true)
        assertEquals(Outcome.NOTIFIED, o)
        assertNull(app.getSharedPreferences("omm_update", 0).getString("silent", null))
    }

    @Test fun oldAndroidAsks() = assertEquals(Outcome.NOTIFIED, run(newer, me, sdk = 30))

    @Test fun obtainiumNotifiesAtOnce() {
        assertEquals(Outcome.NOTIFIED, run(newer, "dev.imranr.obtainium", now = 0L))   // 🦠 @1114: no 8 h grace any more
        assertEquals("obtainium", shadowOf(notif.contentIntent).savedIntent.data?.scheme)
        assertEquals(0, silentRuns)
    }

    @Test fun storeIsSilent() {
        assertEquals(Outcome.NONE, run(newer, "com.android.vending"))
        assertEquals(0, asked.size)
    }

    @Test fun sameOrOlderIsSilent() {
        assertEquals(Outcome.NONE, run(BuildConfig.VERSION_CODE))
        assertEquals(Outcome.NONE, run(BuildConfig.VERSION_CODE - 5))
        assertNull(notif)
    }

    @Test fun offlineIsSilent() = runBlocking {
        assertEquals(Outcome.NONE, checkAndNotify(app, { throw java.io.IOException("offline") }, null, 35, 0L, noSilent))
    }

    /** D-083: dflt OFF (kit, was ON D-078) · nvr touched ➡ asked (banner/notif ➡ Install) · ⚙️ ON ➡ silent · back OFF ➡ asked (worker reads omm's Prefs, no auto override) */
    @Test fun autoDefaultOffAndOnChoiceStays() = runBlocking {
        assertEquals(false, ommUpdate().autoDefault)
        assertEquals(false, Settings().autoUpdate)                      // fresh Settings = kit dflt
        val prefs = app.prefs
        val again = { app.getSharedPreferences("omm_update", 0).edit().clear().commit(); nm.cancelAll() }
        try {
            prefs.clearAutoUpdate()                                     // key absent = nvr touched ➡ Prefs.kt ➡ kit dflt
            assertEquals(Outcome.NOTIFIED, checkAndNotify(app, fake(newer), me, 35, 0L, noSilent))
            again(); prefs.setAutoUpdate(true)
            assertEquals(Outcome.SILENT, checkAndNotify(app, fake(newer), me, 35, 0L, noSilent))
            again(); prefs.setAutoUpdate(false)
            assertEquals(Outcome.NOTIFIED, checkAndNotify(app, fake(newer), me, 35, 0L, noSilent))
            assertEquals("only th ON check went silent", 1, silentRuns)
        } finally { prefs.clearAutoUpdate() }                            // DataStore = process-wide ➡ back 2 nvr touched
    }

    /** D-083: Android refuses a silent try LATER (session result, GrapheneOS @1123) ➡ "Tap = install" notif · nxt check asks, no 2nd silent try 4 th version */
    @Test fun refusedSilentInstallAsksInstead() {
        app.getSharedPreferences("omm_update", 0).edit().putString("silent", "9.9.9").commit()
        appkit.selfupdate.InstallReceiver().onReceive(app, android.content.Intent()
            .putExtra(Update.EXTRA_BG, true).putExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, android.content.pm.PackageInstaller.STATUS_FAILURE))
        assertNotNull("refused silent ➡ notif", notif)
        assertNull("no 'installed ✔️' aftr a swap th nvr came", app.getSharedPreferences("omm_update", 0).getString("silent", null))
        nm.cancelAll()
        assertEquals(Outcome.NOTIFIED, run(newer, me))                  // auto ON + we r installer, but 9.9.9 failed ➡ ask
        assertEquals(0, silentRuns)
        Update.installError.value = null
    }

    /** a FOREGROUND (banner ➡ Install) failure = th dialog shows it, no extra notif */
    @Test fun refusedForegroundInstallPostsNothing() {
        appkit.selfupdate.InstallReceiver().onReceive(app, android.content.Intent()
            .putExtra(Update.EXTRA_BG, false).putExtra(android.content.pm.PackageInstaller.EXTRA_STATUS, android.content.pm.PackageInstaller.STATUS_FAILURE))
        assertNull(notif)
        assertNotNull(Update.installError.value)
        assertEquals(Outcome.SILENT, run(newer, me))                    // fg failure doesn't block a later silent try
        Update.installError.value = null
    }

    /** 🧩 appkit (D-079): omm's channel URLs + th pre-kit storage names (renamed = lost nag state + a 2nd periodic job) */
    @Test fun ommConfigKeepsUrlsAndStorageNames() {
        val c = ommUpdate()
        assertEquals(listOf("omm_update", "marx_update", "omm.apk", UpdateWorker::class.java.name),
            listOf(c.prefs, c.work, c.sessionName, c.worker.name))
        assertEquals("de.strix.marxversion.notify.UpdateWorker", c.worker.name)   // WorkManager's stored job class
        assertEquals("https://scagent.de/oh-my-marx/", Update.base(false))
        assertEquals("https://scagent.de/oh-my-marx/dev/", Update.base(true))
    }
}
