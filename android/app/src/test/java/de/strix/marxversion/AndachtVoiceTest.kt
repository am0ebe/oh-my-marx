package de.strix.marxversion

import android.os.Looper
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

/** 🔊 D-087: Today's 🕯️ Andacht hs its own 🔊 (id `a:<plan>:<day>`) · no pack (tst = offline) ➡ phone voice, tap = no crash */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class AndachtVoiceTest {
    @get:Rule val rule = createEmptyComposeRule()

    @Test fun andachtHasListen() {
        val app = ApplicationProvider.getApplicationContext<MarxApp>()
        runBlocking { app.prefs.startPlan("30tage", LocalDate.now().toString(), 2) }
        ActivityScenario.launch(MainActivity::class.java).use {
            repeat(10) { shadowOf(Looper.getMainLooper()).idle(); Thread.sleep(50) }
            val btn = { rule.onAllNodesWithContentDescription("Listen to devotional").onFirst() }
            rule.waitUntil(30_000) { rule.onAllNodesWithContentDescription("Listen to devotional").fetchSemanticsNodes().isNotEmpty() }
            btn().performScrollTo().performClick()
            rule.waitForIdle()
        }
    }
}
