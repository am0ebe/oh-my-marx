package de.strix.marxversion.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.R
import de.strix.marxversion.data.DayLog
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.T
import de.strix.marxversion.data.Verse
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.CARD
import de.strix.marxversion.logic.Cell
import de.strix.marxversion.logic.DayTime
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.NERVT
import de.strix.marxversion.logic.cardJustFilled
import de.strix.marxversion.logic.cardsDone
import de.strix.marxversion.logic.chain
import de.strix.marxversion.logic.marxoskop
import de.strix.marxversion.logic.readingDays
import de.strix.marxversion.logic.stampCard
import de.strix.marxversion.logic.streak
import de.strix.marxversion.logic.streakInfo
import de.strix.marxversion.logic.today
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private val NERVT_LABEL = mapOf(
    "chef" to T("Chef", "Boss"), "ueberstunden" to T("Überstunden", "Overtime"), "lohn" to T("Lohn", "Wages"),
    "miete" to T("Miete", "Rent"), "konsum" to T("Konsum", "Shopping"), "ki" to T("KI & Maschinen", "AI & machines"),
    "krise" to T("Krise", "Crisis"),
)

/** 🎟️ ranks: 1 per full Stempelkarte — th path through Kapital's own history o capitalism */
private val RANKS = listOf(
    T("📗 Ursprüngliche Akkumulation", "📗 Primitive accumulation"),
    T("🔁 Einfache Reproduktion", "🔁 Simple reproduction"),
    T("📈 Erweiterte Reproduktion", "📈 Expanded reproduction"),
    T("🏭 Manufaktur", "🏭 Manufacture"),
    T("⚙️ Große Industrie", "⚙️ Large-scale industry"),
    T("🧔 Klassiker-Status", "🧔 Classic status"),
    T("☭ Expropriation der Expropriateure", "☭ Expropriating the expropriators"),
)
private fun rank(i: Int) = RANKS[i.coerceIn(0, RANKS.lastIndex)]

/** first n words + "…" (zh: no spaces ➡ chars) */
private fun teaser(s: String, lang: Lang, words: Int = 8) =
    if (lang == Lang.ZH) s.take(words * 2) + "…" else s.split(" ").let { w -> w.take(words).joinToString(" ") + if (w.size > words) " …" else "" }

/** 🎉 / 🌅 — real calendar + clock, or th 🪝 preview */
@Composable
private fun feastNow(vm: AppVm): Feast? = vm.feastPreview.collectAsStateWithLifecycle().value ?: Feast.on(LocalDate.now())

