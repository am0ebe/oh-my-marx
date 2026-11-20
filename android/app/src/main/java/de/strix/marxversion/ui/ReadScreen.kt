package de.strix.marxversion.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.T
import de.strix.marxversion.data.Verse
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.chapterDone
import de.strix.marxversion.ui.ds.CountChip
import androidx.lifecycle.repeatOnLifecycle
import de.strix.marxversion.ui.ds.GiltEdge
import de.strix.marxversion.ui.ds.Headpiece
import de.strix.marxversion.ui.ds.Ribbon
import de.strix.marxversion.ui.ds.Tailpiece
import de.strix.marxversion.ui.ds.roman
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.OmmType
import de.strix.marxversion.ui.ds.Space
import kotlinx.coroutines.launch

private val ABSCHNITTE = listOf(
    T("Vorworte", "Prefaces"), T("Ware und Geld", "Commodities and Money"),
    T("Die Verwandlung von Geld in Kapital", "The Transformation of Money into Capital"),
    T("Die Produktion des absoluten Mehrwerts", "The Production of Absolute Surplus-Value"),
    T("Die Produktion des relativen Mehrwerts", "The Production of Relative Surplus-Value"),
    T("Die Produktion des absoluten und relativen Mehrwerts", "The Production of Absolute and Relative Surplus-Value"),
    T("Der Arbeitslohn", "Wages"), T("Der Akkumulationsprozeß des Kapitals", "The Process of Accumulation of Capital"),
)

/**
 * 📚 Read list (design_system.md § 10): title-l · progress body-m + 4dp bar · Abschnitte = sticky red rubric heads (title-s)
 *   rows 56: ✔ (ok) / ○ + chapter title serif body-l + "p. N" label-s at th end
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadScreen(vm: AppVm, resume: (String, String) -> Unit = { _, _ -> }, openChapter: (String) -> Unit) {
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val done = remember(readIds) { chapterDone(vm.content.verses, readIds) }
    val n = done.values.count { it }
    val typo = MaterialTheme.typography
    LazyColumn(Modifier.fillMaxWidth()) {
        item {
            Column(Modifier.padding(horizontal = Space.gutter)) {
                Text(tr("Das Kapital, Band 1", "Capital, Volume 1"), style = typo.titleLarge, color = Ds.ink, modifier = Modifier.padding(top = Space.l))
                // QA r5: bar + % · % = paragraphs read (moves evy page, not only per finished chapter)
                val pct = bookPercent(readIds.size, vm.content.verses.size)
                Text(tr("%d von %d Kapiteln erledigt", "%d of %d chapters done").f(n, done.size), style = typo.bodyMedium, color = Ds.ink,
                    modifier = Modifier.padding(top = Space.xs))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = Space.s)) {
                    LinearProgressIndicator(progress = { pct / 100f }, color = Ds.red, trackColor = Ds.paperDeep, strokeCap = StrokeCap.Round,
                        gapSize = 0.dp, drawStopIndicator = {}, modifier = Modifier.weight(1f).height(4.dp))
                    Text("%d %%".f(pct), style = typo.labelMedium, color = Ds.inkMuted, modifier = Modifier.padding(start = Space.m))
                }
                // 📍 qa_1120 "app shd remember where i was" ➡ 1 tap back 2 th last passage on screen (📖 o 📜)
                s.readPos?.let { id -> vm.content.verse(id) }?.let { v ->
                    val ch = vm.content.chapters.firstOrNull { it.kap == v.kap }
                    de.strix.marxversion.ui.ds.Tonal(tr("📖 Weiterlesen · %s · MEW %s", "📖 Continue · %s · MEW %s")
                        .f(ch?.let { chapterTitle(it) } ?: v.kap, v.page ?: "–"), { resume(v.kap, v.id) },
                        Modifier.padding(bottom = Space.s))
                }
                HookNote(Hook.ENDOWED, s.testMode)
            }
        }
        var lastAb = -1
        vm.content.chapters.forEach { c ->
            if (c.abschnitt != lastAb) {
                lastAb = c.abschnitt
                val head = ABSCHNITTE.getOrNull(c.abschnitt)
                stickyHeader(key = "ab${c.abschnitt}") {
                    Text(head?.let { tr(it) } ?: "", style = typo.titleSmall, color = Ds.red,
                        modifier = Modifier.fillMaxWidth().background(Ds.paper)
                            .padding(start = Space.gutter, end = Space.gutter, top = Space.xl, bottom = Space.s))
                }
                // prefaces = 1 "on the house 🎁" under th head (was repeated on all 6 rows)
                if (c.kap.startsWith("V")) item(key = "ab${c.abschnitt}_gift") {
                    Text(tr("geschenkt 🎁", "on the house 🎁"), style = typo.labelMedium, color = Ds.inkMuted,
                        modifier = Modifier.padding(horizontal = Space.gutter))
                }
            }
            item(key = c.kap) {
                val key = if (c.kap.startsWith("V")) "V" else c.kap
                val label = when (key) {
                    "8" -> tr("schon gelebt (du arbeitest ja)", "already lived (you do work, right?)")
                    else -> ""
                }
                val isDone = done[key] == true
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { openChapter(c.kap) }
                    .padding(horizontal = Space.gutter, vertical = Space.s), verticalAlignment = Alignment.CenterVertically) {
                    DoneMark(isDone)
                    Column(Modifier.weight(1f).padding(horizontal = Space.m)) {
                        Text(chapterTitle(c), style = typo.bodyLarge.copy(fontFamily = FontFamily.Serif), color = Ds.ink)
                        if (label.isNotBlank()) Text(label, style = typo.labelMedium, color = Ds.inkMuted)
                    }
                    c.page?.let { Text(tr("S. %d", "p. %d").f(it), style = typo.labelSmall, color = Ds.inkMuted) }
                }
                HorizontalDivider(color = Ds.line, modifier = Modifier.padding(horizontal = Space.gutter))
            }
        }
    }
}

/** ✔ ok-green check (read) / ○ outline (not yet) · shape differs, not only colour (§ 9) */
@Composable
private fun DoneMark(done: Boolean) {
    val state = if (done) tr("gelesen", "read") else tr("ungelesen", "unread")
    Canvas(Modifier.size(20.dp).semantics { stateDescription = state }) {
        val w = size.width
        if (done) {
            val st = 2.5.dp.toPx()
            drawLine(Ds.ok, Offset(w * .14f, w * .54f), Offset(w * .40f, w * .80f), st, StrokeCap.Round)
            drawLine(Ds.ok, Offset(w * .40f, w * .80f), Offset(w * .88f, w * .22f), st, StrokeCap.Round)
        } else drawCircle(Ds.outline, radius = w * .38f, style = Stroke(1.5.dp.toPx()))
    }
}

