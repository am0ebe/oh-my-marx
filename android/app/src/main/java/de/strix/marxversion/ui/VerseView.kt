package de.strix.marxversion.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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

/** full paragraph: only DE + EN exist (1.3 M chars/lang ➡ not machine-translated yet) */
@Composable
fun verseText(v: Verse): String =
    if (LocalLang.current != Lang.DE && v.en.isNotBlank()) v.en.replace(" ¶ ", "\n\n") else v.de

/** th LLM-picked line · ~1/5 o all paragraphs have none (dry tables, short bridges — eg 355 i th Jahresplan) ➡ opening sentence */
@Composable
fun verseQuote(v: Verse): String = when (LocalLang.current) {
    Lang.DE -> v.quoteDe
    Lang.EN -> v.quoteEn.ifBlank { v.quoteDe }
    else -> LocalTr.current?.q?.get(v.id) ?: v.quoteEn.ifBlank { v.quoteDe }
}.ifBlank { opening(verseText(v)) }

/** 1st sentence (≥ 40 chars so "Ad 1." isn't it), max ~220 chars, footnote marks gone */
fun opening(t: String): String {
    val s = t.replace(Regex("\\s*\\[\\d+[a-z]?]"), "").replace(Regex("\\s+"), " ").trim()
    val end = Regex("[.!?;:](\\s|$)").findAll(s).map { it.range.first + 1 }.firstOrNull { it >= 40 } ?: s.length
    return if (end <= 220) s.take(end) else s.take(220).substringBeforeLast(' ') + " …"
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

@Composable
fun chapterTitle(c: Chapter) = when (LocalLang.current) {
    Lang.DE -> c.de
    Lang.EN -> c.en
    else -> LocalTr.current?.chap?.get(c.kap) ?: c.en
}

/**
 * 1 Vers. Tap it ➡ action bar (like th bible app): highlight · bookmark · note · copy · share · listen.
 * Footnote refs [52] r clickable ➡ dialog w th note (Marx' footnotes = where th roasts live).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VerseView(vm: AppVm, v: Verse, speaker: Speaker? = null, showActions: Boolean = true) {
    val mark by vm.mark(v.id).collectAsStateWithLifecycle(null)
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var footnote by remember { mutableStateOf<String?>(null) }
    var editNote by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf(false) }
    var card by remember { mutableStateOf(false) }
    val lang = LocalLang.current
    val txt = verseText(v)
    val r = ref(v)
    val textLang = if (lang == Lang.DE || v.en.isBlank()) Lang.DE else Lang.EN
    val fallbackNote = when {
        lang == Lang.EN && v.en.isBlank() -> tr("(hier kein englischer Absatz — deutsches Original:)", "(no English paragraph here — German original:)")
        lang.ordinal > 1 && v.en.isNotBlank() -> tr("(Volltext nur auf Deutsch/Englisch — hier: Englisch)", "(full text only in German/English — showing English)")
        lang.ordinal > 1 -> tr("(Volltext nur auf Deutsch/Englisch — hier: Deutsch)", "(full text only in German/English — showing German)")
        else -> ""
    }
    val copied = tr("📋 kopiert", "📋 copied")

    // ✨ anything th usr did (🔖 · ✍️ · old highlight) ➡ gold tint + gold frame = "this one is mine" at a glance
    val marked = mark?.bookmark == true || mark?.note != null || mark?.highlight != null
    val bg = if (marked) Gold.copy(alpha = 0.16f) else Color.Transparent
    val frame = when {
        open -> Modifier.border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(6.dp))
        marked -> Modifier.border(1.5.dp, Gold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
        else -> Modifier
    }
    val scale = LocalReadScale.current
    val haptic = LocalHapticFeedback.current
    // ☭ burst on double-tap (Instagram-heart style): 0 ➡ 1 = grow + fade out
    val burst = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Box {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(bg).then(frame)
            .then(if (showActions) Modifier.combinedClickable(
                onClick = { open = !open },
                // double-tap = 🔖 bookmark on/off · on ➡ ☭ burst
                onDoubleClick = {
                    vm.bookmark(v.id)
                    if (mark?.bookmark != true) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { burst.snapTo(0f); burst.animateTo(1f, tween(650)) }
                    }
                },
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
        // ✍️ margin note: shown inline · tap it (or Notiz) = edit right here, no popup
        if (editNote) {
            var draft by remember { mutableStateOf(mark?.note ?: "") }
            val focus = remember { FocusRequester() }
            LaunchedEffect(Unit) { focus.requestFocus() }
            OutlinedTextField(draft, { draft = it }, minLines = 2, modifier = Modifier.fillMaxWidth().padding(top = 6.dp).focusRequester(focus),
                placeholder = { Text(tr("Deine Randnotiz …", "Your margin note …")) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { editNote = false }) { Text("☭", fontSize = 18.sp) }  // ☭ = close (strix: use it more)
                TextButton(onClick = { vm.note(v.id, draft); editNote = false }) { Text(tr("Speichern", "Save")) }
            }
        } else mark?.note?.let {
            Text("✍️ $it", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp).then(if (showActions) Modifier.clickable { editNote = true } else Modifier))
        }

        if (showActions && open) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Action(if (mark?.bookmark == true) R.drawable.ic_bookmark else R.drawable.ic_bookmark_border,
                    tr("Lesezeichen", "Bookmark"), if (mark?.bookmark == true) Red else MaterialTheme.colorScheme.secondary) { vm.bookmark(v.id) }
                Action(R.drawable.ic_note, tr("Notiz", "Note"), if (mark?.note != null) Red else MaterialTheme.colorScheme.secondary) { editNote = !editNote }
                Action(R.drawable.ic_copy, tr("Kopieren", "Copy")) {
                    copyText(ctx, "${lang.quote(txt)}\n— Karl Marx, $r")
                    android.widget.Toast.makeText(ctx, copied, android.widget.Toast.LENGTH_SHORT).show()
                }
                Action(R.drawable.ic_share, tr("Teilen", "Share")) { card = true }  // ➡ colourful card preview 1st
                if (speaker != null) Action(R.drawable.ic_volume, if (speaker.speaking) tr("Stopp", "Stop") else tr("Vorlesen", "Listen"),
                    if (speaker.speaking) Red else MaterialTheme.colorScheme.secondary) {
                    speaker.say(txt, textLang, Persona.of(settings.persona))
                }
            }
        }
        if (showActions && marked) HookNote(Hook.INVESTMENT, settings.testMode)
    }
    if (burst.value < 1f) Text("☭", fontSize = 64.sp, color = Red, modifier = Modifier.align(Alignment.Center).graphicsLayer {
        val t = burst.value
        scaleX = 0.4f + 1.4f * t; scaleY = scaleX; alpha = 1f - t
    })
    }

    // 🌅 Teilen ➡ card preview w just 1 big Share btn (🔊 + 📋 alr sit i th verse's menu)
    if (card) CardDialog(vm, v, tr("🌅 Spruchkarte", "🌅 Verse card"), null, onDismiss = { card = false }, shareOnly = true)

    footnote?.let { n ->
        val note = vm.content.note(v.kap, n)
        val body = when {
            note == null -> "?"
            lang != Lang.DE && note.en.isNotBlank() -> note.en
            else -> note.de
        }
        AlertDialog(
            onDismissRequest = { footnote = null },
            confirmButton = { TextButton(onClick = { footnote = null }) { Text("☭", fontSize = 20.sp) } },
            title = { Text(tr("Fußnote", "Footnote") + " $n") },
            text = { Text(body, fontFamily = FontFamily.Serif) },
        )
    }
}

/** icon + tiny label below — labels = no more guessing what th symbols mean */
@Composable
private fun RowScope.Action(icon: Int, label: String, tint: Color = MaterialTheme.colorScheme.secondary, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick).padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(painterResource(icon), contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 10.sp, maxLines = 1, color = MaterialTheme.colorScheme.secondary)
    }
}

/** small helper: section heading inside a chapter */
@Composable
fun SectionTitle(text: String) = Text(
    text, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary,
    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
)

@Composable
fun Gap(dp: Int = 12) = androidx.compose.foundation.layout.Spacer(Modifier.padding(top = dp.dp))
