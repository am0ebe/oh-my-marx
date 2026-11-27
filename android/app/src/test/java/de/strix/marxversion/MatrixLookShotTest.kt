package de.strix.marxversion

import android.graphics.Bitmap
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.ui.CommentsPanel
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.Ds
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import java.io.File

/**
 * 💬 #366: th Matrix live sesh became a dc sesh ➡ 🦠 judges D-077 (grey id) + D-088 (report dialog) on PNGs, no 2nd account
 * real ids (`@x-ab12:scagent.de`) + 1 fr elsewhere (`@x:matrix.org`) · DE (🦠's app lang) · dialog = its own window ➡ ShadowDialog
 * run: ./gradlew testDebugUnitTest --tests '*MatrixLookShotTest*' -Pshots --rerun ➡ app/build/shots/mx_ids_de.png · mx_report_de.png · mx_reported_de.png
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class MatrixLookShotTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    private val now = System.currentTimeMillis()
    private val me = "@rosa-1f2e:scagent.de"
    private val list = listOf(
        Matrix.Comment("1", "@clara-ab12:scagent.de", "Clara", "Die Ware als Zelle des Ganzen? Wie in der Biologie.", now - 3 * 3_600_000, 49),
        Matrix.Comment("2", "@ernst-7c09:scagent.de", "strix", "Ich bin der echte strix, glaubt mir.", now - 50 * 60_000, 49),
        Matrix.Comment("3", me, "Rosa", "¶ 1,1 — „ungeheure Warensammlung“ — meine Wohnung.", now - 40 * 60_000, 49),
        Matrix.Comment("4", "@jan:matrix.org", "Jan", "Komme über Element von matrix.org.", now - 5 * 60_000, 49),
    )

    private fun save(v: View, name: String) {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        dir.mkdirs()
        File(dir, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun idsReportReported() {
        rule.setContent {
            MarxTheme(Lang.DE, null) {
                Box(Modifier.fillMaxSize().background(Ds.paperHi)) {
                    CommentsPanel(page = 49, list = list, me = me, writer = "Rosa", draft = "", onDraft = {}, canSend = false,
                        busy = false, failed = false, onSend = {}, onDelete = {}, onClose = {}, onReport = { _, done -> done(true) })
                }
            }
        }
        rule.waitForIdle()
        save(rule.activity.window.decorView, "mx_ids_de")
        rule.onNodeWithText("Ich bin der echte strix", substring = true).performTouchInput { longClick() }
        rule.waitForIdle()
        save(ShadowDialog.getLatestDialog().window!!.decorView, "mx_report_de")
        rule.onNodeWithText("Melden").performClick()
        rule.waitForIdle()
        save(rule.activity.window.decorView, "mx_reported_de")
    }
}
