package de.strix.marxversion.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.DisposableEffect
import kotlinx.coroutines.delay
import de.strix.marxversion.data.f
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.R
import de.strix.marxversion.ui.ds.ActStyle
import de.strix.marxversion.ui.ds.ActionItem
import de.strix.marxversion.data.Chapter
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Plan
import de.strix.marxversion.data.PlanDay
import de.strix.marxversion.data.Verse

// ── content in th current language: DE/EN fr th book, oth langs fr th i18n table, EN as fallback ──

/** "Kapital 8,9 · S. 247" — bible-style citation + MEW page (find it i th paper copy) */
@Composable
fun ref(v: Verse): String {
    val book = tr("Kapital", "Capital")
    val page = v.page?.let { " · " + tr("MEW S.", "MEW p.") + " " + it } ?: ""
    return "$book ${v.id}$page"
}

/** th lang th paragraph is actually shown in · IT/ES/PT/ZH/AR: own LLM translation (full_xx.json, @1106)
 *  ➡ EN while loading o if missing ➡ DE if no EN paragraph either */
@Composable
fun verseLang(v: Verse): Lang {
    val lang = LocalLang.current
    return when {
        lang == Lang.DE -> Lang.DE
        lang != Lang.EN && LocalFull.current?.v?.containsKey(v.id) == true -> lang
        v.en.isNotBlank() -> Lang.EN
        else -> Lang.DE
    }
}

/** full paragraph i th cur lang (see verseLang) */
@Composable
fun verseText(v: Verse): String = when (verseLang(v)) {
    Lang.DE -> v.de
    Lang.EN -> v.en.replace(" ¶ ", "\n\n")
    else -> LocalFull.current?.v?.get(v.id) ?: v.de
}

/** th LLM-picked line · ~1/5 o all paragraphs have none (dry tables, short bridges — eg 355 i th Jahresplan) ➡ opening sentence */
@Composable
fun verseQuote(v: Verse): String {
    // 🐛 QA r4 (shots): no EN quote ➡ was th DE quote on an EN card · now: opening line o th paragraph i th shown lang; DE only if no EN paragraph
    val en = v.quoteEn.ifBlank { if (v.en.isBlank()) v.quoteDe else "" }
    return when (LocalLang.current) {
        Lang.DE -> v.quoteDe
        Lang.EN -> en
        else -> LocalTr.current?.q?.get(v.id) ?: en
    }.ifBlank { opening(verseText(v)) }
}

/** 1st sentence (≥ 40 chars so "Ad 1." isn't it), max ~220 chars, footnote marks gone */
fun opening(t: String): String {
    val s = t.replace(Regex("\\s*\\[\\d+[a-z]?]"), "").replace(Regex("\\s+"), " ").trim()
    val end = Regex("[.!?;:](\\s|$)").findAll(s).map { it.range.first + 1 }.firstOrNull { it >= 40 } ?: s.length
    return if (end <= 220) s.take(end) else s.take(220).substringBeforeLast(' ') + " …"
}

/**
 * big round #3: Today/feed show a passage as a short snippet + "weiterlesen" (was: 2-3 k char walls) · cut at th last
 * sentence end ≤ max (≥ max/2), else at a word · null = short enough, show it all (≤ max × 1.3: no "more" 4 3 words)
 */
fun snippet(t: String, max: Int = SNIPPET): String? {
    if (t.length <= max * 13 / 10) return null
    val head = t.take(max)
    val end = Regex("[.!?;:][\"“”»«)]?(\\s|$)").findAll(head).map { it.range.first + it.value.trimEnd().length }.lastOrNull { it >= max / 2 }
    return if (end != null) head.take(end).trimEnd() else head.substringBeforeLast(' ').trimEnd(',', ';', ' ') + " …"
}
const val SNIPPET = 320

/** ~reading minutes 4 th "weiterlesen" label (200 words/min, zh: 400 chars/min) */
fun readMinutes(t: String) = maxOf(1, if (t.count { it == ' ' } < t.length / 20) t.length / 400 else (t.count { it == ' ' } + 1) / 200)

@Composable
fun bridge(d: PlanDay): String = when (LocalLang.current) {
    Lang.DE -> d.bridgeDe
    else -> d.bridgeEn   // oth langs: EN til translate.py learns xd/xe (fan-in pass)
}

@Composable
fun andacht(planId: String, idx: Int, d: PlanDay): String = when (LocalLang.current) {
    Lang.DE -> d.andachtDe
    Lang.EN -> d.andachtEn
    else -> LocalTr.current?.a?.get("$planId:$idx") ?: d.andachtEn
}

