package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.ui.Blue
import de.strix.marxversion.ui.CardAction
import de.strix.marxversion.ui.MarxTheme
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
 * 🎨 button directions rendered 4 real (design_system.md § 5 · arch D-030 💬) ➡ 🦠 picks by eye
 * test-only mock: nothing here ships · same content i evy variant: now · A · A + press edge · B · C
 * run: ./gradlew testDebugUnitTest --tests '*DsGalleryTest*' -Pshots --rerun ➡ app/build/shots/ds_N_name.png
 */
private val paper = Color(0xFFF6EEDC)
private val paperHi = Color(0xFFFFFBF2)
private val paperDeep = Color(0xFFEDE3CC)
private val ink = Color(0xFF3B2A1A)
private val inkMuted = Color(0xFF6B5A48)
private val outline = Color(0xFF8F8271)
private val red = Color(0xFFB3261E)
private val redDeep = Color(0xFF8C1D18)
private val redTint = Color(0xFFEFDAC9)
private val blue = Color(0xFF1F2F4F)
private val blueTint = Color(0xFFE0DBCE)
private val gold = Color(0xFFE8B931)

private enum class St { REST, PRESSED, OFF }

private interface Kit {
    val name: String
    val sub: String
    @Composable fun Primary(label: String, large: Boolean, st: St)
    @Composable fun Second(label: String)
    @Composable fun Link(label: String, danger: Boolean)
    @Composable fun Act(icon: Int, label: String)
    /** on = null ➡ Suggest chip (tap = go) · else Choice chip */
    @Composable fun Chip(label: String, on: Boolean?)
}

@Composable
private fun Lbl(t: String, c: Color, size: Int = 14, w: FontWeight = FontWeight.Medium, serif: Boolean = false, under: Boolean = false) =
    Text(t, color = c, fontSize = size.sp, fontWeight = w, maxLines = 1,
        fontFamily = if (serif) FontFamily.Serif else FontFamily.Default,
        textDecoration = if (under) TextDecoration.Underline else null)

@Composable
private fun Check(c: Color) = Canvas(Modifier.padding(end = 6.dp).size(12.dp)) {
    val s = 2.dp.toPx()
    drawLine(c, Offset(size.width * .08f, size.height * .55f), Offset(size.width * .4f, size.height * .85f), s, StrokeCap.Round)
    drawLine(c, Offset(size.width * .4f, size.height * .85f), Offset(size.width * .95f, size.height * .18f), s, StrokeCap.Round)
}

/** filled btn · edge > 0 = press edge under it (sinks on press) */
@Composable
private fun Fill(label: String, large: Boolean, st: St, shape: Shape, edge: Dp) {
    val h = if (large) 52.dp else 40.dp
    val bg = when (st) { St.OFF -> ink.copy(alpha = .12f); St.PRESSED -> if (edge > 0.dp) red else lerp(red, ink, .22f); else -> red }
    val fg = if (st == St.OFF) ink.copy(alpha = .38f) else Color.White
    val sink = if (edge > 0.dp && st == St.PRESSED) edge - 1.dp else 0.dp
    Box(Modifier.height(h + edge).then(if (large) Modifier.fillMaxWidth() else Modifier)) {
        if (edge > 0.dp && st != St.OFF) Box(Modifier.matchParentSize().padding(top = edge).background(redDeep, shape))
        Box(Modifier.offset(y = sink).height(h).then(if (large) Modifier.fillMaxWidth() else Modifier)
            .background(bg, shape).padding(horizontal = 22.dp), contentAlignment = Alignment.Center) {
            Lbl(label, fg, if (large) 16 else 14, FontWeight.Bold)
        }
    }
}