/** 📊 whole-book % (floor · started = ≥ 1 % · all read = 100) */
fun bookPercent(read: Int, total: Int): Int = when {
    total <= 0 || read <= 0 -> 0
    read >= total -> 100
    else -> (read * 100 / total).coerceIn(1, 99)
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

/** th desk under th book (darker ink) · dog-ear flap = page w a little gold-brown · no raw hex (guard) */
private val DESK = lerp(Ds.ink, Color.Black, .3f)
private val FLAP_DARK = lerp(Ds.page, Ds.goldDeep, .16f)

/**
 * 📖 th book (strix @1103: "like th actual old book, swipe ➡ th page turns"):
 *  whole Kapital as 1 stack o pages · drag ⬅ = th page lifts off th spine + turns · drag ➡ = turn back
 *  🤏 pinch = font size (saved) · long page ➡ scroll down inside it
 *  tap o hold anywhere = turn (left half back · right fwd) · double-tap a passage = its menu (D-057) · 📐 dog-ear = turn
 * Own pager (not HorizontalPager): th turning page must be drawn ABOVE th next one, pinned at th spine.
 * D-031 b: slim paper head on top (back · chapter · 💬 N) · app bars hidden by MainActivity (L5)
 */
@Composable
fun ChapterScreen(vm: AppVm, kap: String, focus: String?, @Suppress("UNUSED_PARAMETER") goChapter: (String) -> Unit = {}) {
    val pages = remember { bookPages(vm.content.verses) }
    // 📍 qa_1120 resume = Read list "Continue" btn (passes focus = saved readPos) · a chapter row = its start (explicit)
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
    LaunchedEffect(index, s.readScroll) { if (!s.readScroll) vm.setReadPos(pages[index].verses.first().id) }   // 📍 📖 position
    // ⏱️ D-062 reading time: 15 s ticks while th app is resumed AND u touched th reader ≤ 90 s ago (no input = nothing counts)
    //   1 sitting ≥ 60 active min ➡ "longshift" (Überstunden badge) · session = this screen visit
    var lastInput by remember { androidx.compose.runtime.mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(index) { lastInput = System.currentTimeMillis() }   // a turn = input too
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        var session = 0L
        lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.RESUMED) {
            while (true) {
                kotlinx.coroutines.delay(de.strix.marxversion.logic.TICK_S * 1000)
                val s = de.strix.marxversion.logic.activeSecs(System.currentTimeMillis() - lastInput)
                vm.addReadSecs(s); session += s
                if (session >= de.strix.marxversion.logic.LONG_SHIFT_S) vm.flag("longshift")
            }
        }
    }
    // 💬 comments: load th chapter's room when entering a chapter · sheet = th page whose 💬 was tapped
    LaunchedEffect(pages[index].kap, s.community) { vm.loadComments(pages[index].kap) }
    var talk by remember { mutableStateOf<BookPage?>(null) }
    talk?.let { p -> CommentsSheet(vm, p.kap, p.page) { talk = null } }

    // 👉 turn by code (double-tap · dog-ear · hint) = same animation as a finished drag · ignored mid-turn
    val fwd: () -> Unit = {
        if (index < pages.lastIndex && !turn.isRunning && turn.value == 0f) scope.launch {
            turn.animateTo(-1f, tween(320)); index++; turn.snapTo(0f)
        }
    }
    val back: () -> Unit = {
        if (index > 0 && !turn.isRunning && turn.value == 0f) scope.launch {
            turn.animateTo(1f, tween(320)); index--; turn.snapTo(0f)
        }
    }
    // 👆 tap / hold = turn: left half ➡ back · right half ➡ fwd · 👆👆 double-tap a passage = its menu (D-057, swaps D-044)
    //   all 3 = 1 detector on th book (below) · passages only report their boxes (BookTaps)
    val book = remember { BookTaps() }
    var bookAt by remember { mutableStateOf(Offset.Zero) }   // th book box's window offset (tap pos ➡ window coords)
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    // (QA r5 peek + hint pill rm'd @1116 qa_1116 L2: "distracting" ➡ th dog-ears r th only hint · git: 65a2639)
    // 📖 ⬄ 📜 (qa_1116 Q4/Q7, 🦠 @1116): 2 reading modes til 🦠 picks 1 design · toggle i th slim head · remembers last
    //   📜 starts at th current page's 1st passage (fixed when th mode flips) · scrolled past ➡ index follows ➡ 📖 resumes there
    val scrollMode = s.readScroll
    val pageOf = remember(pages) { buildMap { pages.forEachIndexed { i, p -> p.verses.forEach { put(it.id, i) } } } }
    val scrollFrom = remember(scrollMode) {
        pages[index].verses.first().id.let { id -> vm.content.verses.indexOfFirst { it.id == id }.coerceAtLeast(0) }
    }

    // 💡 qa_1117 r3: mode chip pressed ➡ show wh th new mode does: 📖 page lifts a bit + "tap edge / swipe" pill ·
    //   📜 bouncing ⬇ + "scroll on" pill · 2.5 s, then gone (only on press: th r5 always-on peek was "distracting")
    var hintKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(hintKey) {
        if (hintKey > 0 && !scrollMode && turn.value == 0f) { turn.animateTo(-0.14f, tween(380)); turn.animateTo(0f, tween(320)) }
    }

    // 📜 scroll mode = no pages ➡ no turn ➡ a passage tap = its menu again (like Today)
    CompositionLocalProvider(LocalReadScale provides scale, LocalBook provides if (scrollMode) null else book) {
     Box(Modifier.fillMaxSize()) {
      // ⌨️ imePadding: th page shrinks above th keyboard ➡ a note at th page bottom stays visible (qa_1116 r2.5)
      Column(Modifier.fillMaxSize().background(Ds.page).imePadding()
          // ⏱️ D-062: any touch on th reader = "still reading" (Initial pass, consumes nothing)
          .pointerInput(Unit) { awaitPointerEventScope { while (true) { awaitPointerEvent(PointerEventPass.Initial); lastInput = System.currentTimeMillis() } } }) {
        SlimHead(vm, pages[index], scrollMode, { vm.setReadScroll(!scrollMode); hintKey++ }) { talk = pages[index] }
        if (scrollMode) {
            // 📜 = whole book, lazy, both ways (qa_1120 🐛 stuck + no way back) · top passage ➡ index = th 📖 page u land on
            ScrollBook(vm, scrollFrom, speaker, Modifier.weight(1f).fillMaxWidth()) { id ->
                pageOf[id]?.let { if (it != index) index = it }
                vm.setReadPos(id)
            }
        } else Box(
            Modifier.weight(1f).fillMaxWidth().background(DESK)  // th desk under th book
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
                }
                // 👆 D-057 (🦠 @1120 "swap"): tap o hold anywhere on th book (passage o paper) = turn · left half back ·
                //   right half fwd · double-tap a passage = its menu (+ buzz) · passages hv no click o their own (🐛 1009)
                //   (was D-044: double-tap = turn · tap = menu · edge quarters = turn)
                .onGloballyPositioned { bookAt = it.positionInWindow() }
                .pointerInput(index, pages.size) {
                    val at = { o: Offset -> if (o.x < size.width / 2) back() else fwd() }
                    detectTapGestures(onTap = at, onLongPress = at, onDoubleTap = { o ->
                        if (book.menuAt(o + bookAt)) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    })
                },
        ) {
            val t = turn.value
            val cam = 18 * density.density
            // under: th page revealed (fwd: nxt page · back: current) · shadow fades as th turning page leaves
            val under = if (t < 0) index + 1 else index
            val over = if (t < 0) index else if (t > 0) index - 1 else -1
            key(under) { Page(vm, pages[under], speaker, under, pages.size, Modifier.fillMaxSize(), flashId, pages.getOrNull(under + 1)) }
            // fwd: covered at th start (dark) ➡ revealed (light) · back: gets covered (light ➡ dark)
            if (t != 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f * if (t < 0) 1f + t else t)))
            if (over >= 0) key(over) {
                // turning page: hinged at th left edge (spine) · fwd: 0 ➡ -90° · back: -90° ➡ 0
                val angle = if (t < 0) 90f * t else -90f * (1f - t)
                Page(vm, pages[over], speaker, over, pages.size, Modifier.fillMaxSize().graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = angle
                    cameraDistance = cam
                }, null, pages.getOrNull(over + 1))
            }
            // 📐 dog-ear = "this is a page, it turns" · bottom-right = fwd · tap = turn
            //   left (back) one rm'd qa_1117 r3 · going back = tap th left quarter o swipe ➡ still works
            if (index < pages.lastIndex && t == 0f) DogEar(Modifier.align(Alignment.BottomEnd), fwd)
        }
      }
      if (hintKey > 0) key(hintKey) { ModeHint(scrollMode, Modifier.align(Alignment.BottomCenter).padding(bottom = Space.xl)) }
     }
    }
}

