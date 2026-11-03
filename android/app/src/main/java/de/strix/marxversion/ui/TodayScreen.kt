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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import de.strix.marxversion.logic.dailyPool
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
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

@OptIn(ExperimentalMaterial3Api::class)
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
    var celebrate by remember { mutableStateOf(false) }
    var rankUp by remember { mutableStateOf<Int?>(null) }
    val persona = Persona.of(s.persona)
    val doneToday = days.any { it.date == today() }
    // 🃏 cards: 🔮 pull down = oracle card · 📳 shake = crisis card (+ red flash) — both land i front i th carousel
    val extras by vm.extraCards.collectAsStateWithLifecycle()
    val jump by vm.homeJump.collectAsStateWithLifecycle()
    var kriseFx by remember { mutableStateOf(false) }
    var swoosh by remember { mutableStateOf(false) }
    var confetti by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val roast: () -> Unit = {
        vm.content.verses.filter { it.polemic && it.quoteDe.isNotBlank() }.randomOrNull()?.let {
            vm.addCard(CardItem(it, CardItem.Kind.KRISE)); kriseFx = true; scope.launch { scroll.animateScrollTo(0) }
        }
    }
    val oracleCard: (Verse?) -> Unit = { v -> v?.let { vm.addCard(CardItem(it, CardItem.Kind.ORAKEL)); scope.launch { scroll.animateScrollTo(0) } } }
    val oracleNow: () -> Unit = { oracleCard(dailyPool(vm.content.verses).randomOrNull()) }
    ShakeDetector { roast() }
    // Amen on today's card: 📳 thump + toast · ☭ swoosh · full Stempelkarte ➡ 🎉 confetti
    val total = readingDays(days)
    val fills = !doneToday && cardJustFilled(total + 1)
    val amenMsg = if (fills) tr("🏅 Stempelkarte voll! Neuer Rang: %s", "🏅 Stamp card full! New rank: %s").f(tr(rank(cardsDone(total + 1) - 1)))
        else tr("☭ Lesetag gezählt — 🔥 %d", "☭ Reading day counted — 🔥 %d").f(streak(days) + 1)
    val amen: (Verse) -> Unit = { v ->
        Toast.makeText(ctx, amenMsg, Toast.LENGTH_LONG).show()
        buzz(ctx, longArrayOf(0, 40, 60, 90))
        if (fills) confetti = true else swoosh = true
        vm.amen(v.id)
    }
    // 🚀 launcher shortcut "Orakel" / "Krise!"
    val req by vm.popRequest.collectAsStateWithLifecycle()
    LaunchedEffect(req) {
        when (req) { "oracle" -> oracleNow(); "krise" -> roast() }
        if (req != null) vm.popRequest.value = null
    }

    PullToRefreshBox(isRefreshing = false, onRefresh = { oracleNow() }) {
    Column(Modifier.fillMaxWidth().verticalScroll(scroll).padding(vertical = 12.dp)) {
        // ── 🃏 th cards: 1st thing u see (Hooked: reward right at th door) ──
        CardCarousel(vm, extras, vod?.let { CardItem(it, CardItem.Kind.TODAY, 0, feast) }, doneToday, speaker, persona, jump, openChapter, amen)
        Column(Modifier.padding(horizontal = 16.dp)) {
            // 👥 Mitlesen: today's crowd (only if switched on + server reachable)
            val crowd by vm.crowd.collectAsStateWithLifecycle()
            LaunchedEffect(s.community) { vm.refreshCrowd() }
            crowd?.let { c ->
                Text(tr("👥 Heute lesen %d mit · %d × Amen ☭", "👥 %d reading along today · %d × Amen ☭").f(c.readersToday, c.amenToday),
                    fontSize = 12.sp, color = Red, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                HookNote(Hook.SOCIAL, s.testMode)
            }
            HookNote(Hook.POPUP, s.testMode)
            HookNote(Hook.TINY, s.testMode)
            HookNote(Hook.VARIABLE, s.testMode)
            HookNote(Hook.SHARE, s.testMode)
        }
        Gap(16)
    Column(Modifier.padding(horizontal = 16.dp)) {
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
                    vm.dayDone(plan.id, idx, day.verseIds); celebrate = true; swoosh = true
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
                // answer = a card, flies 2 th front o th deck up top
                FilterChip(selected = false, onClick = { oracleCard(marxoskop(vm.content.verses, k)) }, label = { Text(tr(NERVT_LABEL.getValue(k))) })
            }
        }
        HookNote(Hook.FORER, s.testMode)
        Text(tr("⬇ Seite runterziehen = Zufalls-Orakel · 📳 Handy schütteln = Krise!", "⬇ pull the page down = random oracle · 📳 shake the phone = crisis!"),
            fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
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
            if (rankUp != null) Confetti {}  // 🎉 full card = paper rain on top
        }
    }
    }  // inner padded Column
    }  // PullToRefreshBox

    if (kriseFx) KriseFlash { kriseFx = false }
    if (swoosh) SickleSwoosh { swoosh = false }
    if (confetti) Confetti { confetti = false }
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
    full: Boolean = false,
    // 📱 stories (full only): tap left/right half o th card = older/newer day · null = no such day
    onOlder: (() -> Unit)? = null, onNewer: (() -> Unit)? = null, storyLabel: String? = null,
    shareOnly: Boolean = false,  // fr a verse's Teilen: just 1 big Share btn + ☭ close
) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val swipeMin = with(LocalDensity.current) { 90.dp.toPx() }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl  // Arabic: "older" = right half
    val lang = LocalLang.current
    val speaker = rememberSpeaker()
    val q = verseQuote(v).ifBlank { verseText(v).take(240).substringBeforeLast(' ') + " …" }
    val r = ref(v)
    val bmp = remember(q, r, lang, feast) { cardBitmap(ctx, q, r, lang, feast).asImageBitmap() }
    val shareTitle = tr("Spruchkarte teilen", "Share verse card")
    val persona = Persona.of(s.persona)
    LaunchedEffect(q, lang, persona) { speaker.prerender(q, lang, persona) }  // 🔊 tap = instant
    val buttons: @Composable (Color) -> Unit = { fg ->
        if (shareOnly) Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { shareCard(ctx, bmp.asAndroidBitmap(), "${lang.quote(q)} — Karl Marx, $r\n$APP_NAME", shareTitle) },
                modifier = Modifier.weight(1f)) {
                Icon(painterResource(R.drawable.ic_share), null, Modifier.size(20.dp)); Text("  " + tr("Teilen", "Share"), fontSize = 17.sp)
            }
            TextButton(onClick = onDismiss) { Text("☭", fontSize = 24.sp, color = Red) }
        } else
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { shareCard(ctx, bmp.asAndroidBitmap(), "${lang.quote(q)} — Karl Marx, $r\n$APP_NAME", shareTitle) }) {
                Icon(painterResource(R.drawable.ic_share), null, Modifier.size(18.dp), tint = fg); Text(" " + tr("Teilen", "Share"), color = fg)
            }
            TextButton(onClick = { speaker.say(q, lang, persona) }) {
                Icon(painterResource(R.drawable.ic_volume), null, Modifier.size(22.dp), tint = if (speaker.speaking) Red else fg)
            }
            CopyIcon("${lang.quote(q)}\n— Karl Marx, $r", fg)
            if (openChapter != null) TextButton(onClick = { openChapter(v.kap, v.id) }) { Text("📖") }
            if (onAmen != null) Button(onClick = onAmen) { Text(tr("Amen ☭", "Amen ☭")) }
            else TextButton(onClick = onDismiss) { Text("☭", fontSize = 22.sp, color = if (fg == Color.White) fg else Red) }
        }
    }
    if (full) {
        // 🌅 daily Losung = th whole screen: card blurred as backdrop + card i front (Hooked: reward right at th door)
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            // swipe ⬆ = Amen · swipe ⬇ = close (whole screen)
            Box(Modifier.fillMaxSize().pointerInput(onAmen) {
                var dy = 0f
                detectVerticalDragGestures(
                    onDragStart = { dy = 0f },
                    onVerticalDrag = { ch, d -> dy += d; ch.consume() },
                    onDragEnd = {
                        when {
                            dy < -swipeMin && onAmen != null -> { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onAmen() }
                            dy > swipeMin -> onDismiss()
                        }
                    },
                )
            }) {
                Image(bmp, null, Modifier.fillMaxSize().blur(40.dp), contentScale = ContentScale.Crop)  // blur = API 31+, older: just darker
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))
                Column(
                    Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    storyLabel?.let { Text(it, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, textAlign = TextAlign.Center) }
                    var held by remember { mutableStateOf(false) }
                    Image(bmp, contentDescription = q, modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 1350f)
                        .shadow(18.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
                        // stories: tap ⬅ half = older day · ➡ half = newer · hold = pause th voice
                        .pointerInput(onOlder, onNewer) {
                            detectTapGestures(
                                onPress = { tryAwaitRelease(); if (held) { held = false; speaker.resume() } },
                                onLongPress = { held = true; speaker.pause() },
                                onTap = { p ->
                                                    val older = (p.x < size.width / 2) != rtl
                                    (if (older) onOlder else onNewer)?.invoke()
                                },
                            )
                        })
                    hooks()
                    buttons(Color.White)
                }
            }
        }
        return
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(Modifier.fillMaxWidth().padding(20.dp)) {
            Column(Modifier.padding(12.dp)) {
                Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Gap(8)
                Image(bmp, contentDescription = q, modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 1350f).clip(RoundedCornerShape(12.dp)))
                hooks()
                buttons(Blue)
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
    // 📱 stories: 0 = today · n = n days ago (max 2 weeks back)
    var back by remember { mutableIntStateOf(0) }
    val day = LocalDate.now().minusDays(back.toLong())
    val feast = if (back == 0) feastNow(vm) else Feast.on(day)
    val v = remember(today(), reroll, feast, back) { if (back == 0) vm.verseOfDay(reroll, feast) else vm.losungOn(day) } ?: return
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val total = readingDays(days)
    val counted = days.any { it.date == today() }
    val fills = !counted && cardJustFilled(total + 1)
    var confetti by remember { mutableStateOf(false) }
    val msg = if (fills)
        tr("🏅 Stempelkarte voll! Neuer Rang: %s", "🏅 Stamp card full! New rank: %s").f(tr(rank(cardsDone(total + 1) - 1)))
    else tr("☭ Lesetag gezählt — 🔥 %d", "☭ Reading day counted — 🔥 %d").f(streak(days) + if (counted) 0 else 1)
    val ago = when (back) {
        0 -> tr("heute", "today")
        1 -> tr("gestern", "yesterday")
        else -> tr("vor %d Tagen", "%d days ago").f(back)
    }
    CardDialog(
        vm, v,
        title = when {
            back > 0 -> tr("📜 Losung von %s", "📜 Verse from %s").f(ago)
            feast != null -> feast.emoji + " " + tr(feast.greet)
            else -> tr("✨ Deine Marx-Losung für heute", "✨ Your Marx verse for today")
        },
        feast = feast,
        onDismiss = { vm.popupDone() },
        openChapter = { k, id -> vm.popupDone(); openChapter(k, id) },
        onAmen = {
            // 📳 a stamp thump · full card ➡ 🎉 confetti, then close
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
            if (fills) confetti = true else vm.amen(v.id)
        },
        hooks = {
            HookNote(Hook.POPUP, s.testMode)
            HookNote(Hook.TINY, s.testMode)
            if (feast != null) HookNote(Hook.FEAST, s.testMode)
        },
        full = true,
        onOlder = if (back < 14) ({ back++ }) else null,
        onNewer = if (back > 0) ({ back-- }) else null,
        storyLabel = "$ago  ·  " + tr("Karte links/rechts tippen = älter/neuer · ⬆ wischen = Amen · ⬇ = zu",
            "tap card left/right = older/newer · swipe ⬆ = Amen · ⬇ = close"),
    )
    if (confetti) Confetti { confetti = false; vm.amen(v.id) }
}
