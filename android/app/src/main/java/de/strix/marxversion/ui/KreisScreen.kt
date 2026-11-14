package de.strix.marxversion.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.Kreis
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.today
import de.strix.marxversion.ui.ds.Callout
import de.strix.marxversion.ui.ds.Check
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.DsField
import de.strix.marxversion.ui.ds.MeBubble
import de.strix.marxversion.ui.ds.OtherBubble
import de.strix.marxversion.ui.ds.Panel
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.TextBtn
import de.strix.marxversion.ui.ds.Tonal
import de.strix.marxversion.ui.ds.Tone
import kotlinx.coroutines.delay
import java.time.LocalDate

// ── 👥 Lesekreise (community step ③): read w friends, see who said Amen today, a shared Stempelkarte, a chat ──
// solidarity, not competition: th shared card only fills on days when EVYONE i th circle read
// 🎨 L3 @1114 (design_system.md § 10 Circle): found / join = 2 Panels · invite = Callout gold · chat = me blue / others paperDeep ·
//   legal small print ➡ ⓘ ➡ MarxDialog · "Leave circle" = destructive Text ➡ confirm dialog (🐛 left at once)

/** shared reading days = days (last 60) on which evy current member sent an Amen */
fun sharedDays(members: Set<String>, events: List<Matrix.Event>): Int {
    if (members.isEmpty()) return 0
    val byDay = events.filter { it.type == Matrix.AMEN && it.day != null }.groupBy({ it.day!! }, { it.sender })
    val from = LocalDate.now().minusDays(60).toString()
    return byDay.count { (day, who) -> day >= from && who.toSet().containsAll(members) }
}

/** ⓘ + "Privacy" ➡ dialog w th legal txt (was a 10-11sp line, § 3: sentences ≥ 14) */
@Composable
private fun PrivacyInfo(text: String) {
    var open by remember { mutableStateOf(false) }
    TextBtn("ⓘ  " + tr("Datenschutz", "Privacy"), { open = true })
    if (open) MarxDialog(onDismiss = { open = false }, title = tr("Datenschutz", "Privacy")) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
    }
}