/** 💡 2.5 s pill aftr th mode chip: wh th new mode does · 📜 ⬇ bounces · fades out (qa_1117 r3) */
@Composable
private fun ModeHint(scrollMode: Boolean, modifier: Modifier) {
    val a = remember { Animatable(0f) }
    val bob = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, tween(200)); delay(2300); a.animateTo(0f, tween(400)) }
    LaunchedEffect(Unit) { if (scrollMode) repeat(3) { bob.animateTo(1f, tween(260)); bob.animateTo(0f, tween(260)) } }
    Row(modifier.graphicsLayer { alpha = a.value }.clip(CircleShape)   // pill = ds Pill (CircleShape).background(Ds.ink.copy(alpha = .82f))
        .padding(horizontal = Space.m, vertical = Space.s), verticalAlignment = Alignment.CenterVertically) {
        if (scrollMode) Text("⬇", color = Ds.page, style = OmmType.bookMeta,
            modifier = Modifier.graphicsLayer { translationY = bob.value * 6 * density }.padding(end = Space.s))
        Text(if (scrollMode) tr("Endlos: einfach nach unten scrollen", "Endless: just keep scrolling down")
            else tr("Buch: tippen = umblättern (links zurück, rechts weiter) · doppelt tippen = Absatz-Menü",
                "Book: tap to turn (left = back, right = next) · double-tap = passage menu"),
            color = Ds.page, style = OmmType.bookMeta)
    }
}

