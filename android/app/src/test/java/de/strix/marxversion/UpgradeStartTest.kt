package de.strix.marxversion

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.data.DayLog
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.os.Looper
import java.time.LocalDate

/**
 * 🔥 DEV 1119.1944 startup crash on 🦠's fon (old state: plans started on v1) ➡ app start w UPGRADED prefs, not a fresh install
 * per plan: started 3 days ago on cut v1 · 2 days logged (yesterday + today) ➡ MainActivity must come up
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class UpgradeStartTest {
    private fun startWith(plan: String) {
        val app = ApplicationProvider.getApplicationContext<MarxApp>()
        val t = LocalDate.now()
        runBlocking {
            app.prefs.startPlan(plan, t.minusDays(3).toString(), 1)
            app.db.logs().day(DayLog(t.minusDays(1).toString(), plan, 0))
            app.db.logs().day(DayLog(t.toString(), plan, 1))
        }
        // coroutine crashes go 2 th thread's uncaught handler ➡ Robolectric only logs them ➡ catch + fail (on a fon = app dies)
        val crashes = java.util.Collections.synchronizedList(mutableListOf<Throwable>())
        val main = Looper.getMainLooper().thread   // Robolectric's "SDK 35 Main Thread" hs its own handler (not th default)
        val prev = main.uncaughtExceptionHandler
        val prevDefault = Thread.getDefaultUncaughtExceptionHandler()
        val catcher = Thread.UncaughtExceptionHandler { _, e -> crashes += e }
        main.uncaughtExceptionHandler = catcher; Thread.setDefaultUncaughtExceptionHandler(catcher)
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                repeat(20) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(50) }
            }
        } finally { main.uncaughtExceptionHandler = prev; Thread.setDefaultUncaughtExceptionHandler(prevDefault) }
        assertTrue("startup crashed: ${crashes.firstOrNull()?.let { it.stackTraceToString().take(600) }}", crashes.isEmpty())
    }

    @Test fun bibel() = startWith("bibel")
    @Test fun dreissig() = startWith("30tage")
    @Test fun montag() = startWith("montag")
    @Test fun wertform() = startWith("wertform")
    @Test fun ludditen() = startWith("ludditen")
}