@Composable
fun planTitle(p: Plan) = when (LocalLang.current) {
    Lang.DE -> p.titleDe
    Lang.EN -> p.titleEn
    else -> LocalTr.current?.plan?.get(p.id)?.getOrNull(0)?.ifBlank { null } ?: p.titleEn
}

@Composable
fun planDesc(p: Plan) = when (LocalLang.current) {
    Lang.DE -> p.descDe
    Lang.EN -> p.descEn
    else -> LocalTr.current?.plan?.get(p.id)?.getOrNull(1)?.ifBlank { null } ?: p.descEn
}

/** section heading: DE ➡ MEW · evy oth lang ➡ th EN (Moore/Aveling) heading · DE only if build.py found none */
@Composable
fun secTitle(v: Verse) = if (LocalLang.current == Lang.DE) v.secTitle else v.secTitleEn.ifBlank { v.secTitle }

@Composable
fun chapterTitle(c: Chapter) = when (LocalLang.current) {
    Lang.DE -> c.de
    Lang.EN -> c.en
    else -> LocalTr.current?.chap?.get(c.kap) ?: c.en
}

/**
 * ✍️ open note editor ⬄ th root (MainActivity) tap watcher · Compose doesn't blur a text field on a tap elsewhere.
 * QA r5: a tap ANYWHERE while th note is open (outside = "tap away", on th field = "tap it again") does exactly 1 thing:
 * close th editor (save if changed) · th tap is eaten ➡ it can't also toggle th action btns below o open sth else.
 * `keep` = th 👥 chip: taps there pass thru (it's part o th editor) · long-press on th field = cursor/paste, passes thru.
 */
object NoteFocus { var bounds: Rect? = null; var keep: Rect? = null }

/** root modifier: short tap (not a drag, not a hold) while a note is open ➡ clear focus (= commit) + consume th tap */
fun Modifier.tapOutsideNote(focus: FocusManager, slop: Float): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        if (NoteFocus.bounds == null) return@awaitEachGesture
        if (NoteFocus.keep?.contains(down.position) == true) return@awaitEachGesture
        var moved = 0f
        var ev: androidx.compose.ui.input.pointer.PointerEvent
        do {
            ev = awaitPointerEvent(PointerEventPass.Initial)
            moved = maxOf(moved, (ev.changes.first().position - down.position).getDistance())
        } while (ev.changes.any { it.pressed })
        val held = ev.changes.first().uptimeMillis - down.uptimeMillis
        if (moved < slop && held < viewConfiguration.longPressTimeoutMillis) {
            ev.changes.forEach { it.consume() }   // th up is gone ➡ no click underneath
            focus.clearFocus()
        }
    }
}

/** 🗒️ "this is MY note, not Marx" at a glance (QA r5): comment-style — side ruler · note icon · tinted bg · sans font */
@Composable
private fun NoteBubble(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(modifier.fillMaxWidth().padding(top = 6.dp).height(androidx.compose.foundation.layout.IntrinsicSize.Min)
        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)).background(Blue.copy(alpha = 0.09f))) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(Blue.copy(alpha = 0.75f)))
        Icon(painterResource(R.drawable.ic_note), null, Modifier.padding(start = 8.dp, top = 9.dp).size(15.dp), tint = Blue)
        Box(Modifier.weight(1f).padding(start = 8.dp, end = 10.dp, top = 7.dp, bottom = 8.dp)) { content() }
    }
}