/**
 * D-031 b: slim paper head = back · chapter title (serif) · CountChip "💬 N" (oth ppl's notes on this page ➡ sheet)
 * page colour, 1dp line under it · th app's top bar + bottom nav r hidden on this route (MainActivity, L5)
 */
@Composable
private fun SlimHead(vm: AppVm, p: BookPage, scrollMode: Boolean, onMode: () -> Unit, onTalk: () -> Unit) {
    val chapter = vm.content.chapters.firstOrNull { it.kap == p.kap }
    val s by vm.settings.collectAsStateWithLifecycle()
    val all by vm.comments.collectAsStateWithLifecycle()
    val talkCount = all[p.kap]?.count { it.page == p.page } ?: 0
    val talkOn = s.community && vm.commentsAvailable
    val back = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Column(Modifier.fillMaxWidth().background(Ds.page)) {
        Row(Modifier.fillMaxWidth().height(Space.tap).padding(end = Space.s), verticalAlignment = Alignment.CenterVertically) {
            BackArrow(tr("Zurück", "Back")) { back?.onBackPressed() }
            Text(chapter?.let { chapterTitle(it) } ?: p.kap, style = OmmType.bookHead, color = Ds.ink,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(end = Space.s))
            if (talkOn && talkCount > 0) CountChip(talkCount, onTalk, tr("%d Kommentare zu dieser Seite", "%d comments on this page").f(talkCount))
            // 📖 ⬄ 📜: chip shows th mode u switch TO · + 1 word (qa_1117 r3: emoji alone = "wh does it do?")
            de.strix.marxversion.ui.ds.HeadChip(if (scrollMode) "📖 " + tr("Seiten", "Pages") else "📜 " + tr("Scrollen", "Scroll"), onMode,
                if (scrollMode) tr("Als Buch mit Seiten lesen", "Read as a book with pages") else tr("Endlos scrollen", "Endless scroll"),
                Modifier.padding(start = Space.xs))
        }
        HorizontalDivider(color = Ds.line)
    }
}