@Composable
fun TodayScreen(vm: AppVm, askNotif: () -> Unit, openChapter: (String, String) -> Unit, openPlans: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val reroll by vm.reroll.collectAsStateWithLifecycle()
    val timePrev by vm.timePreview.collectAsStateWithLifecycle()
    val feast = feastNow(vm)
    val dt = timePrev ?: DayTime.at(LocalTime.now().hour)
    val speaker = rememberSpeaker()
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val openHooked = LocalOpenHooked.current
    val vod = remember(today(), reroll, feast) { vm.verseOfDay(reroll, feast) }
    var oracle by remember { mutableStateOf<Verse?>(null) }
    var celebrate by remember { mutableStateOf(false) }
    var rankUp by remember { mutableStateOf<Int?>(null) }
    val persona = Persona.of(s.persona)
    val doneToday = days.any { it.date == today() }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        // ── 🪝 Hooked mode panel: explainer + test buttons ──
        if (s.testMode) {
            val soon = tr("🔔 kommt in ein paar Sekunden", "🔔 arriving in a few seconds")
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1B8)), modifier = Modifier.padding(bottom = 12.dp)) {
                Column(Modifier.padding(10.dp)) {
                    Text(tr("🪝 Hooked-Modus — Werkbank", "🪝 Hooked mode — workbench"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(onClick = openHooked, label = { Text(tr("📖 Hooked erklärt", "📖 Hooked explained")) })
                        AssistChip(onClick = { vm.reroll.value++ }, label = { Text(tr("🎲 Neuer Vers", "🎲 New verse")) })
                        AssistChip(onClick = { askNotif(); vm.testNotifNow(); Toast.makeText(ctx, soon, Toast.LENGTH_SHORT).show() },
                            label = { Text(tr("🔔 Benachrichtigung", "🔔 Notification")) })
                        AssistChip(onClick = { vm.forcePopup.value = true }, label = { Text(tr("🌅 Losung-Popup", "🌅 Verse popup")) })
                        AssistChip(onClick = { celebrate = true }, label = { Text(tr("🎉 Feier-Popup", "🎉 Celebration")) })
                        AssistChip(onClick = { rankUp = 0; celebrate = true }, label = { Text(tr("🏅 Rang-Aufstieg", "🏅 Rank up")) })
                        AssistChip(onClick = { vm.testNextFeast() },
                            label = { Text(tr("🎉 Feiertag: %s", "🎉 Holiday: %s").f(feast?.emoji ?: "—")) })
                        AssistChip(onClick = { vm.testNextTime() }, label = { Text(tr("🌅 Tageszeit", "🌅 Time of day")) })
                        AssistChip(onClick = { vm.testUndoDay() }, label = { Text(tr("↩️ Heute zurücksetzen", "↩️ Reset today")) })
                    }
                }
            }
        }

        Hero(days, feast, dt, s.testMode)
        Gap(16)

        // ── Vers des Tages ──
        vod?.let { v ->
            val q = verseQuote(v)  // read composable values here — onClick lambdas aren't composable
            val r = ref(v)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp)) {
                    Text(tr("✨ Marx-Losung für heute", "✨ Today's Marx verse"), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Gap(6)
                    // tap th quote ➡ th colourful Spruchkarte popup (share from there)
                    Box(Modifier.clickable { vm.forcePopup.value = true }) { QuoteText(quoted(q)) }
                    Gap(6)
                    Text(r, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalArrangement = Arrangement.Center) {
                        TextButton(onClick = { openChapter(v.kap, v.id) }) { Text(tr("📖 Im Kontext", "📖 In context")) }
                        TextButton(onClick = { vm.forcePopup.value = true }) {
                            Icon(painterResource(R.drawable.ic_share), null, Modifier.size(18.dp)); Text(" " + tr("Spruchkarte", "Verse card"))
                        }
                        TextButton(onClick = { speaker.say(q, lang, persona) }) {
                            Icon(painterResource(R.drawable.ic_volume), null, Modifier.size(18.dp), tint = if (speaker.speaking) Red else Blue)
                        }
                        if (!doneToday) Button(onClick = { vm.amen(v.id) }) { Text(tr("✔️ Gelesen", "✔️ Read")) }
                    }
                    HookNote(Hook.TINY, s.testMode)
                    HookNote(Hook.VARIABLE, s.testMode)
                    HookNote(Hook.SHARE, s.testMode)
                }
            }
        }
        Gap(20)

        // ── Lesepläne ──
        val plan = s.activePlan?.let { vm.content.plan(it) }
        var nextTeaser: String? = null
        if (plan == null) {
            OutlinedButton(onClick = openPlans, modifier = Modifier.fillMaxWidth()) {
                Text(tr("📚 Leseplan wählen — Kapital in kleinen Hostien", "📚 Pick a reading plan — Capital in communion-wafer portions"))
            }
        } else {
            val done = days.filter { it.planId == plan.id }
            val planToday = done.firstOrNull { it.date == today() }
            val idx = (planToday?.planDay ?: (done.maxOfOrNull { it.planDay }?.plus(1) ?: 0)).coerceAtMost(plan.days.lastIndex)
            val day = plan.days[idx]
            Text("${plan.emoji} " + planTitle(plan), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(tr("Tag %d von %d", "Day %d of %d").f(idx + 1, plan.days.size), fontSize = 13.sp)

            // Hooked ch7: day-1 missed ➡ suggest a less challenging plan
            val started = s.planStart?.let { LocalDate.parse(it) }
            if (done.isEmpty() && started != null && ChronoUnit.DAYS.between(started, LocalDate.now()) >= 2 && plan.id != "erschoepft") {
                Card(Modifier.padding(vertical = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(tr("Zu viel Mehrarbeit? Versuch doch erst „Marx für Erschöpfte“ 😴",
                            "Too much surplus labour? Try “Marx for the Exhausted” first 😴"))
                        TextButton(onClick = { vm.startPlan("erschoepft") }) { Text(tr("Wechseln", "Switch")) }
                    }
                }
            }
            val ad = andacht(plan.id, idx, day)
            if (ad.isNotBlank()) {
                Gap(8)
                Text(tr("🕯️ Andacht", "🕯️ Devotional"), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                Text(ad, fontSize = 15.sp, lineHeight = 21.sp)
            }
            Gap(8)
            day.verseIds.mapNotNull { vm.content.verse(it) }.forEach { VerseView(vm, it, speaker); Gap(4) }
            Gap()
            // Hooked ch5: investment loads th nxt trigger ➡ tease tomorrow's reading
            nextTeaser = plan.days.getOrNull(idx + 1)?.verseIds?.firstNotNullOfOrNull { vm.content.verse(it) }
                ?.let { teaser(verseQuote(it), lang) }
            if (planToday == null) {
                Button(onClick = {
                    val total = readingDays(days)
                    if (!doneToday && cardJustFilled(total + 1)) rankUp = cardsDone(total + 1) - 1
                    vm.dayDone(plan.id, idx, day.verseIds); celebrate = true
                },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()) { Text(tr("Tag geschafft! ☭", "Day complete! ☭"), fontSize = 18.sp) }
            } else {
                Text(tr("✔️ Heute erledigt. Feierabend.", "✔️ Done for today. Clock out."), color = MaterialTheme.colorScheme.primary)
                nextTeaser?.let { Text(tr("Morgen: %s", "Tomorrow: %s").f(quoted(it)), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary) }
            }
        }
        Gap(24)

        // ── Marxoskop: "Was nervt?" ──
        Text(tr("🔮 Marxoskop — was nervt dich gerade?", "🔮 Marxoscope — what's bugging you?"), fontWeight = FontWeight.Bold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NERVT.keys.forEach { k ->
                FilterChip(selected = false, onClick = { oracle = marxoskop(vm.content.verses, k) }, label = { Text(tr(NERVT_LABEL.getValue(k))) })
            }
        }
        HookNote(Hook.FORER, s.testMode)
        oracle?.let { v ->
            Card(Modifier.padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp)) {
                    QuoteText(quoted(verseQuote(v)), 18)
                    Text(ref(v), fontSize = 12.sp)
                    TextButton(onClick = { openChapter(v.kap, v.id) }) { Text(tr("📖 Im Kontext", "📖 In context")) }
                }
            }
        }
        Gap(40)

        if (celebrate) {
            AlertDialog(
                onDismissRequest = { celebrate = false; rankUp = null },
                confirmButton = { TextButton(onClick = { celebrate = false; rankUp = null }) { Text("☭") } },
                title = { Text(if (rankUp != null) tr("🏅 Stempelkarte voll!", "🏅 Stamp card full!") else tr("Tag geschafft! ☭", "Day complete! ☭")) },
                text = {
                    Column {
                        rankUp?.let {
                            Text(tr("Neuer Rang:", "New rank:"))
                            Text(tr(rank(it)), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Gap(10)
                        }
                        Text(tr("%d Tage in Folge. Akkumuliert, akkumuliert! Das ist Moses und die Propheten! (Kapital 22,56)",
                            "%d days in a row. Accumulate, accumulate! That is Moses and the prophets! (Capital 22,56)").f(streak(days)))
                        nextTeaser?.let {
                            Gap(10)
                            Text(tr("Morgen: %s", "Tomorrow: %s").f(quoted(it)), fontWeight = FontWeight.Bold)
                            if (s.notifOn) Text(tr("Losung kommt um %02d:%02d.", "Verse arrives at %02d:%02d.").f(s.notifHour, s.notifMinute), fontSize = 13.sp)
                        }
                        if (rankUp != null) HookNote(Hook.STAMPS, s.testMode)
                        HookNote(Hook.NEXT, s.testMode)
                    }
                },
            )
        }
    }
}

