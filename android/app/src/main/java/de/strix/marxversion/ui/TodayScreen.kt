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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.PlanAccents
import de.strix.marxversion.ui.ds.Shapes
import de.strix.marxversion.logic.streakInfo
import de.strix.marxversion.logic.streakGame
import de.strix.marxversion.logic.today
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private val NERVT_LABEL = mapOf(
    "chef" to T("Chef", "Boss"), "ueberstunden" to T("Überstunden", "Overtime"), "lohn" to T("Lohn", "Wages"),
    "miete" to T("Miete", "Rent"), "konsum" to T("Konsum", "Shopping"), "ki" to T("KI & Maschinen", "AI & machines"),
    "krise" to T("Krise", "Crisis"),
)

// 🎟️ RANKS + rank() ➡ ui/Moments.kt (C2: Beförderung + My Capital use them too)

/** ❓ C7 teaser 4 tomorrow (D-048 c): plan day's `qd`/`qe` · fallback = 1st sentence o its Andacht ending i "?" · else null (nvr a cut verse) */
private fun teaseOf(d: de.strix.marxversion.data.PlanDay?, lang: Lang): String? {
    d ?: return null
    val q = if (lang == Lang.DE) d.teaseDe else d.teaseEn.ifBlank { d.teaseDe }
    if (q.isNotBlank()) return q
    val a = if (lang == Lang.DE) d.andachtDe else d.andachtEn
    return Regex("[^.!?]*\\?").find(a)?.value?.trim()?.takeIf { it.length in 8..140 }
}

/** first n words + "…" (zh: no spaces ➡ chars) */
private fun teaser(s: String, lang: Lang, words: Int = 8) =
    if (lang == Lang.ZH) s.take(words * 2) + "…" else s.split(" ").let { w -> w.take(words).joinToString(" ") + if (w.size > words) " …" else "" }

/** 🎉 / 🌅 — real calendar + clock, or th 🪝 preview */
@Composable
private fun feastNow(vm: AppVm): Feast? = vm.feastPreview.collectAsStateWithLifecycle().value ?: Feast.on(LocalDate.now())