/** ← back, drawn (no icon font) · 48 target · mirrored i RTL · 🤖 candidate 4 ui/ds at fan-in (sub-screen bars, L5) */
@Composable
private fun BackArrow(desc: String, onClick: () -> Unit) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Box(Modifier.size(Space.tap).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc },
        contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(24.dp).graphicsLayer { if (rtl) scaleX = -1f }) {
            val w = size.width; val st = 2.dp.toPx()
            drawLine(Ds.ink, Offset(w * .2f, w * .5f), Offset(w * .82f, w * .5f), st, StrokeCap.Round)
            val head = Path().apply { moveTo(w * .46f, w * .22f); lineTo(w * .2f, w * .5f); lineTo(w * .46f, w * .78f) }
            drawPath(head, Ds.ink, style = Stroke(st, cap = StrokeCap.Round))
        }
    }
}

/** 📖 book taps (D-057): ONE detector on th book owns tap · hold · double-tap ➡ passages r NOT clickable i th book
 *  🐛 DEV 1009 (🦠 @1120 "tap still selects th ¶"): passage = combinedClickable ➡ evy tap painted a ripple over th whole ¶
 *  (= "selected") + th turn waited 300 ms 4 a maybe-2nd tap · now: passages only report their box (window coords) ·
 *  double-tap ➡ `menu` = {id, n} ➡ that VerseView toggles its menu · null = not i th 📖 (Today · 📜 scroll) ➡ tap = menu */
class BookTaps {
    val boxes = mutableMapOf<String, androidx.compose.ui.geometry.Rect>()
    var menu by mutableStateOf<Pair<String, Int>?>(null)
    private var n = 0
    /** double-tap at `p` (window coords) ➡ th passage under it, if any, toggles its menu */
    fun menuAt(p: Offset): Boolean = boxes.entries.firstOrNull { it.value.contains(p) }?.let { menu = it.key to ++n; true } ?: false
}
val LocalBook = compositionLocalOf<BookTaps?> { null }