/**
 * 1 Vers. Tap it ➡ action bar (like th bible app): listen · bookmark · note · copy (QA r4: 🔊 left like evywhere, no share i th reader).
 * ✍️ Notiz = th 1 way 2 write on a passage: private by dflt · 👥 chip = also post it 2 th page's Lesesaal (Mitlesen on)
 * Footnote refs [52] r clickable ➡ dialog w th note (Marx' footnotes = where th roasts live).
 * flash = "Im Buch" landed here ➡ scroll into view + pulse 2× so th eye finds it (QA r4)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseView(vm: AppVm, v: Verse, speaker: Speaker? = null, showActions: Boolean = true, flash: Boolean = false, preview: Boolean = false) {
    val mark by vm.mark(v.id).collectAsStateWithLifecycle(null)
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var footnote by remember { mutableStateOf<String?>(null) }
    var editNote by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf(false) }
    val lang = LocalLang.current
    val glow = remember { Animatable(0f) }
    val into = remember { BringIntoViewRequester() }
    LaunchedEffect(flash) {
        if (!flash) return@LaunchedEffect
        delay(300)  // page laid out 1st
        into.bringIntoView(Rect(0f, 0f, 1f, 1e5f))   // taller than th page ➡ scroll aligns th passage's TOP 2 th top (dflt = just peek in at th bottom)
        repeat(2) { glow.animateTo(1f, tween(320)); glow.animateTo(0.3f, tween(320)) }
        glow.animateTo(0f, tween(1200))
    }
    val fullTxt = verseText(v)
    // preview (Today plan + feed, big round #3): snippet + "weiterlesen" · 🔊 still reads th whole passage
    var expanded by remember(v.id) { mutableStateOf(false) }
    val cut = if (preview && !expanded) snippet(fullTxt) else null
    val txt = cut ?: fullTxt
    val r = ref(v)
    val textLang = verseLang(v)   // 🔊 voice reads th lang th text is in
    val fallbackNote = when {
        textLang == lang -> ""
        lang == Lang.EN -> tr("(hier kein englischer Absatz — deutsches Original:)", "(no English paragraph here — German original:)")
        textLang == Lang.EN -> tr("(dieser Absatz noch nicht übersetzt — hier: Englisch)", "(this paragraph isn't translated yet — showing English)")
        else -> tr("(dieser Absatz noch nicht übersetzt — hier: Deutsch)", "(this paragraph isn't translated yet — showing German)")
    }
    val copied = tr("📋 kopiert", "📋 copied")

    // ✨ anything th usr did (🔖 · ✍️ · old highlight) ➡ gold tint + gold frame = "this one is mine" at a glance
    val marked = mark?.bookmark == true || mark?.note != null || mark?.highlight != null
    val bg = if (marked) Gold.copy(alpha = 0.16f) else Color.Transparent
    val frame = when {
        glow.value > 0f -> Modifier.border(2.dp, Red.copy(alpha = glow.value), RoundedCornerShape(6.dp))
        open -> Modifier.border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
        marked -> Modifier.border(1.5.dp, Gold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
        else -> Modifier
    }
    val scale = LocalReadScale.current
    val haptic = LocalHapticFeedback.current
    // QA r5: double-tap = turn th page (i th book) · was 🔖 + ☭ burst · outside th book (Today) no double-tap ➡ tap reacts at once
    val turnPage = LocalPageTurn.current
    Box {
    Column(
        Modifier.fillMaxWidth().bringIntoViewRequester(into).clip(RoundedCornerShape(6.dp)).background(bg)
            .background(Gold.copy(alpha = 0.45f * glow.value)).then(frame)
            .then(if (showActions) Modifier.combinedClickable(
                onClick = { open = !open },
                onDoubleClick = turnPage,
                onLongClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); open = true },
            ) else Modifier)
            .padding(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(r, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (mark?.bookmark == true) Icon(painterResource(R.drawable.ic_bookmark), null, Modifier.size(14.dp), tint = Red)
        }
        if (fallbackNote.isNotBlank()) Text(fallbackNote, fontSize = 11.sp, color = Color.Gray)
        val annotated = buildAnnotatedString {
            var last = 0
            Regex("\\[(\\d+[a-z]?)]").findAll(txt).forEach { m ->
                append(txt.substring(last, m.range.first))
                val n = m.groupValues[1]
                withLink(LinkAnnotation.Clickable("fn$n", TextLinkStyles(SpanStyle(color = Red, fontWeight = FontWeight.Bold))) {
                    footnote = n
                }) { append("[$n]") }
                last = m.range.last + 1
            }
            append(txt.substring(last))
        }
        Text(annotated, fontFamily = FontFamily.Serif, fontSize = 16.sp * scale, lineHeight = 23.sp * scale)
        if (cut != null) Text(tr("weiterlesen ▾ · noch ~%d Min.", "read more ▾ · ~%d min left").f(readMinutes(fullTxt.drop(cut.length))),
            fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Red,
            modifier = Modifier.padding(top = 4.dp).clip(RoundedCornerShape(50)).clickable { expanded = true }
                .background(Red.copy(alpha = 0.08f)).padding(horizontal = 10.dp, vertical = 4.dp))
        // ✍️ margin note: shown inline as a NoteBubble · tap it (or Notiz) = edit right here, same bubble, no popup
        // QA r4: no Save/Cancel words · QA r5: no ★ either · any tap (away o on th field) = close · changed ➡ saved · blank ➡ note gone
        if (editNote) {
            var draft by remember { mutableStateOf(mark?.note ?: "") }
            var public by remember { mutableStateOf(false) }
            var hadFocus by remember { mutableStateOf(false) }
            var closed by remember { mutableStateOf(false) }
            val focus = remember { FocusRequester() }
            LaunchedEffect(Unit) { focus.requestFocus() }
            DisposableEffect(Unit) { onDispose { NoteFocus.bounds = null; NoteFocus.keep = null } }
            val pubName = vm.mxName.collectAsStateWithLifecycle().value ?: vm.suggestedName
            val commit = {
                if (!closed) {
                    closed = true
                    val t = draft.trim()
                    if (t != (mark?.note ?: "")) vm.note(v.id, t)   // blank ➡ vm drops th note
                    if (public && t.isNotBlank()) vm.postComment(v.kap, v.page, "¶ ${v.id} — $t", pubName) {}
                    editNote = false
                }
            }
            Column(Modifier.fillMaxWidth().onGloballyPositioned { NoteFocus.bounds = it.boundsInRoot() }) {
                NoteBubble {
                    val style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface)
                    androidx.compose.foundation.text.BasicTextField(draft, { draft = it }, textStyle = style, minLines = 2,
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Blue),
                        modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged {
                            if (it.isFocused) hadFocus = true else if (hadFocus) commit()
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        decorationBox = { inner ->
                            if (draft.isEmpty()) Text(tr("Deine Randnotiz …", "Your margin note …"), style = style.copy(color = Color.Gray))
                            inner()
                        })
                }
                // 👥 same note, also public on this page (Mitlesen) = th old "Mitreden" btn, now at th passage (QA r4: 1 pattern)
                if (settings.community && vm.commentsAvailable) FilterChip(selected = public, onClick = { public = !public },
                    modifier = Modifier.onGloballyPositioned { NoteFocus.keep = it.boundsInRoot() },
                    label = { Text(tr("👥 auch im Lesesaal posten — als %s", "👥 also post to the reading room — as %s").f(pubName), fontSize = 12.sp) })
            }
        } else mark?.note?.let {
            NoteBubble(if (showActions) Modifier.clickable { editNote = true } else Modifier) {
                Text(it, fontSize = 14.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        if (showActions && open) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                if (speaker != null) Action(R.drawable.ic_volume, if (speaker.speaking) tr("Stopp", "Stop") else tr("Vorlesen", "Listen"),
                    if (speaker.speaking) Red else MaterialTheme.colorScheme.secondary) {
                    speaker.say(txt, textLang, Persona.of(settings.persona))
                }
                Action(if (mark?.bookmark == true) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border,
                    tr("Lesezeichen", "Bookmark"), if (mark?.bookmark == true) Red else MaterialTheme.colorScheme.secondary) { vm.bookmark(v.id) }
                Action(R.drawable.ic_note, tr("Notiz", "Note"), if (mark?.note != null) Red else MaterialTheme.colorScheme.secondary) { editNote = !editNote }
                Action(R.drawable.ic_copy, tr("Kopieren", "Copy")) {
                    copyText(ctx, "${lang.quote(txt)}\n— Karl Marx, $r")
                    android.widget.Toast.makeText(ctx, copied, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
        if (showActions && marked) HookNote(Hook.INVESTMENT, settings.testMode)
    }
    }

    footnote?.let { n ->
        val note = vm.content.note(v.kap, n)
        val own = LocalFull.current?.fn?.get("${v.kap}:$n")
        val body = when {
            note == null -> "?"
            own != null -> own
            lang != Lang.DE && note.en.isNotBlank() -> note.en
            else -> note.de
        }
        MarxDialog({ footnote = null }, title = tr("Fußnote", "Footnote") + " $n") {
            Text(body, fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 23.sp)
        }
    }
}

/** icon + label below ➡ thin wrapper over ds `ActionItem` Bare (L0 @1114: label 10 ➡ 12sp, label-m) */
@Composable
private fun RowScope.Action(icon: Int, label: String, tint: Color = MaterialTheme.colorScheme.secondary, onClick: () -> Unit) =
    ActionItem(icon, label, onClick, Modifier.weight(1f), style = ActStyle.Bare, tint = tint, labelColor = MaterialTheme.colorScheme.secondary)

/** small helper: section heading inside a chapter */
@Composable
fun SectionTitle(text: String) = Text(
    text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
)

@Composable
fun Gap(dp: Int = 12) = androidx.compose.foundation.layout.Spacer(Modifier.padding(top = dp.dp))
