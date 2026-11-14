package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.logic.StampCard
import de.strix.marxversion.ui.DialogAction
import de.strix.marxversion.ui.MarxDialogPanel
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.AccentPanel
import de.strix.marxversion.ui.ds.ActStyle
import de.strix.marxversion.ui.ds.ActionItem
import de.strix.marxversion.ui.ds.Callout
import de.strix.marxversion.ui.ds.ChoiceChip
import de.strix.marxversion.ui.ds.CloseStar
import de.strix.marxversion.ui.ds.CountChip
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.DsSwitch
import de.strix.marxversion.ui.ds.HookPostIt
import de.strix.marxversion.ui.ds.MarxVoice
import de.strix.marxversion.ui.ds.MeBubble
import de.strix.marxversion.ui.ds.MoreChip
import de.strix.marxversion.ui.ds.OmmType
import de.strix.marxversion.ui.ds.OtherBubble
import de.strix.marxversion.ui.ds.Panel
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.StampRow
import de.strix.marxversion.ui.ds.SuggestChip
import de.strix.marxversion.ui.ds.Tag
import de.strix.marxversion.ui.ds.TextBtn
import de.strix.marxversion.ui.ds.Tonal
import de.strix.marxversion.ui.ds.Tone
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
 * 🎨 ds_gallery = th whole design system i 1 img (design_system.md § 11 · D-019 · D-030 ✔️ A+)
 * renders th REAL ui/ds components (nothing mocked): type scale · 5 btn kinds × states · chips · singletons ·
 * surfaces · 5 voices · stamp row · dialog panel ➡ 🦠 eyeballs th system, lanes check against it
 * run: ./gradlew testDebugUnitTest --tests '*DsGalleryTest*' -Pshots --rerun ➡ app/build/shots/ds_gallery.png
 */
private val PLAN_ORANGE = Ds.red.copy(red = .85f, green = .45f, blue = .1f)   // stand-in plan accent

/** a source th reports "pressed" ➡ static pressed state, same code path as a finger */
@Composable
private fun pressed(): MutableInteractionSource {
    val s = remember { MutableInteractionSource() }
    // interactions = SharedFlow w no replay ➡ wait 1 frame til th component's collector subscribed
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(50); s.emit(PressInteraction.Press(Offset(10f, 10f))) }
    return s
}

@Composable
private fun H(t: String) = Column(Modifier.padding(top = Space.xl, bottom = Space.s)) {
    Text(t.uppercase(), style = OmmType.bookMeta, color = Ds.red)
    HorizontalDivider(Modifier.padding(top = Space.xs), color = Ds.line)
}

@Composable
private fun Note(t: String) = Text(t, style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted, modifier = Modifier.padding(bottom = Space.xs))

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Left() = Column(Modifier.fillMaxWidth()) {
    Text("Oh My Marx · design system", style = MaterialTheme.typography.titleLarge, color = Ds.ink)
    Note("ds_gallery · L0 @1114 · D-030 A+")
    H("type")
    val ty = MaterialTheme.typography
    listOf<Pair<String, TextStyle>>(
        "display 28" to ty.displaySmall, "title-l 22" to ty.titleLarge, "title-m 18" to ty.titleMedium, "title-s 16" to ty.titleSmall,
        "body-l 16 · main sentences" to ty.bodyLarge, "body-m 14 · bubbles, callouts" to ty.bodyMedium,
        "label-l 14 · btn" to ty.labelLarge, "label-m 12 · chips, tags" to ty.labelMedium, "label-s 11 · refs (floor)" to ty.labelSmall,
    ).forEach { (t, s) -> Text(t, style = s, color = Ds.ink) }
    Text("Book title 24", style = OmmType.bookTitle, color = Ds.ink)
    Text("Book head 17 · rubric", style = OmmType.bookHead, color = Ds.red)
    Text("Book body 17 — the wealth of societies…", style = OmmType.bookBody, color = Ds.ink)
    Text("Quote 22 italic", style = OmmType.quote, color = Ds.blue)
    Text("BOOK META 11 · P. 49", style = OmmType.bookMeta, color = Ds.inkMuted)

    H("1 · primary")
    Note("L · rest · pressed (sinks into edge) · off · loading")
    Column(verticalArrangement = Arrangement.spacedBy(Space.s)) {
        Primary("Amen — read today", {})
        Primary("Amen — read today", {}, src = pressed())
        Primary("Amen — read today", {}, enabled = false)
        Primary("Amen — read today", {}, loading = true)
    }
    Note("S · rest · pressed · off")
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        Primary("Send", {}, large = false); Primary("Send", {}, large = false, src = pressed()); Primary("Send", {}, large = false, enabled = false)
    }
    H("2 · tonal  ·  3 · text")
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s), verticalAlignment = Alignment.CenterVertically) {
        Tonal("Copy link", {}); Tonal("Join", {}, src = pressed()); TextBtn("Later", {})
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s), verticalAlignment = Alignment.CenterVertically) {
        Tonal("Refresh", {}, enabled = false); TextBtn("Show me", {}); TextBtn("Leave circle", {}, danger = true)
    }
    H("4 · action item")
    Note("Circle · quote card row, About")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        ActionItem(R.drawable.ic_volume, "Listen", {}); ActionItem(R.drawable.ic_copy, "Copy", {})
        ActionItem(R.drawable.ic_book, "In book", {}); ActionItem(R.drawable.ic_share, "Share card", {})
        ActionItem(R.drawable.ic_send, "Share app", {}, tint = Ds.red)
    }
    Note("Bare · verse menu i th book")
    Row(Modifier.fillMaxWidth().background(Ds.paperHi)) {
        ActionItem(R.drawable.ic_volume, "Listen", {}, Modifier.weight(1f), style = ActStyle.Bare)
        ActionItem(R.drawable.ic_bookmark, "Bookmark", {}, Modifier.weight(1f), style = ActStyle.Bare, tint = Ds.red)
        ActionItem(R.drawable.ic_note, "Note", {}, Modifier.weight(1f), style = ActStyle.Bare)
        ActionItem(R.drawable.ic_copy, "Copy", {}, Modifier.weight(1f), style = ActStyle.Bare, src = pressed())
    }
    H("5 · chips")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s)) {
        ChoiceChip("Preacher", true, {}); ChoiceChip("Comrade", false, {})
        SuggestChip("Rent", {}); SuggestChip("Boss", {}); SuggestChip("Overtime", {})
    }
    H("singletons")
    Row(horizontalArrangement = Arrangement.spacedBy(Space.m), verticalAlignment = Alignment.CenterVertically) {
        CloseStar({}); DsSwitch(true, {}); DsSwitch(false, {}); MoreChip("read more", {}); CountChip(3, {}, "3 notes")
    }
}