/** 📐 folded page corner: desk shows thru th cut, th flap lies on th page w a soft shadow */
@Composable
private fun DogEar(modifier: Modifier, onClick: () -> Unit, left: Boolean = false) {
    val desc = if (left) tr("Zurückblättern", "Previous page") else tr("Umblättern", "Turn the page")
    // 48 target (§ 9) · th drawn corner stays 36 at th very corner · left = mirrored (back)
    Box(modifier.size(Space.tap).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc },
        contentAlignment = if (left) Alignment.BottomStart else Alignment.BottomEnd) {
        Canvas(Modifier.size(36.dp).graphicsLayer { if (left) scaleX = -1f }) {
            val w = size.width; val h = size.height
            val cut = Path().apply { moveTo(w, 0f); lineTo(w, h); lineTo(0f, h); close() }
            drawPath(cut, DESK)
            val flap = Path().apply { moveTo(0f, h); lineTo(w, 0f); lineTo(0f, 0f); close() }
            drawPath(flap, Brush.linearGradient(listOf(FLAP_DARK, Ds.page), start = Offset(w / 2, h / 2), end = Offset(0f, 0f)))
            drawLine(Color.Black.copy(alpha = .33f), Offset(0f, h), Offset(w, 0f), strokeWidth = 1.5f)
        }
    }
}

