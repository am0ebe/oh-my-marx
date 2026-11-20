package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.CloseStar
import de.strix.marxversion.ui.ds.CloseVariant
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.DsDark
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * ★ D-055 close btn sheet (design § 14.5 · lane C6 · 🦠 r1 "gv many versions, see it i diff ctx")
 * 8 variants × 4 ctx (dialog on paperHi · bottom sheet · hero gradient · 🌙 dark) + 1 zoom ×2 ➡ build/shots/close_sheet.png
 * run: ./gradlew testDebugUnitTest --tests '*CloseSheetTest*' -Pshots · 🦠 picks ➡ CloseStar dflt (ui/ds/Buttons.kt), 1 line
 */
private val HERO = listOf(Color(0xFF1F4E9A), Color(0xFF3A8BD0))

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class CloseSheetTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    /** always runs: evy variant composes, says "Close" 4 TalkBack + fires onClick */
    @Test fun variantsClick() {
        var hits = 0
        rule.setContent { MarxTheme(Lang.EN, null) { Column { CloseVariant.entries.forEach { CloseStar({ hits++ }, variant = it) } } } }
        val nodes = rule.onAllNodesWithContentDescription("Close")
        assertEquals(CloseVariant.entries.size, nodes.fetchSemanticsNodes().size)
        CloseVariant.entries.indices.forEach { nodes[it].performClick() }
        assertEquals(CloseVariant.entries.size, hits)
    }

    @Test fun sheet() {
        assumeTrue("needs -Pshots", System.getProperty("shots") == "1")
        rule.setContent { MarxTheme(Lang.EN, null) { Sheet() } }
        rule.waitForIdle()
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        dir.mkdirs()
        File(dir, "close_sheet.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

@Composable
private fun Sheet() = Column(Modifier.fillMaxSize().background(Ds.paper).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("★ close btn · D-055 · 🦠 pick 1", fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Ds.ink)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf("", "dialog", "sheet", "hero", "dark", "×2").forEachIndexed { i, h ->
            Text(h, fontSize = 10.sp, color = Ds.inkMuted, modifier = Modifier.width(if (i == 0) 70.dp else if (i == 5) 72.dp else 58.dp))
        }
    }
    CloseVariant.entries.forEach { v ->
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(70.dp)) {
                Text(v.name + if (v == CloseVariant.V4) " 🤖" else "", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ds.ink)
                Text(v.look, fontSize = 9.sp, lineHeight = 11.sp, color = Ds.inkMuted)
            }
            Cell(Ds.paperHi, null, v, Ds.red, title = true)
            Cell(Ds.paper, null, v, Ds.red, handle = true)
            Cell(Color.Transparent, Brush.verticalGradient(HERO), v, Ds.white)
            Cell(DsDark.surface, null, v, DsDark.red)
            Box(Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)).background(Ds.paperHi), contentAlignment = Alignment.Center) {
                CloseStar({}, tint = Ds.red, size = 72.dp, variant = v)
            }
        }
    }
}

/** 1 ctx: a 58 × 72 corner o a surface w th ★ at its top-end (where it sits i th app) */
@Composable
private fun Cell(bg: Color, brush: Brush?, v: CloseVariant, tint: Color, title: Boolean = false, handle: Boolean = false, w: Dp = 58.dp) =
    Box(Modifier.width(w).height(72.dp).clip(RoundedCornerShape(10.dp))
        .then(if (brush != null) Modifier.background(brush) else Modifier.background(bg))
        .border(1.dp, Ds.ink.copy(alpha = .08f), RoundedCornerShape(10.dp))) {
        if (handle) Box(Modifier.align(Alignment.TopCenter).padding(top = 4.dp).width(20.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Ds.ink.copy(alpha = .25f)))
        if (title) Text("Title", fontFamily = FontFamily.Serif, fontSize = 10.sp, color = Ds.ink, modifier = Modifier.padding(start = 4.dp, top = 14.dp))
        CloseStar({}, Modifier.align(Alignment.TopEnd).padding(top = 10.dp, end = 4.dp), tint = tint, variant = v)
        Box(Modifier.align(Alignment.BottomStart).padding(4.dp).fillMaxWidth(.7f).height(3.dp).background(
            (if (bg == DsDark.surface || brush != null) Color.White else Ds.ink).copy(alpha = .18f)))
    }
