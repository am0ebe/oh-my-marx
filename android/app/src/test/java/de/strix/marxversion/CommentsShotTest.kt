package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.ui.CommentsPanel
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Radius
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * 💬 comments sheet shot (L2 · design_system.md § 7) · th real sheet needs Matrix (token + network) ➡ ShotsTest can't open it
 * ➡ renders th stateless `CommentsPanel` w fake comments, framed like th sheet (paperHi · r24 top · handle) over a dimmed page
 * run: ./gradlew testDebugUnitTest --tests '*CommentsShotTest*' -Pshots --rerun ➡ app/build/shots/comments.png · comments_empty.png
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class CommentsShotTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    private val now = System.currentTimeMillis()
    private val list = listOf(
        Matrix.Comment("1", "@clara", "Clara 1918", "So the commodity is the cell of the whole thing? Like biology.", now - 3 * 3_600_000, 49),
        Matrix.Comment("2", "@me", "Rosa 1871", "¶ 1,1 — “immense accumulation” — my flat is exactly this.", now - 40 * 60_000, 49),
        Matrix.Comment("3", "@ernst", "Ernst 1968", "Read it twice. The second time it clicked.", now - 5 * 60_000, 49),
    )

    @Test fun comments() {
        var i by mutableIntStateOf(0)
        val frames = listOf("comments", "comments_empty")
        rule.setContent {
            MarxTheme(Lang.EN, null) {
                Box(Modifier.fillMaxSize().background(Ds.page)) {
                    Box(Modifier.fillMaxSize().background(Ds.ink.copy(alpha = .32f)))   // scrim over th page
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = Radius.xl, topEnd = Radius.xl)).background(Ds.paperHi)) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(32.dp, 4.dp).clip(CircleShape).background(Ds.outline))
                        }
                        CommentsPanel(page = 49, list = if (i == 0) list else emptyList(), me = "@me", writer = "Rosa 1871",
                            draft = "", onDraft = {}, canSend = false, busy = false, failed = false,
                            onSend = {}, onDelete = {}, onClose = {})
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
        frames.forEachIndexed { n, name ->
            i = n
            rule.waitForIdle()
            val v = rule.activity.window.decorView
            val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
            dir.mkdirs()
            File(dir, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
