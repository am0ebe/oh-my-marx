package de.strix.marxversion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.text.TextLayoutResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 🤏 todo ⏳ updater step 6 (fr qa_1121 r2 ck 4 · 🐛 @1120 pinch lost i 📜, #200): any chapter ➡ 📜 Scroll ➡ pinch =
 * txt grows ➡ back 2 📖 Pages = same size · real app (MainActivity), Robolectric · was a 🦠🧪 fon step (#352 ③)
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class PinchScaleTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val app get() = rule.activity.application as MarxApp

    private fun has(t: String, exact: Boolean = false) = rule.onAllNodesWithText(t, substring = !exact).fetchSemanticsNodes().isNotEmpty()
    private fun tap(t: String, exact: Boolean = false) {
        rule.waitUntil(30_000) { has(t, exact) }
        rule.onAllNodesWithText(t, substring = !exact).onFirst().performClick()
    }
    private fun settle() { rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }
    private val first = "wealth of those societies"

    /** font size th passage is laid out with (TextLayoutResult = wh th screen draws) */
    private fun fontSp(): Float {
        rule.waitUntil(10_000) { has(first) }
        val node = rule.onAllNodesWithText(first, substring = true, useUnmergedTree = true).fetchSemanticsNodes()
            .first { SemanticsActions.GetTextLayoutResult in it.config }   // th Text itself, not th passage box
        val out = mutableListOf<TextLayoutResult>()
        node.config[SemanticsActions.GetTextLayoutResult].action!!.invoke(out)
        return out.first().layoutInput.style.fontSize.value
    }

    /** DataStore = process-wide ➡ a grown scale + 📜 mode leaked into ReaderGestureTest (4 red, #352) ➡ back 2 📖 at size 1 */
    @After fun resetReader() { runBlocking { app.prefs.run { setReadScale(1f); setReadScroll(false) } } }

    @Test fun pinchInScrollGrowsTextAndPagesKeepIt() {
        tap("Read", true); tap("p. 49"); settle()
        tap("Scroll"); settle()
        val before = fontSp()
        // 2 fingers spread apart vertically on th 📜 list (zoom ≈ 2 ➡ clamp 2f · Initial pass = no scroll while pinching)
        rule.onAllNodes(hasScrollToIndexAction()).onFirst().performTouchInput {
            pinch(Offset(centerX, centerY - 60f), Offset(centerX, centerY - 260f),
                Offset(centerX, centerY + 60f), Offset(centerX, centerY + 260f), 600)
        }
        settle()
        val grown = fontSp()
        assertTrue("📜 pinch out = bigger txt ($before ➡ $grown sp)", grown > before * 1.2f)
        val saved = runBlocking { app.prefs.settings.first().readScale }
        assertTrue("scale saved ($saved)", saved > 1.2f)
        tap("Pages"); settle()
        assertEquals("📖 Pages = same size as i 📜", grown, fontSp(), 0.01f)
        tap("Scroll"); settle()
        assertEquals("📜 again = still th same", grown, fontSp(), 0.01f)
    }
}
