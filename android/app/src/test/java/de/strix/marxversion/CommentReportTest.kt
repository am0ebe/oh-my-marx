package de.strix.marxversion

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.ui.CommentsPanel
import de.strix.marxversion.ui.MarxTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** 🚩 D-088 (D-073 gate): hold a stranger's comment ➡ confirm ➡ report (ok / failed line) · hold ur own = delete, nvr report · Cancel = nothing sent */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class CommentReportTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>(effectRule)
    private val now = System.currentTimeMillis()
    private val list = listOf(
        Matrix.Comment("e1", "@clara", "Clara", "buy cheap watches", now - 60_000, 49),
        Matrix.Comment("e2", "@me", "Rosa", "my own note", now - 30_000, 49),
    )
    private val reports = mutableListOf<String>()
    private val deletes = mutableListOf<String>()
    private var answer = true

    private fun panel() = rule.setContent {
        MarxTheme(Lang.EN, null) {
            CommentsPanel(page = 49, list = list, me = "@me", writer = "Rosa", draft = "", onDraft = {}, canSend = false, busy = false, failed = false,
                onSend = {}, onDelete = { deletes += it }, onClose = {}, onReport = { id, done -> reports += id; done(answer) })
        }
    }
    private fun hold(t: String) = rule.onNodeWithText(t).performTouchInput { longClick() }

    @Test fun holdStrangerReports() {
        panel()
        hold("buy cheap watches"); rule.onNodeWithText("Report").performClick(); rule.waitForIdle()
        assertEquals(listOf("e1"), reports)
        rule.onNodeWithText("Reported", substring = true).assertExists()
    }

    @Test fun cancelSendsNothing_ownIsDeleteNotReport() {
        panel()
        hold("buy cheap watches"); rule.onNodeWithText("Cancel").performClick(); rule.waitForIdle()
        rule.onNodeWithText("Report this comment?", substring = true).assertDoesNotExist()
        hold("my own note"); rule.waitForIdle()
        assertEquals(emptyList<String>(), reports)
        assertEquals(listOf("e2"), deletes)
    }

    @Test fun failedReportSaysSo() {
        answer = false; panel()
        hold("buy cheap watches"); rule.onNodeWithText("Report").performClick(); rule.waitForIdle()
        rule.onNodeWithText("didn't go through", substring = true).assertExists()
    }
}