private object KitNow : Kit {
    override val name = "0 · now"
    override val sub = "M3 Button · OutlinedButton · TextButton · AssistChip · FilterChip · CardAction"
    @Composable override fun Primary(label: String, large: Boolean, st: St) =
        Button(onClick = {}, enabled = st != St.OFF, modifier = if (large) Modifier.fillMaxWidth() else Modifier) {
            Text(label, fontSize = if (large) 17.sp else 14.sp, fontWeight = FontWeight.Bold)
        }
    @Composable override fun Second(label: String) = OutlinedButton(onClick = {}) { Text(label) }
    @Composable override fun Link(label: String, danger: Boolean) {
        if (danger) OutlinedButton(onClick = {}) { Text(label) } else TextButton(onClick = {}) { Text(label) }
    }
    @Composable override fun Act(icon: Int, label: String) = CardAction(icon, label, Blue) {}
    @Composable override fun Chip(label: String, on: Boolean?) {
        if (on == null) AssistChip(onClick = {}, label = { Text(label) })
        else FilterChip(selected = on, onClick = {}, label = { Text(label) })
    }
}

private class KitA(val edge: Boolean) : Kit {
    override val name = if (edge) "A+ · pill tonal + press edge (pick)" else "A · pill tonal"
    override val sub = if (edge) "like A · only the 1 big button gets a 4dp edge and sinks on press"
        else "red pill = the action · blue tint pill = 2nd · flat, calm"
    @Composable override fun Primary(label: String, large: Boolean, st: St) =
        Fill(label, large, st, CircleShape, if (edge && large) 4.dp else 0.dp)
    @Composable override fun Second(label: String) =
        Box(Modifier.height(40.dp).background(blueTint, CircleShape).padding(horizontal = 18.dp), contentAlignment = Alignment.Center) { Lbl(label, blue) }
    @Composable override fun Link(label: String, danger: Boolean) =
        Box(Modifier.height(40.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) { Lbl(label, if (danger) redDeep else blue) }
    @Composable override fun Act(icon: Int, label: String) =
        Column(Modifier.width(66.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp).background(blueTint, CircleShape), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), label, Modifier.size(20.dp), tint = blue)
            }
            Spacer(Modifier.height(4.dp)); Lbl(label, ink, 12)
        }
    @Composable override fun Chip(label: String, on: Boolean?) {
        val m = Modifier.height(32.dp).let {
            when (on) {
                true -> it.background(redTint, CircleShape)
                false -> it.background(paperDeep, CircleShape)
                null -> it.background(paperHi, CircleShape).border(1.dp, outline, CircleShape)
            }
        }
        Row(m.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            if (on == true) Check(red)
            Lbl(label, if (on == true) red else ink, 12)
        }
    }
}