/** member = chip off-state (paperDeep pill h 32) · read today = ✓ (state nvr by colour alone) · not tappable */
@Composable
private fun MemberChip(label: String, done: Boolean) =
    Row(Modifier.height(32.dp).background(Ds.paperDeep, CircleShape).padding(horizontal = Space.m),
        verticalAlignment = Alignment.CenterVertically) {
        if (done) Check(Ds.ink)
        Text(label, color = if (done) Ds.ink else Ds.inkMuted, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }

/** tab: my circles + found / join */
@Composable
fun KreiseScreen(vm: AppVm, open: (Int) -> Unit) {
    val kreise by vm.kreise.collectAsStateWithLifecycle()
    val name by vm.mxName.collectAsStateWithLifecycle()
    val members by vm.kreisMembers.collectAsStateWithLifecycle()
    val tl by vm.kreisTimeline.collectAsStateWithLifecycle()
    val myName = vm.suggestedName
    var newName by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf<String?>(null) }   // "found" · "join" · "invite" ➡ that btn spins
    var msg by remember { mutableStateOf<String?>(null) }
    val notFound = tr("⚠️ Kein Kreis mit diesem Code — vertippt? Internet?", "⚠️ No circle with this code — typo? Internet?")
    val failed = tr("⚠️ Ging nicht — Internet? Nochmal versuchen.", "⚠️ Didn't work — internet? Try again.")
    val created = tr("✔️ Kreis gegründet · Code %s · antippen ➡ 📤 Einladen", "✔️ Circle founded · code %s · tap it ➡ 📤 Invite")
    LaunchedEffect(kreise.size) { kreise.forEach { vm.loadKreis(it.room) } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.gutter),
        verticalArrangement = Arrangement.spacedBy(Space.betwPanels)) {
        Column {
            Text(tr("Lesekreise", "Reading circles"), style = MaterialTheme.typography.titleLarge, color = Ds.ink)   // sweep r4: no emoji, like evy oth screen title
            Text(tr("Lies mit Freund:innen: wer hat heute Amen gesagt, ein gemeinsamer Chat und eine Stempelkarte, die sich nur füllt, wenn alle gelesen haben. Solidarität statt Wettbewerb.",
                "Read with friends: who said Amen today, a shared chat and a stamp card that only fills when everyone has read. Solidarity, not competition."),
                style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted, modifier = Modifier.padding(top = Space.s))
            HookNote(Hook.SOCIAL, vm.settings.collectAsStateWithLifecycle().value.testMode)
        }

        // 📨 came in via an invite link ➡ alr a member? open it · else 1 tap 2 join
        val pending by vm.pendingKreis.collectAsStateWithLifecycle()
        pending?.let { pc ->
            val idx = kreise.indexOfFirst { it.code == pc }
            if (idx >= 0) LaunchedEffect(pc) { vm.pendingKreis.value = null; open(idx) }
            else Callout(Tone.Gold, lead = "📨", title = tr("Einladung in einen Lesekreis", "Invitation to a reading circle")) {
                Text(pc, style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace), color = Ds.ink,
                    modifier = Modifier.padding(vertical = Space.xs))
                WritesAs(name ?: myName)
                Row(Modifier.fillMaxWidth().padding(top = Space.s), horizontalArrangement = Arrangement.spacedBy(Space.s, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically) {
                    TextBtn(tr("Nicht jetzt", "Not now"), { vm.pendingKreis.value = null })
                    Primary(tr("Beitreten", "Join"), {
                        busy = "invite"; msg = null
                        vm.joinKreis(pc, name ?: myName.takeIf { it.isNotBlank() }) { ok -> busy = null; if (ok) vm.pendingKreis.value = null else msg = notFound }
                    }, large = false, loading = busy == "invite", enabled = busy == null && (name != null || myName.isNotBlank()))
                }
            }
        }

        kreise.forEachIndexed { i, k ->
            val m = members[k.room].orEmpty()
            val ev = tl[k.room].orEmpty()
            val todayWho = ev.filter { it.type == Matrix.AMEN && it.day == today() }.map { it.sender }.toSet()
            Panel(onClick = { open(i) }) {
                Text(k.name, style = MaterialTheme.typography.titleMedium, color = Ds.ink)
                Text(tr("%d Leute · heute %d ✔️ · 🎟️ %d gemeinsame Tage", "%d people · today %d ✔️ · 🎟️ %d shared days")
                    .f(m.size, todayWho.intersect(m.keys).size, sharedDays(m.keys, ev)), style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
                ev.lastOrNull { it.type == "m.room.message" }?.let {
                    Text("💬 ${it.name}: ${it.text}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, color = Ds.inkMuted)
                }
            }
        }
        if (kreise.isEmpty()) Text(tr("🌱 Noch kein Kreis. Gründe einen und schick den Code an Freund:innen — oder tritt mit einem Code bei.",
            "🌱 No circle yet. Found one and send the code to friends — or join with a code."),
            style = MaterialTheme.typography.bodyLarge, color = Ds.ink, modifier = Modifier.padding(vertical = Space.s))

        // QA r5 (#8): th name field lives only i ⚙️ · here: who u r + where 2 change it (no account yet = th prefilled comrade name)
        WritesAs(name ?: myName)
        val me = name ?: myName.takeIf { it.isNotBlank() }
        // ＋ found = Panel w 1 Primary-S
        Panel {
            Text(tr("Kreis gründen", "Found a circle"), style = MaterialTheme.typography.titleSmall, color = Ds.ink,
                modifier = Modifier.padding(bottom = Space.s))
            DsField(newName, { newName = it }, label = tr("Name des Kreises", "Circle name"),
                placeholder = tr("z. B. Kapital-Küche", "e.g. Capital kitchen"),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
            Row(Modifier.fillMaxWidth().padding(top = Space.s), horizontalArrangement = Arrangement.End) {
                Primary(tr("Gründen", "Found"), {   // ☭ = close only (@1107)
                    busy = "found"; msg = null
                    vm.createKreis(newName, me) { c -> busy = null; if (c == null) msg = failed else { newName = ""; msg = created.f(c) } }
                }, large = false, loading = busy == "found", enabled = busy == null && newName.isNotBlank() && me != null)
            }
        }
        // 🔑 join = Panel w Tonal (only 1 Primary per Panel)
        Panel {
            Text(tr("Mit Code beitreten", "Join with a code"), style = MaterialTheme.typography.titleSmall, color = Ds.ink,
                modifier = Modifier.padding(bottom = Space.s))
            DsField(code, { code = it.uppercase().take(8) }, label = tr("Code", "Code"), placeholder = "K7M2Q9",
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
            Row(Modifier.fillMaxWidth().padding(top = Space.s), horizontalArrangement = Arrangement.End) {
                Tonal(tr("Beitreten", "Join"), {
                    busy = "join"; msg = null
                    vm.joinKreis(code, me) { ok -> busy = null; if (ok) code = "" else msg = notFound }
                }, loading = busy == "join", enabled = busy == null && code.length >= 6 && me != null)
            }
        }
        msg?.let { Text(it, color = if (it.startsWith("✔️")) Ds.ink else Ds.redDeep, style = MaterialTheme.typography.bodyMedium) }
        if (!vm.commentsAvailable) Text(tr("⚠️ Diese Version kann keine Kreise (kein Server-Token gebaut).", "⚠️ This build can't do circles (no server token)."),
            color = Ds.redDeep, style = MaterialTheme.typography.bodyMedium)
        PrivacyInfo(tr("Kreise laufen über unseren eigenen Matrix-Server (scagent.de). Nicht Ende-zu-Ende-verschlüsselt — schreib nichts, was der Server-Betreiber nicht lesen soll.",
            "Circles run on our own Matrix server (scagent.de). Not end-to-end encrypted — don't write what the server operator shouldn't read."))
    }
}

/** 1 circle: today's ✔️ per person · shared Stempelkarte · chat · invite */
@Composable
fun KreisDetail(vm: AppVm, i: Int, back: () -> Unit) {
    val kreise by vm.kreise.collectAsStateWithLifecycle()
    val k: Kreis = kreise.getOrNull(i) ?: return
    val members by vm.kreisMembers.collectAsStateWithLifecycle()
    val tl by vm.kreisTimeline.collectAsStateWithLifecycle()
    val me by produceState<String?>(null) { value = vm.myUserId() }
    val ctx = LocalContext.current
    val m = members[k.room].orEmpty()
    val ev = tl[k.room].orEmpty()
    val todayWho = ev.filter { it.type == Matrix.AMEN && it.day == today() }.map { it.sender }.toSet()
    val shared = sharedDays(m.keys, ev)
    var draft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }
    val chat = ev.filter { it.type == "m.room.message" }
    val list = rememberLazyListState()
    LaunchedEffect(k.room) { vm.loadKreis(k.room) }
    LaunchedEffect(chat.size) {
        if (chat.isNotEmpty()) { list.scrollToItem(chat.lastIndex + 1); vm.kreisSeen(k.room, chat.last().ts) }  // 🔔 seen ➡ no notif 4 these
    }
    LaunchedEffect(copied) { if (copied) { delay(1200); copied = false } }   // inline ✓ 1.2 s, no Toast (§ 7)
    // link = th landing page (bin/kreis.html) ➡ "In Oh My Marx öffnen" ➡ app opens w th code · + th code 4 typing
    val invite = tr("Komm in meinen Lesekreis „%s“ bei Oh My Marx ☭\nAntippen: %s\n(oder in der App: 👥 Kreis ➡ Code %s · App: https://scagent.de/oh-my-marx/)",
        "Join my reading circle “%s” on Oh My Marx ☭\nTap: %s\n(or in the app: 👥 Circle ➡ code %s · app: https://scagent.de/oh-my-marx/)")
        .f(k.name, "https://scagent.de/oh-my-marx/k/?c=" + k.code, k.code)
    val inviteTitle = tr("Einladen", "Invite")

    if (confirmLeave) MarxDialog(onDismiss = { confirmLeave = false }, title = tr("Kreis verlassen?", "Leave circle?"),
        primary = DialogAction(tr("Verlassen", "Leave")) { confirmLeave = false; vm.leaveKreis(k); back() },
        defer = DialogAction(tr("Bleiben", "Stay")) { confirmLeave = false }) {
        Text(tr("Chat und gemeinsame Stempelkarte von „%s“ verschwinden. Mit dem Code %s kannst du wieder beitreten.",
            "The chat and shared stamp card of “%s” disappear. You can rejoin with the code %s.").f(k.name, k.code),
            style = MaterialTheme.typography.bodyMedium, color = Ds.ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = Space.gutter).imePadding(), state = list) {
        item {
            Text(k.name, style = MaterialTheme.typography.titleLarge, color = Ds.ink, modifier = Modifier.padding(top = Space.l))
            // code = Tonal copy-pill (tap = copy + ✓) · invite = share sheet
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s),
                modifier = Modifier.padding(top = Space.s)) {
                Tonal((if (copied) "✓ " else tr("Code ", "Code ")) + k.code, {
                    ctx.getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("code", k.code))
                    copied = true
                })
                Tonal(tr("Einladen", "Invite"), {
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, invite), inviteTitle))
                    vm.flag("invite")   // 🎖️ Agitator
                })
            }
            Spacer(Modifier.height(Space.xl))
            // ✔️ who read today
            Text(tr("Heute", "Today"), style = MaterialTheme.typography.titleSmall, color = Ds.ink, modifier = Modifier.padding(bottom = Space.s))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s), verticalArrangement = Arrangement.spacedBy(Space.s)) {
                m.forEach { (uid, n) -> MemberChip(n + if (uid == me) tr(" (du)", " (you)") else "", uid in todayWho) }
            }
            if (me != null && me !in todayWho) Text(tr("➡ Sag auf „Heute“ Amen ☭ — dann sehen's die anderen hier.", "➡ Say Amen ☭ on “Today” — then the others see it here."),
                style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted, modifier = Modifier.padding(top = Space.s))
            Spacer(Modifier.height(Space.xl))
            // 🎟️ shared card: 1 stamp per day evyone read
            Text(tr("🎟️ Gemeinsame Stempelkarte — %d Tage, an denen alle gelesen haben", "🎟️ Shared stamp card — %d days on which everyone read").f(shared),
                style = MaterialTheme.typography.titleSmall, color = Ds.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.padding(vertical = Space.s)) {
                repeat(10) { n ->
                    val filled = n < shared % 10 || (shared > 0 && shared % 10 == 0)
                    Box(Modifier.size(28.dp).border(2.dp, Ds.red, CircleShape).background(if (filled) Ds.red else Ds.gold.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center) { if (filled) Text("☭", style = MaterialTheme.typography.labelMedium, color = Ds.gold) }
                }
            }
            Spacer(Modifier.height(Space.l))
            Text(tr("💬 Chat", "💬 Chat"), style = MaterialTheme.typography.titleSmall, color = Ds.ink, modifier = Modifier.padding(bottom = Space.s))
            if (chat.isEmpty()) Text(tr("🌱 Noch still hier. Was nervt dich diese Woche — und was sagt Marx dazu?", "🌱 Quiet here. What bugs you this week — and what does Marx say?"),
                style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted)
        }
        // 🗣️ voices: mine = blue ruler (same "me" as margin notes) end · others = paperDeep bubble start
        items(chat, key = { it.id }) { c ->
            val mine = c.sender == me
            val time = DateUtils.getRelativeTimeSpanString(c.ts).toString()
            Box(Modifier.fillMaxWidth().padding(vertical = Space.xs)
                .padding(start = if (mine) Space.xxl else 0.dp, end = if (mine) 0.dp else Space.xxl),
                contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart) {
                if (mine) MeBubble {
                    Text(tr("du", "you") + "  " + time, style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted)
                    Text(c.text)
                } else OtherBubble(c.name, time) { Text(c.text) }
            }
        }
        item {
            // composer = comments composer: field r12 + Primary-S "Send" (1 Primary on th screen)
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(vertical = Space.s)) {
                DsField(draft, { draft = it }, modifier = Modifier.weight(1f), singleLine = false,
                    placeholder = tr("Nachricht …", "Message …"),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                Primary(tr("Senden", "Send"), { busy = true; vm.kreisSay(k.room, draft) { ok -> busy = false; if (ok) draft = "" } },
                    large = false, loading = busy, enabled = draft.isNotBlank() && !busy, modifier = Modifier.padding(start = Space.s))
            }
            // DE labels r long ➡ wraps instead o clipping (360dp)
            FlowRow(Modifier.fillMaxWidth().padding(bottom = Space.l), horizontalArrangement = Arrangement.spacedBy(Space.xs)) {
                TextBtn(tr("Aktualisieren", "Refresh"), { vm.loadKreis(k.room) })
                PrivacyInfo(tr("Nicht Ende-zu-Ende-verschlüsselt · der Server-Betreiber könnte mitlesen.", "Not end-to-end encrypted · the server operator could read along."))
                TextBtn(tr("Kreis verlassen", "Leave circle"), { confirmLeave = true }, danger = true)
            }
        }
    }
}
