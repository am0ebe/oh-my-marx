package de.strix.marxversion

import android.os.Looper
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 🐛 #390 CI 1237: compose-ui-test's dflt effect dispatcher = Unconfined ➡ `produceState { withContext(Default) }` (Cards.kt card bitmap)
 *  resumes ON th worker ➡ recomposer frame runs there ➡ "multithreaded access to SnapshotStateObserver" · fix = [effectRule] */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = ShotsApp::class)
class EffectThreadTest {
    @get:Rule val rule = createComposeRule(effectRule)

    @Test fun produceStateResumesOnMain() {
        var resumedOn: Thread? = null
        rule.setContent {
            val v by produceState("…") {
                val s = withContext(Dispatchers.Default) { Thread.sleep(50); "done" }
                resumedOn = Thread.currentThread(); value = s
            }
            Text(v)
        }
        rule.waitUntil(5_000) { resumedOn != null }
        assertEquals(Looper.getMainLooper().thread, resumedOn)
    }
}