private object KitB : Kit {
    private val r = RoundedCornerShape(8.dp)
    override val name = "B · letterpress"
    override val sub = "every button is a block of type with an edge · sinks on press + haptic"
    @Composable override fun Primary(label: String, large: Boolean, st: St) = Fill(label, large, st, r, 4.dp)
    @Composable private fun Block(h: Dp, edge: Dp, sunk: Boolean, content: @Composable () -> Unit) =
        Box(Modifier.height(h + edge)) {
            if (!sunk) Box(Modifier.matchParentSize().padding(top = edge).background(lerp(paper, ink, .45f), r))
            Box(Modifier.offset(y = if (sunk) edge - 1.dp else 0.dp).height(h).background(if (sunk) redTint else paperHi, r)
                .border(1.dp, if (sunk) red else outline, r).padding(horizontal = 14.dp), contentAlignment = Alignment.Center) { content() }
        }
    @Composable override fun Second(label: String) = Block(40.dp, 3.dp, false) { Lbl(label, ink, 14, FontWeight.Bold) }
    @Composable override fun Link(label: String, danger: Boolean) =
        Box(Modifier.height(40.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) { Lbl(label, if (danger) redDeep else blue) }
    @Composable override fun Act(icon: Int, label: String) =
        Column(Modifier.width(66.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Block(44.dp, 3.dp, false) { Icon(painterResource(icon), label, Modifier.size(20.dp), tint = ink) }
            Spacer(Modifier.height(4.dp)); Lbl(label, ink, 12)
        }
    @Composable override fun Chip(label: String, on: Boolean?) =
        Block(30.dp, 2.dp, on == true) { Lbl(label, if (on == true) red else ink, 12, FontWeight.Bold) }
}

private object KitC : Kit {
    override val name = "C · rubric"
    override val sub = "no fills · serif ink + red rule = the action · red underlined words = 2nd"
    @Composable override fun Primary(label: String, large: Boolean, st: St) {
        val c = when (st) { St.OFF -> ink.copy(alpha = .38f); St.PRESSED -> red; else -> ink }
        Box(Modifier.height(if (large) 52.dp else 40.dp).then(if (large) Modifier.fillMaxWidth() else Modifier), contentAlignment = Alignment.Center) {
            Column(Modifier.width(IntrinsicSize.Max), horizontalAlignment = Alignment.CenterHorizontally) {
                Lbl(label, c, if (large) 19 else 15, FontWeight.Bold, serif = true)
                Spacer(Modifier.height(3.dp))
                Box(Modifier.fillMaxWidth().height(2.dp).background(if (st == St.OFF) ink.copy(alpha = .12f) else red))
            }
        }
    }
    @Composable override fun Second(label: String) =
        Box(Modifier.height(40.dp).padding(end = 12.dp), contentAlignment = Alignment.Center) { Lbl(label, red, under = true) }
    @Composable override fun Link(label: String, danger: Boolean) =
        Box(Modifier.height(40.dp).padding(horizontal = 8.dp), contentAlignment = Alignment.Center) { Lbl(label, if (danger) redDeep else inkMuted) }
    @Composable override fun Act(icon: Int, label: String) =
        Column(Modifier.width(66.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { Icon(painterResource(icon), label, Modifier.size(22.dp), tint = ink) }
            Lbl(label, inkMuted, 12)
        }
    @Composable override fun Chip(label: String, on: Boolean?) =
        Box(Modifier.height(32.dp).padding(end = 10.dp), contentAlignment = Alignment.Center) {
            when (on) {
                true -> Row(verticalAlignment = Alignment.CenterVertically) { Check(red); Lbl(label, red, 13, FontWeight.Bold) }
                false -> Lbl(label, ink, 13)
                null -> Lbl(label, red, 13, under = true)
            }
        }
}

private val KITS = listOf(KitNow, KitA(false), KitA(true), KitB, KitC)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Sheet(k: Kit) = Column(Modifier.fillMaxSize().background(paper).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(k.name, color = ink, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Text(k.sub, color = inkMuted, fontSize = 14.sp, lineHeight = 20.sp)
    Column {
        Text("Capital 22,56 · MEW p. 621", color = inkMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text("Accumulate, accumulate! That is Moses and the prophets!", color = ink, fontSize = 17.sp, lineHeight = 26.sp, fontFamily = FontFamily.Serif)
    }
    k.Primary("Amen — read today", true, St.REST)
    k.Primary("Amen — pressed", true, St.PRESSED)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        k.Primary("Send", false, St.REST); k.Primary("Send", false, St.PRESSED); k.Primary("Send", false, St.OFF)
        Text("rest · pressed · off", color = inkMuted, fontSize = 11.sp)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        k.Second("Copy link"); k.Second("In book"); Spacer(Modifier.weight(1f)); k.Link("Later", false)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        k.Act(R.drawable.ic_volume, "Listen"); k.Act(R.drawable.ic_copy, "Copy"); k.Act(R.drawable.ic_book, "In book")
        k.Act(R.drawable.ic_share, "Share card"); k.Act(R.drawable.ic_send, "Share app")
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        k.Chip("Preacher", true); k.Chip("Comrade", false); k.Chip("Rent", null); k.Chip("Boss", null); k.Chip("Overtime", null)
    }
    Column(Modifier.fillMaxWidth().background(paperHi, RoundedCornerShape(24.dp)).border(BorderStroke(1.dp, gold.copy(alpha = .55f)), RoundedCornerShape(24.dp)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("New badge", color = ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("7 days in a row. Marx kept your page.", color = ink, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        k.Primary("Keep going", true, St.REST)
        k.Link("Later", false)
    }
    k.Link("Leave circle", true)
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h960dp-xxhdpi", application = android.app.Application::class)
class DsGalleryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    @Test fun buttons() {
        var i by mutableIntStateOf(0)
        rule.setContent { MarxTheme(Lang.EN, null) { Sheet(KITS[i]) } }
        listOf("now", "A", "A_edge", "B", "C").forEachIndexed { n, name ->
            i = n
            rule.waitForIdle()
            val v = rule.activity.window.decorView
            val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
            rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
            dir.mkdirs()
            File(dir, "ds_${n}_$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
