package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.logic.StampCard
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.ActionItem
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.OmmType
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.StampRow
import de.strix.marxversion.ui.ds.Tag
import de.strix.marxversion.ui.ds.Tonal
import de.strix.marxversion.ui.ds.starPath
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
 * 💬 open picks rendered as phone mockups (🦠 @1114: "alw better 4 me 2 see") — test-only, nothing ships
 * D-031 reader chrome a · b · c · D-036 Today above th fold a · b · c ➡ build/shots/d031_*.png · d036_*.png
 * real ds parts (btns · actions · stamps · type), hand-built screens · data = fake (streak 3 · stamps 3/10)
 */
private val DAY = listOf(Color(0xFF1F4E9A), Color(0xFF3A8BD0), Color(0xFF7DB8E6))
private val CARD = listOf(Color(0xFF3A1F5C), Color(0xFFB3261E), Color(0xFFF2A65A))
private const val QUOTE = "“Capital is dead labour, which, vampire-like, only lives by sucking living labour.”"

@Composable
private fun StatusBar(bg: Color, fg: Color) = Row(Modifier.fillMaxWidth().height(24.dp).background(bg).padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically) {
    Text("12:00", color = fg, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f)); Text("▾ ▮", color = fg, fontSize = 11.sp)
}

@Composable
private fun TopBar() = Row(Modifier.fillMaxWidth().height(56.dp).background(Ds.red).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(36.dp).clip(CircleShape).background(Ds.dev))
    Text("Oh My Marx ☭", color = Ds.white, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp).weight(1f))
    Text("EN ▾", color = Ds.white, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(end = 16.dp))
    Icon(painterResource(R.drawable.ic_settings), null, tint = Ds.white)
}

@Composable
private fun NavBar(sel: Int) = Row(Modifier.fillMaxWidth().height(72.dp).background(Ds.paperDeep).padding(top = 8.dp)) {
    listOf(R.drawable.ic_nav_today to "Today", R.drawable.ic_nav_plans to "Plans", R.drawable.ic_book to "Read",
        R.drawable.ic_nav_group to "Circle", null to "My Capital").forEachIndexed { i, (ic, l) ->
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.width(56.dp).height(28.dp).clip(CircleShape).background(if (i == sel) Ds.blueTint else Color.Transparent), contentAlignment = Alignment.Center) {
                if (ic != null) Icon(painterResource(ic), null, Modifier.size(22.dp), tint = Ds.ink) else Text("☭", color = Ds.ink, fontWeight = FontWeight.Bold)
            }
            Text(l, style = MaterialTheme.typography.labelMedium, color = if (i == sel) Ds.ink else Ds.inkMuted)
        }
    }
}

@Composable
private fun Star(d: Dp, n: Int) = Box(Modifier.size(d), contentAlignment = Alignment.Center) {
    Canvas(Modifier.fillMaxSize()) {
        val r = size.minDimension / 2f; val p = starPath(center + Offset(0f, r * .095f), r)
        drawPath(p, Ds.red); drawPath(p, Ds.gold, style = Stroke((d.value / 30f).dp.toPx(), join = StrokeJoin.Round))
    }
    Text("$n", color = Ds.white, fontWeight = FontWeight.Bold, fontSize = (d.value * .3f).sp, modifier = Modifier.padding(top = (d.value * .07f).dp))
}

@Composable
private fun Week(dot: Dp) = Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, l ->
        val read = i < 3; val today = i == 3
        Box(Modifier.size(dot).clip(CircleShape).background(when { read -> Ds.gold; today -> Ds.white.copy(alpha = .3f); else -> Ds.white.copy(alpha = .12f) })
            .then(if (today) Modifier.border(2.dp, Ds.white, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
            Text(if (read) "☭" else l, color = if (read) Ds.red else Ds.white, fontSize = (dot.value * .42f).sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeroBox(content: @Composable ColumnScope.() -> Unit) = Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    .clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(DAY)).background(Color.Black.copy(alpha = .15f)).padding(16.dp), content = content)

/** today's hero as it is now (≈ 230dp) */
@Composable
private fun HeroNow() = HeroBox {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("3 days of reading capital accumulated", color = Ds.white.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
            Text("Keep it rolling!", color = Ds.white, fontSize = 24.sp, fontWeight = FontWeight.Bold, style = OmmType.bookTitle)
            Text("Short break ☕ — Marx reads faster than a meeting.", color = Ds.white.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
        }
        Star(92.dp, 3)
    }
    Spacer(Modifier.height(12.dp)); Week(34.dp); Spacer(Modifier.height(14.dp))
    StampRow(StampCard(1, 3, 3), doneToday = false)
}

/** D-036 a: compact 120dp — star 48 + headline · 7 dots 28 + Tag ☭ 3/10 (stamp card ➡ My Capital) */
@Composable
private fun HeroCompact() = HeroBox {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Star(48.dp, 3)
        Column(Modifier.padding(start = 12.dp)) {
            Text("Keep it rolling!", color = Ds.white, style = MaterialTheme.typography.titleMedium)
            Text("3 days of reading capital", color = Ds.white.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium)
        }
    }
    Spacer(Modifier.height(10.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Week(28.dp); Spacer(Modifier.weight(1f))
        Box(Modifier.clip(CircleShape).background(Ds.gold).padding(horizontal = 8.dp, vertical = 2.dp)) { Text("☭ 3/10", color = Ds.ink, style = MaterialTheme.typography.labelMedium) }
    }
}

/** D-036 c aft th 1st scroll: hero shrinks 2 a 56dp strip */
@Composable
private fun HeroStrip() = Row(Modifier.fillMaxWidth().height(56.dp).background(Brush.horizontalGradient(DAY)).padding(horizontal = 16.dp),
    verticalAlignment = Alignment.CenterVertically) {
    Star(36.dp, 3); Text("Keep it rolling!", color = Ds.white, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 10.dp).weight(1f))
    Week(18.dp)
}

@Composable
private fun QuoteCard(h: Dp, size: Int) = Column(Modifier.fillMaxWidth().padding(horizontal = 32.dp).height(h).shadow(8.dp, RoundedCornerShape(16.dp))
    .clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(CARD)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center) {
    Text("QUOTE OF THE DAY", style = OmmType.bookMeta, color = Ds.gold)
    Spacer(Modifier.height(12.dp))
    Text(QUOTE, style = OmmType.quote.copy(fontSize = size.sp, lineHeight = (size * 1.35).sp), color = Ds.white, textAlign = TextAlign.Center)
    Spacer(Modifier.height(10.dp))
    Text("— Karl Marx, Capital 10,1", style = MaterialTheme.typography.labelSmall, color = Ds.white.copy(alpha = .85f))
}

@Composable
private fun Actions() = Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
    ActionItem(R.drawable.ic_volume, "Listen", {}); ActionItem(R.drawable.ic_copy, "Copy", {}); ActionItem(R.drawable.ic_book, "In book", {})
    ActionItem(R.drawable.ic_share, "Share card", {}); ActionItem(R.drawable.ic_send, "Share app", {})
}

