package de.strix.marxversion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.click
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
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

    @Test fun tapTurnsDoubleTapOpensMenu() {
        tap("Read", true); tap("p. 49"); settle()
        // 👆👆 ➡ menu (Note · Copy …), page stays
        passage().performTouchInput { doubleClick() }; settle()
        assertTrue("double-tap opens th passage menu", has("Note", true))
        assertTrue(has(first))
        // 👆 right half ➡ nxt page (page 1 leaves th composition)
        passage().performTouchInput { click(centerRight - Offset(24f, 0f)) }; settle()
        assertFalse("tap right = next page", has(first))
        // 👆 hold on th left half ➡ back 2 page 1
        rule.onRoot().performTouchInput { longClick(Offset(60f, centerY)) }; settle()
        assertTrue("hold left = back", has(first))
    }
}