/** 1 sheet o old paper: running head (chapter · MEW page) · paragraphs · folio at th bottom */
@Composable
private fun Page(vm: AppVm, p: BookPage, speaker: Speaker, i: Int, n: Int, modifier: Modifier, flashId: String?, next: BookPage? = null) {
    val chapter = vm.content.chapters.firstOrNull { it.kap == p.kap }
    val reads by vm.pageReads.collectAsStateWithLifecycle()
    val firstOfChapter = p.verses.first().let { f -> vm.content.chapter(p.kap).firstOrNull()?.id == f.id }
    // 📜 C4 holy book (D-053 b): chapter ends on this page ➡ tailpiece (+ ☭ seal if read) · 🔖 on th page ➡ ribbon
    val lastOfChapter = next == null || next.kap != p.kap
    val marks by vm.marks.collectAsStateWithLifecycle()
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val bookmarked = remember(marks, p) { p.verses.any { v -> marks.any { it.verseId == v.id && it.bookmark } } }
    val sealed = lastOfChapter && remember(readIds, p.kap) { vm.content.chapter(p.kap).let { vs -> vs.isNotEmpty() && vs.count { it.id in readIds } >= vs.size * 0.9 } }
    Box(modifier.background(Ds.page).background(
        // spine shadow left + page-edge darkening right = it looks bound
        Brush.horizontalGradient(0f to Color.Black.copy(alpha = .2f), 0.06f to Color.Transparent, 0.94f to Color.Transparent,
            1f to Color.Black.copy(alpha = .13f)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = Space.readerStart, end = Space.readerEnd, top = Space.m, bottom = Space.l)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // (7) running head: verso (even) = DAS KAPITAL · recto (odd) = chapter title
                Text(if (i % 2 == 0) tr("DAS KAPITAL", "CAPITAL") else (chapter?.let { chapterTitle(it) } ?: p.kap).uppercase(),
                    style = OmmType.bookMeta, color = Ds.inkMuted, maxLines = 1, modifier = Modifier.weight(1f))
                // 👥 Mitlesen: how often this page was read (social proof, opt-in) · muted, red = act only
                p.page?.let { pg -> reads[pg]?.let { Text("👥 %d×  ·  ".f(it), style = OmmType.bookMeta, color = Ds.inkMuted) } }
                p.page?.let { Text(tr("MEW S. %d", "MEW p. %d").f(it), style = OmmType.bookMeta, color = Ds.inkMuted) }
            }
            HorizontalDivider(color = Ds.line, modifier = Modifier.padding(vertical = Space.s))
            if (firstOfChapter) {
                // (5) headpiece + (4) incipit (Roman numeral · prefaces = "das Vorwort")
                Headpiece(Modifier.padding(top = Space.m))
                val no = p.kap.toIntOrNull()
                Text(if (no != null) tr("HIER BEGINNT DAS KAPITEL %s", "HERE BEGINS CHAPTER %s").f(roman(no))
                    else tr("HIER BEGINNT DAS VORWORT", "HERE BEGINS THE PREFACE"),
                    style = OmmType.bookMeta, color = Ds.red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Space.s))
                Text(chapter?.let { chapterTitle(it) } ?: p.kap, style = OmmType.bookTitle, color = Ds.ink,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Space.xs, bottom = Space.l))
            }
            p.verses.forEachIndexed { k, v ->
                val prev = p.verses.getOrNull(k - 1)
                if (v.secTitle.isNotBlank() && v.sec != prev?.sec) {
                    if (prev != null) Ornament()
                    SectionTitle(secTitle(v))
                }
                VerseView(vm, v, speaker, flash = v.id == flashId, dropCap = firstOfChapter && k == 0, showRef = false,
                    sectionCap = v.secTitle.isNotBlank() && v.sec != prev?.sec)
                Gap(4)
            }
            // QA r4: no share pills (r3) + no "Mitreden" btn down here = pure Marx page · writing = tap a passage ➡ ✍️ Notiz
            //   (👥 chip there posts it 2 th Lesesaal) · reading oth's = 💬 N i th slim head (D-031 b)
            // (6) tailpiece at chapter end · ☭ seal = chapter read (≥ 90 %, T2b)
            if (lastOfChapter && next != null) Tailpiece(sealed)
            // (11) explicit + colophon at th book's end
            if (next == null) {
                Tailpiece(sealed)
                Text(tr("HIER ENDET DER ERSTE BAND", "HERE ENDS THE FIRST VOLUME"), style = OmmType.bookMeta, color = Ds.red,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Text(tr("Gesetzt in Literata · gedruckt auf der Pseudopod", "Set in Literata · printed aboard the Pseudopod"),
                    style = OmmType.bookMeta, color = Ds.inkMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Space.xs))
                // 🎖️ level-6 perk (D-050): ur name i th colophon
                val logDays by vm.days.collectAsStateWithLifecycle()
                val myName by vm.mxName.collectAsStateWithLifecycle()
                if (de.strix.marxversion.logic.cardsDone(de.strix.marxversion.logic.readingDays(logDays)) >= 6)
                    Text(tr("Gelesen von %s — Klassiker-Status", "Read by %s — classic status").f(myName ?: vm.suggestedName),
                        style = OmmType.bookMeta, color = Ds.goldDeep, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Space.xs))
            }
            // (8) catchword rm'd (🦠 qa_1120 "Y? confusing" · looked like a stray artifact)
            Gap(20)
            Text("— ${p.page ?: (i + 1)} —", style = OmmType.bookMeta.copy(fontFamily = FontFamily.Serif), color = Ds.inkMuted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            // 👆 wh u can press (qa_1117 r3, like Today's "⬅ swipe · ⬇ pull · 📳 shake" line) · tiny + muted, under th folio
            //   D-057: tap/hold = turn by side · double-tap a passage = menu
            Text(tr("👆 tippen: links = zurück · rechts = weiter · 👆👆 Absatz doppelt tippen = Menü · ↔ wischen · 🤏 Schriftgröße",
                "👆 tap: left = back · right = next · 👆👆 double-tap a passage = menu · ↔ swipe · 🤏 pinch = text size"),
                style = OmmType.bookMeta, color = Ds.inkMuted.copy(alpha = .7f), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = Space.s))
        }
        // (10) gilt edge on th outer edge · (9) 🔖 page = red silk ribbon fr th top (rtl: Alignment mirrors both)
        GiltEdge(Modifier.align(Alignment.CenterEnd))
        if (bookmarked) Ribbon(Modifier.align(Alignment.TopEnd).padding(end = Space.xl))
    }
}

/** ❦ betw sections i th book · 30 % ink, decor only */
@Composable
private fun Ornament() = Text("❦", style = OmmType.bookHead, color = Ds.ink.copy(alpha = .3f), textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().padding(top = Space.l))