@Composable
private fun Right() = Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.m)) {
    H("surfaces")
    Panel {
        Text("Panel · settings group", style = MaterialTheme.typography.titleSmall, color = Ds.ink)
        listOf("Reading size", "Reminder").forEachIndexed { i, t ->
            if (i > 0) HorizontalDivider(color = Ds.line)
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(t, style = MaterialTheme.typography.bodyLarge, color = Ds.ink, modifier = Modifier.weight(1f))
                DsSwitch(i == 1, {})
            }
        }
    }
    AccentPanel(PLAN_ORANGE, "⚙️", active = true, head = {
        Text("The Luddites", style = MaterialTheme.typography.titleMedium, color = Ds.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(Space.xs)) { Tag("Day 3 / 10", accent = PLAN_ORANGE); Tag("active", gold = true) }
    }) {
        Callout(Tone.accent(PLAN_ORANGE), lead = "🕯️", title = "Andacht") { Text("Machines don't hate you. Read why.") }
    }
    AccentPanel(Ds.blue, "📘", head = { Text("Commodity 101", style = MaterialTheme.typography.titleMedium, color = Ds.ink); Tag("7 days") })
    Callout(Tone.Gold, lead = "🎁", title = "New version") { Text("0.9.2 is ready — 2 fixes.") }
    Callout(Tone.Blue, lead = "💬", title = "And today?") { Text("Where did you see this at work?") }

    H("voices")
    MarxVoice("The wealth of those societies in which the capitalist mode of production prevails, presents itself as “an immense accumulation of commodities”.")
    MeBubble(lead = "✍") { Text("My boss calls this “synergy”.") }
    OtherBubble("Rosa", "2 h") { Text("Same at the print shop!") }
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { MeBubble { Text("Ha. Solidarity.") } }
    HookPostIt { Text("🪝 Trick: the streak makes you fear losing it.") }

    H("stamp card")
    StampRow(StampCard(1, 4, 3), doneToday = false, ring = Ds.outline)
    Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xFF1F2F4F), Color(0xFF8C1D18))), MaterialTheme.shapes.large)
        .padding(Space.m)) { StampRow(StampCard(1, 7, 3), doneToday = true) }

    H("dialog")
    MarxDialogPanel({}, title = "New badge", primary = DialogAction("Keep going") {}, defer = DialogAction("Later") {}) {
        Text("7 days in a row. Marx kept your page.", style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w840dp-h1600dp-xxhdpi", application = android.app.Application::class)
class DsGalleryTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    @Test fun gallery() {
        rule.setContent {
            MarxTheme(Lang.EN, null) {
                Row(Modifier.fillMaxSize().background(Ds.paper).padding(Space.l), horizontalArrangement = Arrangement.spacedBy(Space.xl)) {
                    Box(Modifier.weight(1f)) { Left() }
                    Box(Modifier.width(1.dp).fillMaxSize().background(Ds.line))
                    Box(Modifier.weight(1f)) { Right() }
                }
            }
        }
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1500)   // press springs settle
        rule.waitForIdle()
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        dir.mkdirs()
        File(dir, "ds_gallery.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
