package de.strix.marxversion.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import java.time.format.TextStyle
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.height
import de.strix.marxversion.logic.planIndex
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

private const val DEFAULT_PLAN = "30tage"
private val PlanAccent = Color(0xFF6A3093)  // plan card accent (aubergine = Plans-tab 2nd colour)

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
    var checkFx by remember { mutableStateOf(false) }   // "Tag geschafft" = ✔️ pop (≠ ☭, QA @1112)
    var confetti by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val roast: () -> Unit = {
        vm.content.verses.filter { it.polemic && it.quoteDe.isNotBlank() }.randomOrNull()?.let {
            vm.addCard(CardItem(it, CardItem.Kind.KRISE)); kriseFx = true
            if (de.strix.marxversion.logic.Orden.KRISE !in vm.orden.value) vm.flag("krise:" + System.currentTimeMillis())   // 1 flag per shake, stops once earned
 scope.launch { scroll.animateScrollTo(0) }
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
    // 🏠 Home tab tap ➡ scroll 2 top (th carousel jumps 2 card 1 itself)
    LaunchedEffect(jump) { if (jump > 0) scroll.animateScrollTo(0) }
    // 📖 evy1 i a plan: nothing chosen yet (once th prefs r really loaded) ➡ dflt = th flagship
    val prefsReady by vm.prefsReady.collectAsStateWithLifecycle()
    LaunchedEffect(prefsReady, s.activePlan, s.planStart) {
        if (prefsReady && s.activePlan == null && s.planStart == null && vm.content.plan(DEFAULT_PLAN) != null) vm.startPlan(DEFAULT_PLAN)
    }
    // 🚀 launcher shortcut "Orakel" / "Krise!"
    val req by vm.popRequest.collectAsStateWithLifecycle()
    LaunchedEffect(req) {
        when (req) { "oracle" -> oracleNow(); "krise" -> roast() }
        if (req != null) vm.popRequest.value = null
    }

    PullToRefreshBox(isRefreshing = false, onRefresh = { oracleNow() }) {
    Column(Modifier.fillMaxWidth().verticalScroll(scroll).padding(vertical = 12.dp)) {
        // ── 🆕 newer version ➡ tap = UpdateDialog: download + Android's install dialog (QA r5, was: open th web page) ──
        val upd by vm.update.collectAsStateWithLifecycle()
        upd?.let { u ->
            Card(onClick = { vm.openUpdate() },
                colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 10.dp)) {
                Text(tr("🆕 Version %s ist da — antippen = installieren (deine Daten bleiben)", "🆕 Version %s is out — tap = install (your data stays)").f(u.version),
                    fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
        }
        // ── 🔥 streak widget on top (@1112): chain + week + Stempelkarte = th 1st thing u see ──
        Box(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) { Hero(days, feast, dt, s.testMode) }
        // ── 🃏 th cards (Hooked: reward right at th door) ──
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

        Gap(8)

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
            val idxOrNull = planIndex(done, plan.days.size)
            val accent = PlanAccent
            if (idxOrNull == null) {
                // 🏁 finished: nvr show th last page again
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Gold)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("🏁 " + tr("Plan übererfüllt!", "Plan overfulfilled!"), fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = FontFamily.Serif)
                        Text(planTitle(plan), fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
                        Button(onClick = openPlans, modifier = Modifier.fillMaxWidth()) { Text(tr("📚 Nächsten Plan wählen", "📚 Pick the next plan")) }
                    }
                }
            } else {
            val idx = idxOrNull
            val day = plan.days[idx]
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)) {
              Column(Modifier.padding(16.dp)) {
                // header: emoji disc · title · "Tag x von N" chip · progress bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
                        Text(plan.emoji, fontSize = 20.sp)
                    }
                    Column(Modifier.padding(start = 12.dp).weight(1f)) {
                        Text(planTitle(plan), fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif, color = accent)
                        Text(tr("Tag %d von %d", "Day %d of %d").f(idx + 1, plan.days.size), fontSize = 12.sp, color = Color(0xFF6B6275))
                        Text(tr("noch %d Lesetage bis „%s“", "%d more reading days to “%s”").f(stampCard(readingDays(days)).left, tr(rank(cardsDone(readingDays(days))))),
                            fontSize = 12.sp, color = Color(0xFF6B6275))
                    }
                }
                LinearProgressIndicator(progress = { (idx + if (planToday != null) 1 else 0).toFloat() / plan.days.size },
                    color = accent, trackColor = accent.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(4.dp).clip(RoundedCornerShape(50)))
                Gap(8)

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
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.07f)).padding(12.dp)) {
                    Text(tr("🕯️ Andacht", "🕯️ Devotional"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
                    Text(ad, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
                }
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
                    vm.dayDone(plan.id, idx, day.verseIds); celebrate = true; checkFx = true
                },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()) { Text(tr("Tag geschafft! ✔️", "Day complete! ✔️"), fontSize = 18.sp) }
            }
              }  // card Column
            }  // Card
            }  // plan not finished
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
        // (pull/shake hint lives only under th cards: down here "pull down" doesn't work ➡ misleading, rm'd @1104)
        Gap(40)

        if (celebrate) {
            if (rankUp != null) Confetti {}  // 🎉 full card = paper rain, 1st = behind th dialog
            MarxDialog({ celebrate = false; rankUp = null },
                title = if (rankUp != null) tr("🏅 Stempelkarte voll!", "🏅 Stamp card full!") else tr("Tag geschafft!", "Day complete!"),
            ) {
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
            }
        }
    }
    }  // inner padded Column
    }  // PullToRefreshBox

    if (kriseFx) KriseFlash { kriseFx = false }
    if (swoosh) SickleSwoosh { swoosh = false }
    if (checkFx) CheckPop { checkFx = false }
    if (confetti) Confetti { confetti = false }
}

