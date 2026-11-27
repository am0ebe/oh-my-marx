package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.MarxVoice
import de.strix.marxversion.ui.ds.NoteCard
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** ✍️ C5 · D-054 b: note = comment card (mine: ruler + hairline + tail · others: paperDeep) ➡ build/shots/note_card.png */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class NoteCardShotTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>(effectRule)

    @Test fun cardEditAndShot() {
        var edits = 0
        rule.setContent { MarxTheme(Lang.EN, null) {
            Column(Modifier.fillMaxSize().background(Ds.page).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MarxVoice("The wealth of those societies in which the capitalist mode of production prevails, presents itself as …")
                NoteCard("You", "Note · 19 Nov", tail = true, posted = "also in the reading room", onEdit = { edits++ }) {
                    Text("Commodity = anything that gets sold?") }
                Text("comments sheet:", color = Ds.inkMuted)
                NoteCard("Rosa", "2 h ago", mine = false) { Text("Not just sold — made to be sold.") }
                NoteCard("You", "1 h ago") { Text("Fair. So my labour power too?") }
            }
        } }
        rule.onNodeWithText("✎").performClick()
        assertEquals(1, edits)
        assumeTrue(System.getProperty("shots") == "1")
        rule.waitForIdle()
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        val dir = File(System.getProperty("shotsDir") ?: "build/shots").apply { mkdirs() }
        File(dir, "note_card.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