private const val DEFAULT_PLAN = "30tage"
private const val LUDDITES = "ludditen"   // data/plans_extra.json
private val PlanAccent = PlanAccents[1]  // plan card accent (aubergine = Plans-tab 2nd colour)

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
    var moment by remember { mutableStateOf<Int?>(null) }   // T3 milestone due (streak count) · null = none
    val persona = Persona.of(s.persona)
    val doneToday = days.any { it.date == today() }
    // 🃏 cards: 🔮 pull down = oracle card · 📳 shake = crisis card (+ red flash) — both land i front i th carousel
    val extras by vm.extraCards.collectAsStateWithLifecycle()
    val jump by vm.homeJump.collectAsStateWithLifecycle()
    // 🎉 fx = th app's 1 celebration queue (Fx.kt, MainActivity host) ➡ nvr on top o a badge dialog · was 4 direct overlays
    val fx = LocalCelebrations.current
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val roast: () -> Unit = {
        vm.content.verses.filter { it.polemic && it.quoteDe.isNotBlank() }.randomOrNull()?.let {
            vm.addCard(CardItem(it, CardItem.Kind.KRISE)); fx?.play(Fx.Krise)
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
    // 🏆 C2: wh today's reading day would bring (computed b4 th log lands) ➡ T3 milestone · T4 Beförderung
    val gameAfter = remember(days) { streakGame(days + DayLog(today(), "losung", 0)) }
    LaunchedEffect(total) { fx?.gold = cardsDone(total) >= 7 }   // 🎖️ level-7 perk: gold ☭ swoosh (D-063)
    val amen: (Verse) -> Unit = { v ->
        Toast.makeText(ctx, amenMsg, Toast.LENGTH_LONG).show()
        buzz(ctx, longArrayOf(0, 40, 60, 90))
        fx?.play(Fx.Check)   // D-039: quote-of-th-day = small ✓ pop only · ☭ swoosh = plan day done
        // D-049: Amen day hits a milestone ➡ ✓ ➡ T3 directly (no pane) · full card ➡ T4 (milestone = its 2nd line)
        if (!doneToday && fx != null) {
            if (fills) { rankUp = cardsDone(total + 1) - 1; moment = gameAfter.milestoneToday; fx.reward("rank") }
            else gameAfter.milestoneToday?.let { moment = it; fx.reward("milestone") }
        }
        vm.amen(v.id)   // AFTER play ➡ a new badge dialog queues behind th fx
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
        // Column, not Box: Hero emits th widget + its 4 HookNotes as siblings ➡ i a Box they stacked ON th hero (qa_1117 r3 🐛 "top hook occludes")
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) { Hero(days, feast, dt, s.testMode) }   // D-036 = A (🦠 @1116)
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
        // 🐛 qa_1120: DEV "unlock all" didn't reach th moments ➡ th workbench shows w Testmodus OR DEV unlock
        if (s.testMode || (IS_DEV && s.devUnlock)) {
            val soon = tr("🔔 kommt in ein paar Sekunden", "🔔 arriving in a few seconds")
            Card(colors = CardDefaults.cardColors(containerColor = Ds.hook), modifier = Modifier.padding(bottom = 12.dp)) {
                Column(Modifier.padding(10.dp)) {
                    Text(tr("🪝 Hooked-Modus — Werkbank", "🪝 Hooked mode — workbench"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AssistChip(onClick = openHooked, label = { Text(tr("📖 Hooked erklärt", "📖 Hooked explained")) })
                        AssistChip(onClick = { vm.reroll.value++ }, label = { Text(tr("🎲 Neuer Vers", "🎲 New verse")) })
                        AssistChip(onClick = { askNotif(); vm.testNotifNow(); Toast.makeText(ctx, soon, Toast.LENGTH_SHORT).show() },
                            label = { Text(tr("🔔 Benachrichtigung", "🔔 Notification")) })
                        AssistChip(onClick = { vm.forcePopup.value = true }, label = { Text(tr("🌅 Losung-Popup", "🌅 Verse popup")) })
                        // 🧪 C2 moments, 1 chip each (qa_1120): ⏱️ Stempeluhr (+ "Moment ▸" ➡ T3) · 🏅 Meilenstein · 🎖️ Beförderung
                        listOf<Pair<String, () -> Unit>>(
                            tr("⏱️ Stempeluhr", "⏱️ Time clock") to { moment = 14; fx?.reward("dayDone") ?: run { celebrate = true }; fx?.reward("milestone") },
                            tr("🏅 Meilenstein", "🏅 Milestone") to { moment = 14; fx?.reward("milestone") },
                            tr("🎖️ Beförderung", "🎖️ Promotion") to { rankUp = 2; moment = null; fx?.reward("rank") },
                        ).forEach { (label, go) -> AssistChip(onClick = go, label = { Text(label) }) }
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
            // 🔢 D-042: th cut u started on (a re-cut plan nvr shifts ur days) · pinned = old cut ➡ idx-keyed translations skipped
            val planDays = plan.daysFor(s.planVer)
            val pinned = planDays !== plan.days
            val idxOrNull = planIndex(done, planDays.size)
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
            val day = planDays[idx]
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
                        Text(tr("Tag %d von %d", "Day %d of %d").f(idx + 1, planDays.size), fontSize = 12.sp, color = Color(0xFF6B6275))
                        Text(tr("noch %d Lesetage bis „%s“", "%d more reading days to “%s”").f(stampCard(readingDays(days)).left, tr(rank(cardsDone(readingDays(days))))),
                            fontSize = 12.sp, color = Color(0xFF6B6275))
                    }
                }
                LinearProgressIndicator(progress = { (idx + if (planToday != null) 1 else 0).toFloat() / planDays.size },
                    color = accent, trackColor = accent.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).height(4.dp).clip(Shapes.pill))
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
            val ad = andacht(plan.id, idx, day, pinned)
            if (ad.isNotBlank()) {
                Gap(8)
                Column(Modifier.fillMaxWidth().clip(Shapes.m).background(accent.copy(alpha = 0.07f)).padding(12.dp)) {
                    // 🔊 D-087: `a:<plan>:<day>` = audio contract id ➡ pack clip if loaded + txt unchanged (sha) · else phone voice · tap again = stop
                    val clipId = de.strix.marxversion.data.VoicePacks.andachtId(plan.id, idx)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(tr("🕯️ Andacht", "🕯️ Devotional"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent, modifier = Modifier.weight(1f))
                        speaker.aiLabelFor(clipId)?.let { Text("🔊 $it", style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted) }   // contract: AI speech is labelled
                        val listen = if (speaker.speaking) tr("Stopp", "Stop") else tr("Andacht vorlesen", "Listen to devotional")
                        Box(Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button) { speaker.say(ad, lang, persona, clipId) }
                            .semantics { contentDescription = listen }, contentAlignment = Alignment.Center) {
                            Icon(painterResource(R.drawable.ic_volume), null, tint = if (speaker.speaking) Red else accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(ad, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Gap(8)
            // qa_1116 r2.1: full txt, no "weiterlesen · ~1 min" expand (big round #3 snippet off · VerseView `preview` stays 4 later)
            day.verseIds.mapNotNull { vm.content.verse(it) }.forEach { VerseView(vm, it, speaker); Gap(4) }
            // 💡 curated plans (🤖 Ludditen): NEW bridge txt Marx ➡ today, under th original passage (#6)
            val br = bridge(plan.id, idx, day, pinned)
            if (br.isNotBlank()) {
                Gap(4)
                Column(Modifier.fillMaxWidth().clip(Shapes.m).background(Blue.copy(alpha = 0.07f)).padding(12.dp)) {
                    Text(tr("💡 Und heute?", "💡 And today?"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue)
                    Text(br, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
                    Text(tr("neuer Text — Marx' Absatz oben ist original", "new text — Marx's paragraph above is the original"),
                        fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Gap()
            // Hooked ch5: investment loads th nxt trigger ➡ tease tomorrow's reading
            // C2 Stempeluhr: tomorrow = a teasing Q (C7), not a cut quote
            nextTeaser = teaseOf(planDays.getOrNull(idx + 1), lang)
            if (planToday == null) {
                Button(onClick = {
                    val total = readingDays(days)
                    if (!doneToday && cardJustFilled(total + 1)) rankUp = cardsDone(total + 1) - 1
                    moment = if (!doneToday) gameAfter.milestoneToday else null
                    // 🐛 @1116: ☭ got lost i 4708ab7 (✓ pop only) ➡ back (D-039) · C2: confetti moved 2 th Beförderung screen
                    fx?.play(Fx.Swoosh)
                    // D-049 queue: ☭ ➡ Stempeluhr ➡ T3 Meilenstein ➡ T4 Beförderung ➡ badges · T3 + T4 same day ➡ T4 only (2nd line)
                    if (fx == null) celebrate = true else {
                        fx.reward("dayDone")
                        if (rankUp != null) fx.reward("rank") else if (moment != null) fx.reward("milestone")
                    }
                    vm.dayDone(plan.id, idx, day.verseIds)   // AFTER th reward ➡ a new badge queues behind th pane
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
        var kiAsk by remember { mutableStateOf(false) }
        var kiTapped by remember { mutableStateOf(false) }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            NERVT.keys.forEach { k ->
                // answer = a card, flies 2 th front o th deck up top
                FilterChip(selected = false, onClick = { oracleCard(marxoskop(vm.content.verses, k)); if (k == "ki") kiTapped = true },
                    label = { Text(tr(NERVT_LABEL.getValue(k))) })
            }
        }
        // 🤖 KI chip ➡ link straight 2 th Ludditen plan (#6, QA @1112 "link directly to AI")
        if (kiTapped && s.activePlan != LUDDITES && vm.content.plan(LUDDITES) != null) {
            Card(onClick = { kiAsk = true }, colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(tr("🤖 Mehr davon? 7 Tage „Maschinenstürmer & KI“ — Kapitel 13 + was es heute heißt ➡",
                    "🤖 Want more? 7 days of “Luddites & AI” — chapter 13 + what it means today ➡"),
                    fontSize = 14.sp, modifier = Modifier.padding(12.dp))
            }
        }
        if (kiAsk) MarxDialog({ kiAsk = false }, title = tr("Plan wechseln?", "Switch plan?"), actions = {
            TextButton(onClick = { kiAsk = false }) { Text(tr("Später", "Later")) }
            Button(onClick = { kiAsk = false; kiTapped = false; vm.startPlan(LUDDITES) }) { Text(tr("🤖 Los", "🤖 Start")) }
        }) {
            Text(tr("Dein jetziger Plan bleibt gespeichert — du kannst jederzeit unter „Pläne“ zurück.",
                "Your current plan stays saved — switch back any time under “Plans”."))
        }
        HookNote(Hook.FORER, s.testMode)
        // (pull/shake hint lives only under th cards: down here "pull down" doesn't work ➡ misleading, rm'd @1104)
        Gap(40)
        // (📜 endless feed = th reader's 📜 mode now, qa_1116 Q4/Q7 · Home ends at Marxoskop)

        // 🎉 Day complete: queued slot (plan btn, aftr th ☭) · `celebrate` = direct (🧪 test chip, no queue)
        // ⏱️ C2 Stempeluhr (D-048) · shown aftr th log landed ➡ `days` = incl today
        val dayDoneDialog: @Composable (() -> Unit) -> Unit = { close ->
            val g = remember(days) { streakGame(days) }
            val tot = readingDays(days)
            StempeluhrPane(stampCard(tot), cardsDone(tot) + 1 - (if (stampCard(tot).filled == 0) 1 else 0), g.streak, g.tier,
                tr(shiftLine()), nextTeaser, more = fx?.queued?.isNotEmpty() == true, hooked = s.testMode, onClose = close, level = cardsDone(tot))
        }
        RewardSlot("dayDone") { done -> dayDoneDialog(done) }
        if (celebrate) dayDoneDialog { celebrate = false }
        // 🏅 T3 Meilenstein (D-046) · ✊ token if earned today
        RewardSlot("milestone") { done ->
            val g = remember(days) { streakGame(days) }
            val n = moment ?: g.streak
            MilestoneScreen(n, maxOf(g.tier, de.strix.marxversion.logic.tierOf(n)), best = n >= g.best, chain = chain(days),
                strikeEarned = g.strikeEarnedToday, hooked = s.testMode) { moment = null; done() }
        }
        // 🎖️ T4 Beförderung (D-050) · same-day milestone = 2nd line
        RewardSlot("rank") { done ->
            PromotionScreen(rankUp ?: 0, readingDays(days), moment, s.testMode) { rankUp = null; moment = null; done() }
        }
    }
    }  // inner padded Column
    }  // PullToRefreshBox

}

/**
 * 🌅 hero (à la Insight Timer's streak widget, @1106): gradient by time o day (or 🎉 feast) · nudge title
 * ("Save your chain!") · this week Mo–Su (✓ read · red ! = today still open · 🕊 Sabbat) · streak nr i a red star ·
 * 🎟️ Stempelkarte (🎁 gifts · ☭ earned · red ! = today's stamp waiting) + "N more days to <rank>".
 * ✔️ D-036 = A (🦠 @1116): this hero as it was b4 th ds work · B (bigger) + compact + collapse ❌ · git: 384cbab
 */
@Composable
private fun Hero(days: List<DayLog>, feast: Feast?, dt: DayTime, hooked: Boolean) {
    val white = Color.White
    // C2 · D-045 c: hero = streak world only (★ tier + ✊ + week + nxt-milestone bar) · stamps + rank ➡ Stempeluhr + My Capital
    val g = remember(days) { streakGame(days) }
    val st = g.streak; val rest = g.rest
    val today = LocalDate.now()
    val read = days.map { it.date }.toSet()
    val doneToday = today.toString() in read
    val level = cardsDone(read.size)   // 🎖️ perks (D-050): 1 gold rim · 5 gear rim · 7 gold star
    val heroShape = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(heroShape)
            .then(if (level >= 1) Modifier.border(2.dp, de.strix.marxversion.ui.ds.Ds.gold, heroShape) else Modifier)
            .background(Brush.verticalGradient((feast?.colors ?: dt.colors).take(3).map { Color(it) }))
            .background(Color.Black.copy(alpha = 0.15f))  // scrim: white txt stays readable on light stops
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 🥚 D-066: feast ➡ th day's egg pops in left o th title (same art as th launcher icon)
            if (feast != null) de.strix.marxversion.ui.ds.EggPop(feast, Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (st == 1) tr("1 Tag Lesekapital akkumuliert", "1 day of reading capital accumulated")   // qa_1117: was "1 days"
                    else if (st > 0) tr("%d Tage Lesekapital akkumuliert", "%d days of reading capital accumulated").f(st)
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                de.strix.marxversion.ui.ds.StreakStar(st, g.tier, level = level, feast = feast)
                if (g.strikes > 0 || st >= 7) de.strix.marxversion.ui.ds.StrikeSlots(g.strikes, Modifier.padding(top = 2.dp))
            }
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
                    d in rest -> Triple(de.strix.marxversion.ui.ds.Ds.blue, white, "🕊")   // qa_1120: white dove on blue (was on white = invisible)
                    d in g.struck -> Triple(de.strix.marxversion.ui.ds.Ds.red, white, "✊")   // ✊ Streiktag: red, white fist
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
        g.strikeUsedLast?.takeIf { it in g.struck && !it.isBefore(today.minusDays(6)) }?.let {
            Text(tr("✊ Streiktag eingelöst — die Kette hält.", "✊ Strike day used — the chain holds."),
                color = white.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
        }
        // 📉 D-047 Abschreibung: tier above th streak's own = capital writing itself off · copy 1× = only th day aftr a missed day
        val y = today.minusDays(1)
        val missedYesterday = y.toString() !in read && y !in rest && y !in g.struck
        if (g.depreciating && missedYesterday) Text(tr("Dein Lesekapital schreibt sich ab — langsam. Ein Tag reicht, um es zu stoppen.",
            "Your reading capital is depreciating — slowly. One day is enough to stop it."),
            color = white.copy(alpha = 0.9f), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 2.dp))
        Gap(12)
        // 🎯 nxt milestone bar (D-045 c · goal gradient) · stamp row + rank ➡ Stempeluhr pane + My Capital
        MilestoneBar(st, g.next, Modifier.fillMaxWidth())
    }
    HookNote(Hook.STREAK, hooked)
    HookNote(Hook.MILESTONE, hooked)
    HookNote(Hook.SABBATH, hooked)
    HookNote(if (feast != null) Hook.FEAST else Hook.DAYTIME, hooked)
}

// ★ StreakStar (tier ornaments + ✊ slots) ➡ ui/ds/Emblem.kt (C2)

// 🌅 CardDialog + LosungPopup (fullscreen daily card · verse-share preview) rm'd @1113 (QA r4): cards live on Home since @1106,
//    verse share gone fr th reader ➡ both were dead · git: d0ea734