@Composable
private fun PlanRow() = Tonal("📖  Pick a reading plan  ➜", {}, Modifier.fillMaxWidth().padding(horizontal = 16.dp))

@Composable
private fun Fold() = Box(Modifier.fillMaxWidth().height(0.dp))

@Composable
private fun Today(which: Char) = Column(Modifier.fillMaxSize().background(Ds.paper)) {
    StatusBar(Ds.redDeep, Ds.white); TopBar()
    Column(Modifier.weight(1f).padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (which) {
            'a' -> { HeroCompact(); QuoteCard(360.dp, 22); Actions(); PlanRow() }
            'b' -> { HeroNow(); QuoteCard(250.dp, 17); Actions(); PlanRow() }
            else -> { HeroStrip(); QuoteCard(360.dp, 22); Actions(); PlanRow() }
        }
    }
    NavBar(0)
}

// ── D-031 reader chrome ─────────────────────────────────────────────────────────────────────────────

private val VERSES = listOf(
    "1,1" to "The wealth of those societies in which the capitalist mode of production prevails, presents itself as “an immense accumulation of commodities,” its unit being a single commodity.",
    "1,2" to "A commodity is, in the first place, an object outside us, a thing that by its properties satisfies human wants of some sort or another.",
    "1,3" to "Every useful thing, as iron, paper, &c., may be looked at from the two points of view of quality and quantity.",
    "1,4" to "The utility of a thing makes it a use value. But this utility is not a thing of air.",
)

@Composable
private fun Page(modifier: Modifier) = Column(modifier.fillMaxWidth().background(Ds.page).padding(start = 24.dp, end = 20.dp, top = 12.dp)) {
    Row { Text("1. COMMODITIES", style = OmmType.bookMeta, color = Ds.inkMuted, modifier = Modifier.weight(1f)); Text("MEW P. 49", style = OmmType.bookMeta, color = Ds.inkMuted) }
    Spacer(Modifier.height(16.dp))
    Text("1. Commodities", style = OmmType.bookTitle, color = Ds.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(12.dp))
    Text("The Two Factors of a Commodity", style = OmmType.bookHead, color = Ds.red)
    VERSES.forEach { (r, t) ->
        Text("Capital $r", style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted, modifier = Modifier.padding(top = 12.dp))
        Text(t, style = OmmType.bookBody, color = Ds.ink)
    }
}

@Composable
private fun SlimHead() = Row(Modifier.fillMaxWidth().height(48.dp).background(Ds.page).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
    Text("←", color = Ds.ink, fontSize = 22.sp, modifier = Modifier.padding(horizontal = 8.dp))
    Text("1. Commodities", style = MaterialTheme.typography.titleSmall.copy(fontFamily = OmmType.bookHead.fontFamily), color = Ds.ink, modifier = Modifier.weight(1f))
    Box(Modifier.clip(CircleShape).background(Ds.paperDeep).padding(horizontal = 10.dp, vertical = 4.dp)) { Text("💬 3", style = MaterialTheme.typography.labelMedium, color = Ds.ink) }
}

@Composable
private fun Reader(which: Char) = Column(Modifier.fillMaxSize().background(Ds.page)) {
    when (which) {
        'a' -> { StatusBar(Ds.redDeep, Ds.white); TopBar(); Page(Modifier.weight(1f)); NavBar(2) }
        'b' -> { StatusBar(Ds.page, Ds.ink); SlimHead(); Box(Modifier.fillMaxWidth().height(1.dp).background(Ds.line)); Page(Modifier.weight(1f)) }
        else -> {
            Box(Modifier.weight(1f)) {
                Column { Spacer(Modifier.height(24.dp)); Page(Modifier.fillMaxSize()) }
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).clip(CircleShape).background(Ds.ink.copy(alpha = .75f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)) { Text("tap = show bars", color = Ds.paperHi, style = MaterialTheme.typography.labelMedium) }
            }
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class DsChoicesTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    @Test fun picks() {
        var i by mutableIntStateOf(0)
        val frames = listOf("d036_a", "d036_b", "d036_c", "d031_a", "d031_b", "d031_c")
        rule.setContent { MarxTheme(Lang.EN, null) { val f = frames[i]; if (f.startsWith("d036")) Today(f.last()) else Reader(f.last()) } }
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
