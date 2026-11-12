package de.strix.marxversion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.IconButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.f

/** "Mein Kapital" — evy highlight/note/bookmark = th usr's accumulated (reading) capital */
@Composable
fun MineScreen(vm: AppVm, openChapter: (String, String) -> Unit) {
    val marks by vm.marks.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(tr("Mein Kapital", "My Capital"), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            // sweep r4: highlighter is gone fr th verse menu ➡ count only wh u can still make (old highlights still tint their verse)
            Text(tr("%d Notizen · %d Lesezeichen", "%d notes · %d bookmarks")
                .f(marks.count { it.note != null }, marks.count { it.bookmark }), fontSize = 13.sp)
            Text(tr("Deine vergegenständlichte Lesearbeit. Unveräußerlich — Export kommt noch.",
                "Your objectified reading labour. Inalienable — export coming."), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            HookNote(Hook.INVESTMENT, s.testMode)
            val orden by vm.orden.collectAsStateWithLifecycle()
            OrdenSection(orden, s.testMode)
            Gap(10)
            if (marks.isEmpty()) Text("\n" + tr("Noch nichts akkumuliert. Tipp auf einen Absatz ➡ Lesezeichen oder Notiz — ursprüngliche Akkumulation beginnt klein.",
                "Nothing accumulated yet. Tap a passage ➡ Bookmark or Note — primitive accumulation starts small."))
        }
        if (marks.any { it.bookmark }) item {
            Text(tr("🔖 Lesezeichen", "🔖 Bookmarks"), fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }
        items(marks, key = { it.verseId }) { m ->
            vm.content.verse(m.verseId)?.let { v ->
                Column(Modifier.fillMaxWidth().clickable { openChapter(v.kap, v.id) }.padding(vertical = 8.dp)) {
                    Text((if (m.bookmark) "🔖 " else "") + ref(v), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(quoted(verseQuote(v).ifBlank { verseText(v).take(160) }), fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                    m.note?.let { Text("✍️ $it", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun SettingsScreen(vm: AppVm, askNotifPermission: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var pickTime by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(tr("Einstellungen", "Settings"), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Gap()

        // ── daily verse + time (time row greyed out + locked when off) ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Tägliche Marx-Losung", "Daily Marx verse"), Modifier.weight(1f))
            Switch(s.notifOn, { on -> if (on) askNotifPermission(); vm.setNotif(on, s.notifHour, s.notifMinute) })
        }
        Row(
            Modifier.fillMaxWidth().alpha(if (s.notifOn) 1f else 0.35f)
                .then(if (s.notifOn) Modifier.clickable { pickTime = true } else Modifier).padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(tr("Uhrzeit", "Time"), Modifier.weight(1f))
            Text("%02d:%02d".f(s.notifHour, s.notifMinute), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        HookNote(Hook.TRIGGER, s.testMode)
        HorizontalDivider()

        // ── voice persona ──
        Gap(8)
        Text(tr("🔊 Vorlese-Stimme — antippen = Hörprobe", "🔊 Reading voice — tap = sample"))
        val speaker = rememberSpeaker()
        val lang = LocalLang.current
        val sample = tr("Das Kapital ist verstorbne Arbeit, die sich nur vampyrmäßig belebt.",
            "Capital is dead labour, that, vampire-like, only lives by sucking living labour.")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Persona.picker.forEach { p ->
                FilterChip(selected = Persona.of(s.persona) == p, onClick = { vm.setPersona(p); speaker.sample(sample, lang, p) }, label = { Text(tr(p.label)) })
            }
        }
        Gap(8)
        HorizontalDivider()

        // ── 📱 widget hint ──
        Gap(8)
        Text(tr("📱 Widget", "📱 Widget"))
        Text(tr("Homescreen lang drücken ➡ Widgets ➡ Oh My Marx",
            "Long-press the home screen ➡ Widgets ➡ Oh My Marx"),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        HookNote(Hook.WIDGET, s.testMode)
        Gap(8)
        HorizontalDivider()

        // ── 👥 Mitlesen (opt-in · data/Community.kt + server/omm_api.py) ──
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tr("👥 Mitlesen", "👥 Read along"))
                Text(tr("anonym: zeigt, wie viele diese Seite heute lesen + schaltet Kommentare frei",
                    "anonymous: shows how many read this page today + unlocks comments"), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Switch(s.community, { vm.setCommunity(it) })
        }
        // 💬 comments: name = what others see (account is made w ur 1st comment, lives only on this fon)
        if (s.community) {
            val mx by vm.mxName.collectAsStateWithLifecycle()
            // QA r3: always editable · prefilled w a random comrade name · saves on blur / Done only if changed (no Save btn)
            val start = mx ?: vm.suggestedName
            var draft by remember(start) { mutableStateOf(start) }
            var focused by remember { mutableStateOf(false) }
            val focus = androidx.compose.ui.platform.LocalFocusManager.current
            fun commit() { val n = draft.trim(); if (n.isNotEmpty() && n != mx && (mx != null || n != vm.suggestedName)) vm.rename(n) else if (n.isEmpty()) draft = start }
            OutlinedTextField(draft, { draft = it }, singleLine = true, modifier = Modifier.fillMaxWidth()
                .onFocusChanged { f -> if (focused && !f.isFocused) commit(); focused = f.isFocused },
                label = { Text(nameLabel()) },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
        }
        HookNote(Hook.SOCIAL, s.testMode)
        HorizontalDivider()

        // ── 🪝 Hooked mode (= explainer + 🪝 notes everywhere + test buttons) ──
        val openHooked = LocalOpenHooked.current
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tr("🪝 Hooked-Modus", "🪝 Hooked mode"))
                Text(tr("Die App erklärt, wie sie dich anfixt: gelbe 🪝-Zettel an jedem Trick, eine Lernseite zu Nir Eyals „Hooked“ + Test-Knöpfe auf „Heute“.",
                    "The app explains how it hooks you: yellow 🪝 notes on every trick, a lesson page on Nir Eyal's “Hooked” + test buttons on “Today”."), fontSize = 12.sp)
            }
            Switch(s.testMode, { vm.setTestMode(it) })
        }
        if (s.testMode) TextButton(onClick = openHooked) { Text(tr("📖 Hooked erklärt ➡", "📖 Hooked explained ➡")) }
        HorizontalDivider()

        // ── 📜 release notes (ui/Changelog.kt) ──
        Gap(8)
        ReleaseNotes()
        Gap(8)
        HorizontalDivider()

        // ── about ──
        Gap()
        Text(tr("Über", "About"), fontWeight = FontWeight.Bold)
        Text("$APP_NAME · " + tr("Version %s", "Version %s").f(BuildConfig.VERSION_NAME), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Gap(6)
        Text(tr("🤖 Stolz vibe-gecodet mit Claude Opus + Sonnet. Entfremdete Arbeit, vollautomatisiert. Die Produktionsmittel gehören trotzdem uns.",
            "🤖 Proudly vibe-coded with Claude Opus + Sonnet. Alienated labour, fully automated. The means of production are still ours."), fontSize = 13.sp)
        val ctx = androidx.compose.ui.platform.LocalContext.current
        // 🔘 🤏 icon row: 🌐 page · 📤 share th app · 💬 feedback = mail straight 2 us (version prefilled, 0 web hops)
        val pageUrl = de.strix.marxversion.data.Update.page
        val shareTxt = tr("Marx lesen wie die Bibel — Oh My Marx ☭", "Read Marx like the Bible — Oh My Marx ☭")   // ShareAppPanel adds APP_LINK
        var sharePanel by remember { mutableStateOf(false) }
        if (sharePanel) ShareAppPanel(shareTxt) { sharePanel = false }
        val ver = BuildConfig.VERSION_NAME
        val mailBody = tr("Was ist passiert / was wünschst du dir?\n\n\n(Version %s — ein Screenshot hilft)", "What happened / what would you like?\n\n\n(version %s — a screenshot helps)").f(ver)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            IconBtn("🌐", tr("Webseite", "Web page")) { runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(pageUrl))) } }
            IconBtn("📤", tr("App teilen", "Share the app")) { sharePanel = true }
            IconBtn("💬", tr("Feedback: Bug, Idee, Lob", "Feedback: bug, idea, praise")) { runCatching { ctx.startActivity(android.content.Intent(android.content.Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:")).apply {
                putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("omm@scagent.de"))
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Oh My Marx $ver")
                putExtra(android.content.Intent.EXTRA_TEXT, mailBody)
            }) } }
        }
        Gap(6)
        Text("🔓 GPL-3.0", fontSize = 13.sp)
    }

    if (pickTime) TimeWheelDialog(s.notifHour, s.notifMinute, onDismiss = { pickTime = false }) { h, m ->
        vm.setNotif(s.notifOn, h, m); pickTime = false
    }
}

/** 🤏 emoji icon button (contentDescription = a11y label) */
@Composable
private fun IconBtn(icon: String, desc: String, onClick: () -> Unit) {
    IconButton(onClick, Modifier.semantics { contentDescription = desc }) { Text(icon, fontSize = 22.sp) }
}
