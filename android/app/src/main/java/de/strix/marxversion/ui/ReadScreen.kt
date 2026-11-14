package de.strix.marxversion.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
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
fun ReadScreen(vm: AppVm, openChapter: (String) -> Unit) {
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
 *  verse: tap = menu · hold = menu · double-tap anywhere = turn (QA r5, was 🔖) · 📐 dog-ear = turn
 * Own pager (not HorizontalPager): th turning page must be drawn ABOVE th next one, pinned at th spine.
 * D-031 b: slim paper head on top (back · chapter · 💬 N) · app bars hidden by MainActivity (L5)
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

    // 👉 turn by code (double-tap · dog-ear · hint) = same animation as a finished drag · ignored mid-turn
    val fwd: () -> Unit = {
        if (index < pages.lastIndex && !turn.isRunning && turn.value == 0f) scope.launch {
            turn.animateTo(-1f, tween(320)); index++; turn.snapTo(0f)
        }
    }
    // 💡 QA r5 "page turn = NICE, but hidden": th 1st few chapters th page peeks (lifts 2× at th corner) + a hint pill,
    //   until u turned 3 pages urself · counter i plain SharedPreferences (UI hint, not user data)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val hintPrefs = remember { ctx.getSharedPreferences("omm_ui", android.content.Context.MODE_PRIVATE) }
    var turnsDone by remember { mutableIntStateOf(hintPrefs.getInt("turns", 0)) }
    LaunchedEffect(index) {
        if (index != start && turnsDone < TURN_HINT) { turnsDone++; hintPrefs.edit().putInt("turns", turnsDone).apply() }
    }
    var hintOn by remember { mutableStateOf(turnsDone < TURN_HINT) }   // pill: ~6 s per chapter open, gone on th 1st turn
    LaunchedEffect(index) { if (index != start) hintOn = false }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(6000); hintOn = false }
    LaunchedEffect(Unit) {
        if (turnsDone >= TURN_HINT || index >= pages.lastIndex) return@LaunchedEffect
        kotlinx.coroutines.delay(900)
        repeat(2) { if (turn.value == 0f) { turn.animateTo(-0.16f, tween(380)); turn.animateTo(0f, tween(420)) } }
    }

    CompositionLocalProvider(LocalReadScale provides scale, LocalPageTurn provides fwd) {
      Column(Modifier.fillMaxSize().background(Ds.page)) {
        SlimHead(vm, pages[index]) { talk = pages[index] }
        Box(
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
                // 👆👆 double-tap on th paper (margins, head, folio) = turn · on a passage VerseView routes its double-tap here too
                .pointerInput(Unit) { detectTapGestures(onDoubleTap = { fwd() }) },
        ) {
            val t = turn.value
            val cam = 18 * density.density
            // under: th page revealed (fwd: nxt page · back: current) · shadow fades as th turning page leaves
            val under = if (t < 0) index + 1 else index
            val over = if (t < 0) index else if (t > 0) index - 1 else -1
            key(under) { Page(vm, pages[under], speaker, under, pages.size, Modifier.fillMaxSize(), flashId) }
            // fwd: covered at th start (dark) ➡ revealed (light) · back: gets covered (light ➡ dark)
            if (t != 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f * if (t < 0) 1f + t else t)))
            if (over >= 0) key(over) {
                // turning page: hinged at th left edge (spine) · fwd: 0 ➡ -90° · back: -90° ➡ 0
                val angle = if (t < 0) 90f * t else -90f * (1f - t)
                Page(vm, pages[over], speaker, over, pages.size, Modifier.fillMaxSize().graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = angle
                    cameraDistance = cam
                }, null)
            }
            // 📐 dog-ear bottom-right = "this is a page, it turns" · tap = turn (indicator + 2nd way 2 do it)
            if (index < pages.lastIndex && t == 0f) DogEar(Modifier.align(Alignment.BottomEnd), fwd)
            // 💬 hint pill (only while th peek is still on, see TURN_HINT · 6 s, then out o th way) · paperHi + line, label-m
            if (hintOn && index < pages.lastIndex) Text(
                tr("⬅ wischen oder doppelt tippen = umblättern", "⬅ swipe or double-tap = turn the page"),
                style = MaterialTheme.typography.labelMedium, color = Ds.ink,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Space.l)
                    .background(Ds.paperHi, CircleShape).border(1.dp, Ds.line, CircleShape)
                    .padding(horizontal = Space.l, vertical = Space.s))
        }
      }
    }
}

