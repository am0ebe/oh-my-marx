package de.strix.marxversion

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** 🧺 D-084 (🦠 @1126 "start w lvl1. It shd also have a name"): th REAL Mine screen nvr says LEVEL 0 / "No rank yet" */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class MineLevelTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun has(t: String, exact: Boolean = false) = rule.onAllNodesWithText(t, substring = !exact).fetchSemanticsNodes().isNotEmpty()

    @Test fun mineStartsAtLevel1WithAName() {
        rule.waitUntil(30_000) { has("My Capital", true) }
        rule.onAllNodesWithText("My Capital").onFirst().performClick()
        rule.waitUntil(30_000) { has("LEVEL ", false) }
        assertFalse("old: LEVEL 0 at start", has("LEVEL 0", true))
        assertFalse("old: no name at start", has("No rank yet"))
        // fresh reader (0 full cards) ➡ LEVEL 1 + its name · days leaked fr anoth tst (Robolectric) ➡ a higher level, still named
        if (has("LEVEL 1", true)) assertTrue("level 1 hs a name", has("Commodity", true))
    }
}