/**
 * 🌅 hero (à la Insight Timer): gradient by time o day (or 🎉 feast) · greeting · 🔥 streak + chain · 🎟️ Stempelkarte.
 */
@Composable
private fun Hero(days: List<DayLog>, feast: Feast?, dt: DayTime, hooked: Boolean) {
    val white = Color.White
    val (st, rest) = streakInfo(days)
    val total = readingDays(days)
    val card = stampCard(total)
    val ranks = cardsDone(total)
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient((feast?.colors ?: dt.colors).take(3).map { Color(it) }))
            .background(Color.Black.copy(alpha = 0.15f))  // scrim: white txt stays readable on light stops
            .padding(16.dp),
    ) {
        Text(feast?.let { it.emoji + "  " + tr(it.greet) } ?: tr(dt.greet), color = white, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp)
        Gap(8)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🔥 $st", color = white, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Column(Modifier.padding(start = 10.dp)) {
                Text(tr("Tage Lesekapital akkumuliert", "days of reading capital accumulated"), color = white, fontSize = 13.sp)
                if (ranks > 0) Text(tr("Rang: %s", "Rank: %s").f(tr(rank(ranks - 1))), color = white.copy(alpha = 0.85f), fontSize = 12.sp)
            }
        }
        Row {
            chain(days).forEach {
                Text(when (it) { Cell.DONE -> "☭"; Cell.REST -> "🕊"; Cell.EMPTY -> "·" },
                    color = white, fontSize = 16.sp, modifier = Modifier.padding(end = 3.dp))
            }
        }
        if (rest.isNotEmpty()) Text(tr("🕊️ Sabbat-Joker eingelöst — 1 Ruhetag pro Woche bricht die Kette nicht.",
            "🕊️ Sabbath joker used — 1 rest day per week doesn't break the chain."), color = white.copy(alpha = 0.85f), fontSize = 11.sp)
        Gap(10)
        // 🎟️ Stempelkarte: 🎁 gift · ☭ earned · ○ to go
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(CARD) { i ->
                Box(
                    Modifier.size(24.dp).clip(CircleShape)
                        .then(if (i < card.filled) Modifier.background(Gold) else Modifier.border(1.5.dp, white.copy(alpha = 0.7f), CircleShape)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (i < card.gift) Text("🎁", fontSize = 11.sp)
                    else if (i < card.filled) Text("☭", fontSize = 12.sp, color = Red, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(tr("🎟️ Stempelkarte %d · noch %d Lesetage bis „%s“", "🎟️ Stamp card %d · %d more reading days to “%s”")
            .f(card.no, card.left, tr(rank(ranks))), color = white, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }
    HookNote(Hook.STREAK, hooked)
    HookNote(Hook.STAMPS, hooked)
    HookNote(Hook.SABBATH, hooked)
    HookNote(if (feast != null) Hook.FEAST else Hook.DAYTIME, hooked)
}

/**
 * 🌅 Spruchkarte dialog: th card bitmap itself (what u see = what friends get) + share · 🔊 · context · Amen.
 * Used by th daily Losung popup AND by "Teilen" on any verse.
 */
@Composable
fun CardDialog(
    vm: AppVm, v: Verse, title: String, feast: Feast?, onDismiss: () -> Unit,
    openChapter: ((String, String) -> Unit)? = null, onAmen: (() -> Unit)? = null, hooks: @Composable () -> Unit = {},
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val speaker = rememberSpeaker()
    val q = verseQuote(v).ifBlank { verseText(v).take(240).substringBeforeLast(' ') + " …" }
    val r = ref(v)
    val bmp = remember(q, r, lang, feast) { cardBitmap(q, r, lang, feast).asImageBitmap() }
    val shareTitle = tr("Spruchkarte teilen", "Share verse card")
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().padding(20.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Gap(8)
                Image(bmp, contentDescription = q, modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 1350f).clip(RoundedCornerShape(12.dp)))
                hooks()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { shareCard(ctx, bmp.asAndroidBitmap(), "${lang.quote(q)} — Karl Marx, $r\n$APP_NAME", shareTitle) }) {
                        Icon(painterResource(R.drawable.ic_share), null, Modifier.size(18.dp)); Text(" " + tr("Teilen", "Share"))
                    }
                    TextButton(onClick = { speaker.say(q, lang, Persona.of(s.persona)) }) {
                        Icon(painterResource(R.drawable.ic_volume), null, Modifier.size(18.dp), tint = if (speaker.speaking) Red else Blue)
                    }
                    if (openChapter != null) TextButton(onClick = { openChapter(v.kap, v.id) }) { Text("📖") }
                    if (onAmen != null) Button(onClick = onAmen) { Text(tr("Amen ☭", "Amen ☭")) }
                    else TextButton(onClick = onDismiss) { Text("✕") }
                }
            }
        }
    }
}

