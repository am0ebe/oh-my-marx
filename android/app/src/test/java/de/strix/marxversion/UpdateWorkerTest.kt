package de.strix.marxversion

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.notify.NOTIF_UPDATE
import de.strix.marxversion.notify.Outcome
import de.strix.marxversion.notify.checkAndNotify
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
 * browser/adb ➡ notification (no daily spam) · we installed ourselves on 12+ ➡ silent, no notification ·
 * Obtainium ➡ quiet 4 GRACE_H, then a notification whose tap = obtainium://refresh · store ➡ nothing · older/offline = silent
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
    private val noSilent: suspend (de.strix.marxversion.data.Update.Latest) -> Unit = { silentRuns++ }
    private val notif get() = shadowOf(nm).getNotification(NOTIF_UPDATE)
    private fun run(code: Int, installer: String? = null, sdk: Int = 35, now: Long = 0L) = runBlocking {
        checkAndNotify(app, fake(code), installer, sdk, now, noSilent)
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

    @Test fun silentFailsFallsBackToNotification() = runBlocking {
        val o = checkAndNotify(app, fake(newer), me, 35, 0L) { throw java.io.IOException("sha256 mismatch") }
        assertEquals(Outcome.NOTIFIED, o)
        assertNull(app.getSharedPreferences("omm_update", 0).getString("silent", null))
    }

    @Test fun oldAndroidAsks() = assertEquals(Outcome.NOTIFIED, run(newer, me, sdk = 30))

    @Test fun obtainiumGetsAHeadStart() {
        val h = 3_600_000L
        assertEquals(Outcome.NONE, run(newer, "dev.imranr.obtainium", now = 0L))      // 1st seen
        assertEquals(Outcome.NONE, run(newer, "dev.imranr.obtainium", now = 7 * h))   // Obtainium's 6 h check had its chance? not yet
        assertNull(notif)
        assertEquals(Outcome.NOTIFIED, run(newer, "dev.imranr.obtainium", now = 9 * h))
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
}
