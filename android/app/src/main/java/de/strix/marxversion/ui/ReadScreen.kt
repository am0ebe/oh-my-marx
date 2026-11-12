package de.strix.marxversion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import kotlin.math.abs
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import de.strix.marxversion.data.Verse
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.chapterDone

private val ABSCHNITTE = listOf(
    T("Vorworte", "Prefaces"), T("Ware und Geld", "Commodities and Money"),
    T("Die Verwandlung von Geld in Kapital", "The Transformation of Money into Capital"),
    T("Die Produktion des absoluten Mehrwerts", "The Production of Absolute Surplus-Value"),
    T("Die Produktion des relativen Mehrwerts", "The Production of Relative Surplus-Value"),
    T("Die Produktion des absoluten und relativen Mehrwerts", "The Production of Absolute and Relative Surplus-Value"),
    T("Der Arbeitslohn", "Wages"), T("Der Akkumulationsprozeß des Kapitals", "The Process of Accumulation of Capital"),
)

@Composable
fun ReadScreen(vm: AppVm, openChapter: (String) -> Unit) {
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val done = remember(readIds) { chapterDone(vm.content.verses, readIds) }
    val n = done.values.count { it }
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(tr("Das Kapital, Band 1", "Capital, Volume 1"), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text(tr("%d von %d erledigt", "%d of %d done").f(n, done.size), fontSize = 13.sp)
            LinearProgressIndicator(progress = { n / done.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
            HookNote(Hook.ENDOWED, s.testMode)
        }
        var lastAb = -1
        vm.content.chapters.forEach { c ->
            if (c.abschnitt != lastAb) {
                lastAb = c.abschnitt
                item { SectionTitle(ABSCHNITTE.getOrNull(c.abschnitt)?.let { tr(it) } ?: "") }
            }
            item {
                val key = if (c.kap.startsWith("V")) "V" else c.kap
                val label = when {
                    key == "V" -> tr("geschenkt 🎁", "on the house 🎁")
                    key == "8" -> tr("schon gelebt (du arbeitest ja)", "already lived (you do work, right?)")
                    else -> ""
                }
                Row(Modifier.fillMaxWidth().clickable { openChapter(c.kap) }.padding(vertical = 8.dp)) {
                    Text(if (done[key] == true) "✔️ " else "○ ", fontSize = 15.sp)
                    Column {
                        Text(chapterTitle(c), fontSize = 15.sp)
                        // sweep r4: no dangling " · " when a preface has no page
                        Text(listOfNotNull(c.page?.let { tr("S. %d", "p. %d").f(it) }, label.ifBlank { null }).joinToString(" · "),
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

/** 🤏 reader font scale (pinch) · only th chapter reader provides it, evywhere else = 1 */
val LocalReadScale = compositionLocalOf { 1f }

/** 1 page o th book = th paragraphs that start on 1 MEW page (= strix's paper copy) — 708 pages, ~2 000 chars each */
data class BookPage(val kap: String, val page: Int?, val verses: List<Verse>)

fun bookPages(all: List<Verse>): List<BookPage> {
    val out = mutableListOf<BookPage>()
    for (v in all) {
        val last = out.lastOrNull()
        if (last != null && last.kap == v.kap && last.page == v.page) out[out.lastIndex] = last.copy(verses = last.verses + v)
        else out += BookPage(v.kap, v.page, listOf(v))
    }
    return out
}

private val PAPER = Color(0xFFF3E7CF)
private val INK = Color(0xFF3B2A1A)

/**
 * 📖 th book (strix @1103: "like th actual old book, swipe ➡ th page turns"):
 *  whole Kapital as 1 stack o pages · drag ⬅ = th page lifts off th spine + turns · drag ➡ = turn back
 *  🤏 pinch = font size (saved) · long page ➡ scroll down inside it
 *  verse: tap = menu · double-tap = 🔖 + ☭ burst · hold = menu
 * Own pager (not HorizontalPager): th turning page must be drawn ABOVE th next one, pinned at th spine.
 */
@Composable
fun ChapterScreen(vm: AppVm, kap: String, focus: String?, @Suppress("UNUSED_PARAMETER") goChapter: (String) -> Unit = {}) {
    val pages = remember { bookPages(vm.content.verses) }
    val start = remember(kap, focus) {
        pages.indexOfFirst { p -> p.verses.any { it.id == focus } }.takeIf { it >= 0 } ?: pages.indexOfFirst { it.kap == kap }.coerceAtLeast(0)
    }
    var index by remember(start) { mutableIntStateOf(start) }
    // ✨ "Im Buch" (Today card) landed on `focus` ➡ that passage pulses once · turned away = done (no re-flash on return)
    var flashId by remember(focus) { mutableStateOf(focus) }
    LaunchedEffect(index) { if (index != start) flashId = null }
    val turn = remember { Animatable(0f) }   // < 0 = current page turning away (fwd) · > 0 = previous page turning back
    val scope = rememberCoroutineScope()
    val s by vm.settings.collectAsStateWithLifecycle()
    var scale by remember(s.readScale) { mutableFloatStateOf(s.readScale) }
    val speaker = rememberSpeaker()
    val density = LocalDensity.current
    LaunchedEffect(index) { vm.read(pages[index].verses.map { it.id }); vm.pageSeen(pages[index].page) }  // page seen = read (+ 👥 count)
    // 💬 comments: load th chapter's room when entering a chapter · sheet = th page whose 💬 was tapped
    LaunchedEffect(pages[index].kap, s.community) { vm.loadComments(pages[index].kap) }
    var talk by remember { mutableStateOf<BookPage?>(null) }
    talk?.let { p -> CommentsSheet(vm, p.kap, p.page) { talk = null } }

    CompositionLocalProvider(LocalReadScale provides scale) {
        Box(
            Modifier.fillMaxSize().background(Color(0xFF2A1E14))  // th desk under th book
                .pointerInput(Unit) {  // 🤏 pinch (Initial pass: before th page scrolls)
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var pinched = false
                        do {
                            val ev = awaitPointerEvent(PointerEventPass.Initial)
                            if (ev.changes.count { it.pressed } >= 2) {
                                val z = ev.calculateZoom()
                                if (z != 1f) { scale = (scale * z).coerceIn(0.7f, 2f); pinched = true }
                                ev.changes.forEach { it.consume() }
                            }
                        } while (ev.changes.any { it.pressed })
                        if (pinched) vm.setReadScale(scale)
                    }
                }
                .pointerInput(pages.size) {  // 👉 drag = turn
                    val w = size.width.toFloat()
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { ch, dx ->
                            ch.consume()
                            val lo = if (index < pages.lastIndex) -1f else 0f
                            val hi = if (index > 0) 1f else 0f
                            scope.launch { turn.snapTo((turn.value + dx / w).coerceIn(lo, hi)) }
                        },
                        onDragEnd = {
                            scope.launch {
                                val t = turn.value
                                when {
                                    t < -0.25f -> { turn.animateTo(-1f, tween(260)); index++; turn.snapTo(0f) }
                                    t > 0.25f -> { turn.animateTo(1f, tween(260)); index--; turn.snapTo(0f) }
                                    else -> turn.animateTo(0f, tween(180))
                                }
                            }
                        },
                        onDragCancel = { scope.launch { turn.animateTo(0f) } },
                    )
                },
        ) {
            val t = turn.value
            val cam = 18 * density.density
            // under: th page revealed (fwd: nxt page · back: current) · shadow fades as th turning page leaves
            val under = if (t < 0) index + 1 else index
            val over = if (t < 0) index else if (t > 0) index - 1 else -1
            key(under) { Page(vm, pages[under], speaker, under, pages.size, Modifier.fillMaxSize(), flashId) { talk = pages[under] } }
            // fwd: covered at th start (dark) ➡ revealed (light) · back: gets covered (light ➡ dark)
            if (t != 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f * if (t < 0) 1f + t else t)))
            if (over >= 0) key(over) {
                // turning page: hinged at th left edge (spine) · fwd: 0 ➡ -90° · back: -90° ➡ 0
                val angle = if (t < 0) 90f * t else -90f * (1f - t)
                Page(vm, pages[over], speaker, over, pages.size, Modifier.fillMaxSize().graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = angle
                    cameraDistance = cam
                }, null) { talk = pages[over] }
            }
        }
    }
}

/** 1 sheet o old paper: running head (chapter · MEW page) · paragraphs · folio at th bottom */
@Composable
private fun Page(vm: AppVm, p: BookPage, speaker: Speaker, i: Int, n: Int, modifier: Modifier, flashId: String?, onTalk: () -> Unit) {
    val chapter = vm.content.chapters.firstOrNull { it.kap == p.kap }
    val reads by vm.pageReads.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val all by vm.comments.collectAsStateWithLifecycle()
    val talkCount = all[p.kap]?.count { it.page == p.page } ?: 0
    val talkOn = s.community && vm.commentsAvailable
    val firstOfChapter = p.verses.first().let { f -> vm.content.chapter(p.kap).firstOrNull()?.id == f.id }
    Box(modifier.background(PAPER).background(
        // spine shadow left + page-edge darkening right = it looks bound
        Brush.horizontalGradient(0f to Color(0x33000000), 0.06f to Color.Transparent, 0.94f to Color.Transparent, 1f to Color(0x22000000)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 22.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text((chapter?.let { chapterTitle(it) } ?: p.kap).uppercase(), fontSize = 10.sp, color = INK.copy(alpha = 0.6f),
                    letterSpacing = 1.2.sp, maxLines = 1, modifier = Modifier.weight(1f))
                // 👥 Mitlesen: how often this page was read (social proof, opt-in)
                p.page?.let { pg -> reads[pg]?.let { Text("👥 %d×  ·  ".f(it), fontSize = 10.sp, color = Red.copy(alpha = 0.8f)) } }
                if (talkOn && talkCount > 0) Text("💬 %d  ·  ".f(talkCount), fontSize = 10.sp, color = Red.copy(alpha = 0.8f),
                    modifier = Modifier.clickable(onClick = onTalk))
                p.page?.let { Text(tr("MEW S. %d", "MEW p. %d").f(it), fontSize = 10.sp, color = INK.copy(alpha = 0.6f)) }
            }
            HorizontalDivider(color = INK.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 6.dp))
            if (firstOfChapter) Text(chapter?.let { chapterTitle(it) } ?: p.kap, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = INK,
                fontFamily = FontFamily.Serif, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp))
            p.verses.forEachIndexed { k, v ->
                val prev = p.verses.getOrNull(k - 1)
                if (v.secTitle.isNotBlank() && v.sec != prev?.sec) SectionTitle(v.secTitle)
                VerseView(vm, v, speaker, flash = v.id == flashId)
                Gap(4)
            }
            // QA r4: no share pills (r3) + no "Mitreden" btn down here = pure Marx page · writing = tap a passage ➡ ✍️ Notiz
            //   (👥 chip there posts it 2 th Lesesaal) · reading oth's = 💬 N i th running head
            Gap(20)
            Text("— ${p.page ?: (i + 1)} —", fontSize = 12.sp, color = INK.copy(alpha = 0.55f), fontFamily = FontFamily.Serif,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (i == 0 || i == n - 1) Text(
                tr("⬅ ziehen = umblättern · 🤏 zwei Finger = Schrift · doppelt tippen = 🔖", "⬅ drag = turn the page · 🤏 two fingers = font · double-tap = 🔖"),
                fontSize = 11.sp, color = INK.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
        }
    }
}