/** 🌅 Losung popup — 1st open o th day, or tap on th Losung. "Amen" = 1 reading day (tiny habit). */
@Composable
fun LosungPopup(vm: AppVm, openChapter: (String, String) -> Unit) {
    val reroll by vm.reroll.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val days by vm.days.collectAsStateWithLifecycle()
    val feast = feastNow(vm)
    val v = remember(today(), reroll, feast) { vm.verseOfDay(reroll, feast) } ?: return
    val ctx = LocalContext.current
    val total = readingDays(days)
    val counted = days.any { it.date == today() }
    val msg = if (!counted && cardJustFilled(total + 1))
        tr("🏅 Stempelkarte voll! Neuer Rang: %s", "🏅 Stamp card full! New rank: %s").f(tr(rank(cardsDone(total + 1) - 1)))
    else tr("☭ Lesetag gezählt — 🔥 %d", "☭ Reading day counted — 🔥 %d").f(streak(days) + if (counted) 0 else 1)
    CardDialog(
        vm, v,
        title = feast?.let { it.emoji + " " + tr(it.greet) } ?: tr("✨ Deine Marx-Losung für heute", "✨ Your Marx verse for today"),
        feast = feast,
        onDismiss = { vm.popupDone() },
        openChapter = { k, id -> vm.popupDone(); openChapter(k, id) },
        onAmen = { vm.amen(v.id); Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show() },
        hooks = {
            HookNote(Hook.POPUP, s.testMode)
            HookNote(Hook.TINY, s.testMode)
            if (feast != null) HookNote(Hook.FEAST, s.testMode)
        },
    )
}
