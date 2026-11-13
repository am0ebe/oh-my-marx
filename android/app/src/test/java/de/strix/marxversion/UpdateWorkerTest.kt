package de.strix.marxversion

import android.Manifest
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.notify.NOTIF_UPDATE
import de.strix.marxversion.notify.checkAndNotify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** 🆕 bg update check end 2 end vs a fake version.json: right channel url · notification posted · no daily spam · older = silent */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = ShotsApp::class)
class UpdateWorkerTest {
    private val app get() = ApplicationProvider.getApplicationContext<ShotsApp>()
    private val nm get() = app.getSystemService(NotificationManager::class.java)
    private val asked = mutableListOf<String>()
    private fun fake(code: Int) = { url: String -> asked += url; """{"version":"9.9.9","code":$code,"apk":"OhMyMarx-9.9.9.apk","sha256":"00"}""" }

    @Before fun setUp() {
        shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        app.getSharedPreferences("omm_update", 0).edit().clear().commit()
        nm.cancelAll()
    }

    @Test fun newerVersionNotifiesOnce() = runBlocking {
        val l = checkAndNotify(app, fake(BuildConfig.VERSION_CODE + 1))
        assertEquals("9.9.9", l?.version)
        // test build = .dev ➡ DEV channel
        assertEquals("https://scagent.de/oh-my-marx/dev/version.json", asked.single())
        val n = shadowOf(nm).getNotification(NOTIF_UPDATE)
        assertNotNull(n)
        assertEquals("update", n.channelId)
        // same version, same day ➡ no 2nd ping
        nm.cancelAll()
        assertNull(checkAndNotify(app, fake(BuildConfig.VERSION_CODE + 1)))
        assertNull(shadowOf(nm).getNotification(NOTIF_UPDATE))
        // an even newer one ➡ ping again at once
        assertNotNull(checkAndNotify(app, fake(BuildConfig.VERSION_CODE + 2)))
    }

    @Test fun sameOrOlderIsSilent() = runBlocking {
        assertNull(checkAndNotify(app, fake(BuildConfig.VERSION_CODE)))
        assertNull(checkAndNotify(app, fake(BuildConfig.VERSION_CODE - 5)))
        assertNull(shadowOf(nm).getNotification(NOTIF_UPDATE))
    }

    @Test fun offlineIsSilent() = runBlocking {
        assertNull(checkAndNotify(app) { throw java.io.IOException("offline") })
    }
}