/**
 * 🌅 hero (à la Insight Timer's streak widget, @1106): gradient by time o day (or 🎉 feast) · nudge title
 * ("Save your chain!") · this week Mo–Su (✓ read · red ! = today still open · 🕊 Sabbat) · streak nr i a red star ·
 * 🎟️ Stempelkarte (🎁 gifts · ☭ earned · red ! = today's stamp waiting) + "N more days to <rank>".
 */
@Composable
private fun Hero(days: List<DayLog>, feast: Feast?, dt: DayTime, hooked: Boolean) {
    val white = Color.White
    val (st, rest) = streakInfo(days)
    val total = readingDays(days)
    val card = stampCard(total)
    val ranks = cardsDone(total)
    val today = LocalDate.now()
    val read = days.map { it.date }.toSet()
    val doneToday = today.toString() in read
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient((feast?.colors ?: dt.colors).take(3).map { Color(it) }))
            .background(Color.Black.copy(alpha = 0.15f))  // scrim: white txt stays readable on light stops
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (st > 0) tr("%d Tage Lesekapital akkumuliert", "%d days of reading capital accumulated").f(st)
                    else tr("Schön, dass du da bist", "Good to see you"), color = white.copy(alpha = 0.9f), fontSize = 13.sp)
                Text(when {
                    doneToday -> tr("Schicht erledigt", "Shift done")   // ✓ lives i th week row · "akkumuliert" only once (@1107)
                    st > 0 -> tr("Weiter geht's!", "Keep it rolling!")
                    else -> tr("Fang heute an!", "Start today!")
                }, color = white, fontSize = 24.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif, lineHeight = 28.sp)
                Gap(4)
                Text(feast?.let { it.emoji + "  " + tr(it.greet) } ?: tr(dt.greetOn(today)), color = white.copy(alpha = 0.9f),
                    fontSize = 13.sp, lineHeight = 17.sp)
            }
            StreakStar(st)
        }
        Gap(12)
        // this week, Mo ➡ Su (Insight Timer: ✓ · ! · letters)
        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val lang = LocalLang.current
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (0L..6L).map { monday.plusDays(it) }.forEach { d ->
                val isRead = d.toString() in read
                val (bg, fg, txt) = when {
                    isRead -> Triple(white, Color(0xFF1D1230), "✓")
                    d in rest -> Triple(white.copy(alpha = 0.85f), Color(0xFF1D1230), "🕊")
                    d == today -> Triple(white.copy(alpha = 0.3f), white, d.dayOfWeek.getDisplayName(TextStyle.NARROW, lang.locale))   // open today = letter, no "!"
                    else -> Triple(white.copy(alpha = if (d.isAfter(today)) 0.16f else 0.08f), white.copy(alpha = 0.7f),
                        d.dayOfWeek.getDisplayName(TextStyle.NARROW, lang.locale))
                }
                Box(Modifier.size(34.dp).clip(CircleShape).background(bg)
                    .then(if (d == today) Modifier.border(2.5.dp, white, CircleShape) else Modifier), contentAlignment = Alignment.Center) {   // today = ring
                    Text(txt, color = fg, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (rest.isNotEmpty()) Text(tr("🕊️ Sabbat-Joker eingelöst — 1 Ruhetag pro Woche bricht die Kette nicht.",
            "🕊️ Sabbath joker used — 1 rest day per week doesn't break the chain."), color = white.copy(alpha = 0.85f), fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp))
        Gap(14)
        // 🎟️ Stempelkarte: 🎁 gift · ☭ earned · red ! = today's stamp, still open · ○ to go
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(CARD) { i ->
                val waiting = !doneToday && i == card.filled
                Box(
                    Modifier.size(24.dp).clip(CircleShape)
                        .then(when {
                            i < card.filled -> Modifier.background(Gold)
                            waiting -> Modifier.background(Red)
                            else -> Modifier.border(1.5.dp, white.copy(alpha = 0.7f), CircleShape)
                        }),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        i < card.filled -> Text("☭", fontSize = 12.sp, color = Red, fontWeight = FontWeight.Bold)   // gift stamps = same ☭ (@1112, was 🎁)
                        waiting -> Text("!", fontSize = 13.sp, color = white, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        // "N more days to <rank>" moved 2 th plan card (QA @1112)
        if (ranks > 0) Text(tr("Rang: %s", "Rank: %s").f(tr(rank(ranks - 1))), color = white.copy(alpha = 0.8f), fontSize = 11.sp)
    }
    HookNote(Hook.STREAK, hooked)
    HookNote(Hook.STAMPS, hooked)
    HookNote(Hook.SABBATH, hooked)
    HookNote(if (feast != null) Hook.FEAST else Hook.DAYTIME, hooked)
}

/** streak nr on a red star w gold rim (Insight Timer puts it i a singing bowl · we hv th Soviet star) */
@Composable
private fun StreakStar(n: Int) {
    Box(Modifier.size(92.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center; val ro = size.minDimension / 2f; val ri = ro * 0.42f
            val p = Path().apply {
                for (k in 0 until 10) {
                    val r = if (k % 2 == 0) ro else ri
                    val a = Math.toRadians(-90.0 + k * 36.0)
                    val x = c.x + r * kotlin.math.cos(a).toFloat(); val y = c.y + r * kotlin.math.sin(a).toFloat()
                    if (k == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(p, Red)
            drawPath(p, Gold, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))
        }
        Text("$n", color = Color.White, fontSize = if (n < 100) 26.sp else 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp))
    }
}

// 🌅 CardDialog + LosungPopup (fullscreen daily card · verse-share preview) rm'd @1113 (QA r4): cards live on Home since @1106,
//    verse share gone fr th reader ➡ both were dead · git: d0ea734