/**
 * D-031 b: slim paper head = back · chapter title (serif) · CountChip "💬 N" (oth ppl's notes on this page ➡ sheet)
 * page colour, 1dp line under it · th app's top bar + bottom nav r hidden on this route (MainActivity, L5)
 */
@Composable
private fun SlimHead(vm: AppVm, p: BookPage, onTalk: () -> Unit) {
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

/** turns u do urself b4 th peek + hint pill stop */
private const val TURN_HINT = 3

/** 👉 "turn forward" fr inside a page (VerseView double-tap) · null = not i th book (Today plan verses) */
val LocalPageTurn = compositionLocalOf<(() -> Unit)?> { null }

/** 📐 folded page corner: desk shows thru th cut, th flap lies on th page w a soft shadow */
@Composable
private fun DogEar(modifier: Modifier, onClick: () -> Unit) {
    val desc = tr("Umblättern", "Turn the page")
    // 48 target (§ 9) · th drawn corner stays 36 at th very corner
    Box(modifier.size(Space.tap).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc },
        contentAlignment = Alignment.BottomEnd) {
        Canvas(Modifier.size(36.dp)) {
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
private fun Page(vm: AppVm, p: BookPage, speaker: Speaker, i: Int, n: Int, modifier: Modifier, flashId: String?) {
    val chapter = vm.content.chapters.firstOrNull { it.kap == p.kap }
    val reads by vm.pageReads.collectAsStateWithLifecycle()
    val firstOfChapter = p.verses.first().let { f -> vm.content.chapter(p.kap).firstOrNull()?.id == f.id }
    Box(modifier.background(Ds.page).background(
        // spine shadow left + page-edge darkening right = it looks bound
        Brush.horizontalGradient(0f to Color.Black.copy(alpha = .2f), 0.06f to Color.Transparent, 0.94f to Color.Transparent,
            1f to Color.Black.copy(alpha = .13f)))) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = Space.readerStart, end = Space.readerEnd, top = Space.m, bottom = Space.l)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text((chapter?.let { chapterTitle(it) } ?: p.kap).uppercase(), style = OmmType.bookMeta, color = Ds.inkMuted,
                    maxLines = 1, modifier = Modifier.weight(1f))
                // 👥 Mitlesen: how often this page was read (social proof, opt-in) · muted, red = act only
                p.page?.let { pg -> reads[pg]?.let { Text("👥 %d×  ·  ".f(it), style = OmmType.bookMeta, color = Ds.inkMuted) } }
                p.page?.let { Text(tr("MEW S. %d", "MEW p. %d").f(it), style = OmmType.bookMeta, color = Ds.inkMuted) }
            }
            HorizontalDivider(color = Ds.line, modifier = Modifier.padding(vertical = Space.s))
            if (firstOfChapter) Text(chapter?.let { chapterTitle(it) } ?: p.kap, style = OmmType.bookTitle, color = Ds.ink,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = Space.l))
            p.verses.forEachIndexed { k, v ->
                val prev = p.verses.getOrNull(k - 1)
                if (v.secTitle.isNotBlank() && v.sec != prev?.sec) {
                    if (prev != null) Ornament()
                    SectionTitle(secTitle(v))
                }
                VerseView(vm, v, speaker, flash = v.id == flashId, dropCap = firstOfChapter && k == 0)
                Gap(4)
            }
            // QA r4: no share pills (r3) + no "Mitreden" btn down here = pure Marx page · writing = tap a passage ➡ ✍️ Notiz
            //   (👥 chip there posts it 2 th Lesesaal) · reading oth's = 💬 N i th slim head (D-031 b)
            Gap(20)
            Text("— ${p.page ?: (i + 1)} —", style = OmmType.bookMeta.copy(fontFamily = FontFamily.Serif), color = Ds.inkMuted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (i == 0 || i == n - 1) Text(
                tr("⬅ wischen oder doppelt tippen = umblättern · 🤏 zwei Finger = Schrift", "⬅ swipe or double-tap = turn the page · 🤏 two fingers = font"),
                style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = Space.s))
        }
    }
}

/** ❦ betw sections i th book · 30 % ink, decor only */
@Composable
private fun Ornament() = Text("❦", style = OmmType.bookHead, color = Ds.ink.copy(alpha = .3f), textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().padding(top = Space.l))
