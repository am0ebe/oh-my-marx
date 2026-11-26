package de.strix.marxversion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 👆 D-057 (🦠 @1120 "swap"): i th 📖 book a passage tap o hold = turn (left half back · right half fwd) ·
 * double-tap = th passage menu · real app (MainActivity), Robolectric
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class ReaderGestureTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun has(t: String, exact: Boolean = false) = rule.onAllNodesWithText(t, substring = !exact).fetchSemanticsNodes().isNotEmpty()
    private fun tap(t: String, exact: Boolean = false) {
        rule.waitUntil(30_000) { has(t, exact) }
        rule.onAllNodesWithText(t, substring = !exact).onFirst().performClick()
    }
    private fun settle() { rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }
    private val first = "wealth of those societies"
    private fun passage() = rule.onAllNodesWithText(first, substring = true).onFirst()

    /** 🐛 DEV 1009 (🦠 @1120): 1st tap on a fresh page = th menu, not a turn · no double-tap b4 (th old tst hid it) */
    @Test fun firstTapTurnsNoMenu() {
        tap("Read", true); tap("p. 49"); settle()
        // 🐛 old: ¶ = combinedClickable ➡ OnClick action + ripple over th whole ¶ on evy tap ("selected" look on th fon)
        assertFalse("📖 passage is not clickable (book detector owns taps)",
            passage().fetchSemanticsNode().config.contains(androidx.compose.ui.semantics.SemanticsActions.OnClick))
        passage().performTouchInput { click(centerRight - Offset(24f, 0f)) }; settle()
        assertFalse("1 tap opens no menu", has("Note", true))
        assertFalse("1 tap right = next page", has(first))
    }

    /** 🐛 qa_1120 #200: 📖 ⬄ 📜 stays on th same page · 📜 can scroll back up (was: batch feed, start = top) · Continue resumes */
    @Test fun modeToggleKeepsPage_scrollGoesBack_continueResumes() {
        tap("Read", true); tap("p. 49"); settle()
        passage().performTouchInput { click(centerRight - Offset(24f, 0f)) }; settle()
        assertFalse("on page 2", has(first))
        tap("Scroll"); settle()
        assertFalse("📜 opens at page 2, not th chapter top", has(first))
        // 👆 slow drags (no fling) back up, 1 by 1, til page 1 shows (old feed: nothing above th start ➡ nvr)
        val list = { rule.onAllNodes(androidx.compose.ui.test.hasScrollToIndexAction()).onFirst() }
        val drag = { down: Boolean -> list().performTouchInput {
            val a = Offset(centerX, top + 80f); val b = Offset(centerX, bottom - 80f)
            if (down) swipe(a, b, 900) else swipe(b, a, 900) }; settle() }
        var back = 0
        while (!has(first) && back < 6) { drag(true); back++ }
        assertTrue("📜 scrolls back up 2 page 1", has(first))
        repeat(back + 2) { drag(false) }
        tap("Pages"); settle()
        assertFalse("📖 lands where 📜 was (not page 1)", has(first))
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }; settle()
        assertTrue("Continue btn on th Read list", has("Continue"))
    }

    @Test fun tapTurnsDoubleTapOpensMenu() {
        tap("Read", true); tap("p. 49"); settle()
        // 👆👆 ➡ menu (Note · Copy …), page stays
        passage().performTouchInput { doubleClick() }; settle()
        assertTrue("double-tap opens th passage menu", has("Note", true))
        assertTrue(has(first))
        // 👆👆 again = menu closes · CI run 1145: a tap w th menu still open didn't turn (local did) ➡ turn only on a bare page
        passage().performTouchInput { doubleClick() }; settle()
        assertFalse("2nd double-tap closes th menu", has("Note", true))
        // 👆 right half ➡ nxt page (page 1 leaves th composition)
        passage().performTouchInput { click(centerRight - Offset(24f, 0f)) }; settle()
        assertFalse("tap right = next page", has(first))
        // 👆 tap left ➡ back 2 page 1
        rule.onRoot().performTouchInput { click(Offset(60f, centerY)) }; settle()
        assertTrue("tap left = back", has(first))
    }

    /** D-065 (🦠 @1120 via M8 #218): hold a passage = its menu, i 📖 AND 📜 · hold nvr turns */
    @Test fun holdOpensMenuInBothModes() {
        tap("Read", true); tap("p. 49"); settle()
        passage().performTouchInput { longClick(center) }; settle()
        assertTrue("📖 hold = menu", has("Note", true))
        assertTrue("📖 hold doesn't turn", has(first))
        passage().performTouchInput { longClick(center) }; settle()   // hold again = menu closes
        tap("Scroll"); settle()
        passage().performTouchInput { longClick(center) }; settle()
        assertTrue("📜 hold = menu", has("Note", true))
        tap("Pages"); settle()
    }
}
