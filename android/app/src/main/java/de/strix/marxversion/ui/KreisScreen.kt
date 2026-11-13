package de.strix.marxversion.ui

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.Kreis
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.today
import java.time.LocalDate

// ── 👥 Lesekreise (community step ③): read w friends, see who said Amen today, a shared Stempelkarte, a chat ──
// solidarity, not competition: th shared card only fills on days when EVYONE i th circle read

/** shared reading days = days (last 60) on which evy current member sent an Amen */
fun sharedDays(members: Set<String>, events: List<Matrix.Event>): Int {
    if (members.isEmpty()) return 0
    val byDay = events.filter { it.type == Matrix.AMEN && it.day != null }.groupBy({ it.day!! }, { it.sender })
    val from = LocalDate.now().minusDays(60).toString()
    return byDay.count { (day, who) -> day >= from && who.toSet().containsAll(members) }
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
    var busy by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    val notFound = tr("⚠️ Kein Kreis mit diesem Code — vertippt? Internet?", "⚠️ No circle with this code — typo? Internet?")
    val failed = tr("⚠️ Ging nicht — Internet? Nochmal versuchen.", "⚠️ Didn't work — internet? Try again.")
    val created = tr("✔️ Kreis gegründet · Code %s · antippen ➡ 📤 Einladen", "✔️ Circle founded · code %s · tap it ➡ 📤 Invite")
    LaunchedEffect(kreise.size) { kreise.forEach { vm.loadKreis(it.room) } }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(tr("Lesekreise", "Reading circles"), fontSize = 22.sp, fontWeight = FontWeight.Bold)   // sweep r4: no emoji, like evy oth screen title
        Text(tr("Lies mit Freund:innen: wer hat heute Amen gesagt, ein gemeinsamer Chat und eine Stempelkarte, die sich nur füllt, wenn alle gelesen haben. Solidarität statt Wettbewerb.",
            "Read with friends: who said Amen today, a shared chat and a stamp card that only fills when everyone has read. Solidarity, not competition."),
            fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        HookNote(Hook.SOCIAL, vm.settings.collectAsStateWithLifecycle().value.testMode)
        Gap(12)

        // 📨 came in via an invite link ➡ alr a member? open it · else 1 tap 2 join
        val pending by vm.pendingKreis.collectAsStateWithLifecycle()
        pending?.let { pc ->
            val idx = kreise.indexOfFirst { it.code == pc }
            if (idx >= 0) LaunchedEffect(pc) { vm.pendingKreis.value = null; open(idx) }
            else Card(colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.25f)), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(tr("📨 Einladung in einen Lesekreis", "📨 Invitation to a reading circle"), fontWeight = FontWeight.Bold)
                    Text(pc, fontFamily = FontFamily.Monospace, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Red)
                    WritesAs(name ?: myName)
                    Row {
                        Button(onClick = {
                            busy = true; msg = null
                            vm.joinKreis(pc, name ?: myName.takeIf { it.isNotBlank() }) { ok -> busy = false; if (ok) vm.pendingKreis.value = null else msg = notFound }
                        }, enabled = !busy && (name != null || myName.isNotBlank())) { Text(tr("☭ Beitreten", "☭ Join")) }
                        TextButton(onClick = { vm.pendingKreis.value = null }) { Text(tr("Später", "Later")) }
                    }
                }
            }
        }

        kreise.forEachIndexed { i, k ->
            val m = members[k.room].orEmpty()
            val ev = tl[k.room].orEmpty()
            val todayWho = ev.filter { it.type == Matrix.AMEN && it.day == today() }.map { it.sender }.toSet()
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { open(i) }) {
                Column(Modifier.padding(12.dp)) {
                    Text(k.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(tr("%d Leute · heute %d ✔️ · 🎟️ %d gemeinsame Tage", "%d people · today %d ✔️ · 🎟️ %d shared days")
                        .f(m.size, todayWho.intersect(m.keys).size, sharedDays(m.keys, ev)), fontSize = 13.sp)
                    ev.lastOrNull { it.type == "m.room.message" }?.let {
                        Text("💬 ${it.name}: ${it.text}", fontSize = 12.sp, maxLines = 1, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        if (kreise.isEmpty()) Text(tr("🌱 Noch kein Kreis. Gründe einen und schick den Code an Freund:innen — oder tritt mit einem Code bei.",
            "🌱 No circle yet. Found one and send the code to friends — or join with a code."), modifier = Modifier.padding(vertical = 8.dp))
        Gap(16)

        // QA r5 (#8): th name field lives only i ⚙️ · here: who u r + where 2 change it (no account yet = th prefilled comrade name)
        WritesAs(name ?: myName)
        Gap(8)
        val me = name ?: myName.takeIf { it.isNotBlank() }
        Text(tr("＋ Kreis gründen", "＋ Found a circle"), fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(newName, { newName = it }, singleLine = true, modifier = Modifier.weight(1f),
                placeholder = { Text(tr("z. B. Kapital-Küche", "e.g. Capital kitchen")) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
            Button(onClick = {
                busy = true; msg = null
                vm.createKreis(newName, me) { c -> busy = false; if (c == null) msg = failed else { newName = ""; msg = created.f(c) } }
            }, enabled = !busy && newName.isNotBlank() && me != null, modifier = Modifier.padding(start = 8.dp)) { Text(tr("Gründen", "Found")) }  // ☭ = close only (@1107)
        }
        Gap(12)
        Text(tr("🔑 Mit Code beitreten", "🔑 Join with a code"), fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(code, { code = it.uppercase().take(8) }, singleLine = true, modifier = Modifier.weight(1f),
                placeholder = { Text("K7M2Q9") }, textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 18.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
            Button(onClick = {
                busy = true; msg = null
                vm.joinKreis(code, me) { ok -> busy = false; if (ok) code = "" else msg = notFound }
            }, enabled = !busy && code.length >= 6 && me != null, modifier = Modifier.padding(start = 8.dp)) { Text(tr("Beitreten", "Join")) }
        }
        if (busy) CircularProgressIndicator(Modifier.padding(8.dp))
        msg?.let { Text(it, color = if (it.startsWith("✔️")) MaterialTheme.colorScheme.primary else Red, fontSize = 13.sp) }
        if (!vm.commentsAvailable) Text(tr("⚠️ Diese Version kann keine Kreise (kein Server-Token gebaut).", "⚠️ This build can't do circles (no server token)."), color = Red, fontSize = 12.sp)
        Gap(12)
        Text(tr("Kreise laufen über unseren eigenen Matrix-Server (scagent.de). Nicht Ende-zu-Ende-verschlüsselt — schreib nichts, was der Server-Betreiber nicht lesen soll.",
            "Circles run on our own Matrix server (scagent.de). Not end-to-end encrypted — don't write what the server operator shouldn't read."),
            fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
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
    val chat = ev.filter { it.type == "m.room.message" }
    val list = rememberLazyListState()
    LaunchedEffect(k.room) { vm.loadKreis(k.room) }
    LaunchedEffect(chat.size) {
        if (chat.isNotEmpty()) { list.scrollToItem(chat.lastIndex + 1); vm.kreisSeen(k.room, chat.last().ts) }  // 🔔 seen ➡ no notif 4 these
    }
    // link = th landing page (bin/kreis.html) ➡ "In Oh My Marx öffnen" ➡ app opens w th code · + th code 4 typing
    val invite = tr("Komm in meinen Lesekreis „%s“ bei Oh My Marx ☭\nAntippen: %s\n(oder in der App: 👥 Kreis ➡ Code %s · App: https://scagent.de/oh-my-marx/)",
        "Join my reading circle “%s” on Oh My Marx ☭\nTap: %s\n(or in the app: 👥 Circle ➡ code %s · app: https://scagent.de/oh-my-marx/)")
        .f(k.name, "https://scagent.de/oh-my-marx/k/?c=" + k.code, k.code)
    val inviteTitle = tr("Einladen", "Invite")

    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp).imePadding(), state = list) {
        item {
            Gap(12)
            Text(k.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Code: ", "Code: "), fontSize = 13.sp)
                Text(k.code, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Red)
                TextButton(onClick = {
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, invite), inviteTitle))
                    vm.flag("invite")   // 🎖️ Agitator
                }) { Text(tr("📤 Einladen", "📤 Invite")) }
            }
            Gap(8)
            // ✔️ who read today
            Text(tr("Heute", "Today"), fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                m.forEach { (uid, n) ->
                    val done = uid in todayWho
                    Text((if (done) "✔️ " else "· ") + n + if (uid == me) tr(" (du)", " (you)") else "", fontSize = 13.sp,
                        modifier = Modifier.background(if (done) Gold.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
            if (me != null && me !in todayWho) Text(tr("➡ Sag auf „Heute“ Amen ☭ — dann sehen's die anderen hier.", "➡ Say Amen ☭ on “Today” — then the others see it here."),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            Gap(12)
            // 🎟️ shared card: 1 stamp per day evyone read
            Text(tr("🎟️ Gemeinsame Stempelkarte — %d Tage, an denen alle gelesen haben", "🎟️ Shared stamp card — %d days on which everyone read").f(shared),
                fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                repeat(10) { n ->
                    val filled = n < shared % 10 || (shared > 0 && shared % 10 == 0)
                    Box(Modifier.size(26.dp).border(2.dp, Red, CircleShape).background(if (filled) Red else Gold.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center) { if (filled) Text("☭", fontSize = 12.sp, color = Gold) }
                }
            }
            Gap(12)
            Text(tr("💬 Chat", "💬 Chat"), fontWeight = FontWeight.Bold)
            if (chat.isEmpty()) Text(tr("🌱 Noch still hier. Was nervt dich diese Woche — und was sagt Marx dazu?", "🌱 Quiet here. What bugs you this week — and what does Marx say?"),
                fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        }
        items(chat, key = { it.id }) { c ->
            val mine = c.sender == me
            Column(Modifier.fillMaxWidth().padding(vertical = 3.dp).padding(start = if (mine) 40.dp else 0.dp, end = if (mine) 0.dp else 40.dp)
                .background(if (mine) Gold.copy(alpha = 0.22f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(10.dp)) {
                Row {
                    Text(if (mine) tr("du", "you") else c.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(DateUtils.getRelativeTimeSpanString(c.ts).toString(), fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Text(c.text, fontSize = 15.sp)
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                OutlinedTextField(draft, { draft = it }, modifier = Modifier.weight(1f), maxLines = 4,
                    placeholder = { Text(tr("Nachricht …", "Message …")) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                if (busy) CircularProgressIndicator(Modifier.padding(start = 8.dp))
                else Button(onClick = { busy = true; vm.kreisSay(k.room, draft) { ok -> busy = false; if (ok) draft = "" } },
                    enabled = draft.isNotBlank(), modifier = Modifier.padding(start = 8.dp)) { Text(tr("Senden", "Send")) }
            }
            Row {
                TextButton(onClick = { vm.loadKreis(k.room) }) { Text(tr("🔄 Aktualisieren", "🔄 Refresh")) }
                OutlinedButton(onClick = { vm.leaveKreis(k); back() }) { Text(tr("Kreis verlassen", "Leave circle")) }
            }
            Text(tr("Nicht Ende-zu-Ende-verschlüsselt · der Server-Betreiber könnte mitlesen.", "Not end-to-end encrypted · the server operator could read along."),
                fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }
    }
}
