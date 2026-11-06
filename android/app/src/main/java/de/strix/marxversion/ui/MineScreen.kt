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
            Text(tr("%d Markierungen · %d Notizen · %d Lesezeichen", "%d highlights · %d notes · %d bookmarks")
                .f(marks.count { it.highlight != null }, marks.count { it.note != null }, marks.count { it.bookmark }), fontSize = 13.sp)
            Text(tr("Deine vergegenständlichte Lesearbeit. Unveräußerlich — Export kommt noch.",
                "Your objectified reading labour. Inalienable — export coming."), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            HookNote(Hook.INVESTMENT, s.testMode)
            if (marks.isEmpty()) Text("\n" + tr("Noch nichts akkumuliert. Tipp auf einen Vers ➡ Markieren — ursprüngliche Akkumulation beginnt klein.",
                "Nothing accumulated yet. Tap a verse ➡ Highlight — primitive accumulation starts small."))
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
            Persona.entries.forEach { p ->
                FilterChip(selected = s.persona == p.name, onClick = { vm.setPersona(p); speaker.sample(sample, lang, p) }, label = { Text(tr(p.label)) })
            }
        }
        Text(tr("Heilig = die Android-Stimme 2–3× in verschiedenen Tonhöhen übereinander (Oktave drunter = Autorität, drüber = Chor) + Kirchenhall, auf dem Handy gerechnet. Erstes Abspielen dauert 1–3 s, danach sofort.",
            "Holy = the Android voice stacked 2–3× at different pitches (octave below = authority, above = choir) + church reverb, computed on the phone. First play takes 1–3 s, then instant."), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        Gap(8)
        HorizontalDivider()

        // ── 📱 widget hint ──
        Gap(8)
        Text(tr("📱 Widget", "📱 Widget"))
        Text(tr("Homescreen lang drücken ➡ Widgets ➡ Oh My Marx: Losung + 🔥 Streak, Farbe wechselt mit der Tageszeit.",
            "Long-press the home screen ➡ Widgets ➡ Oh My Marx: daily verse + 🔥 streak, colour changes with the time of day."),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        HookNote(Hook.WIDGET, s.testMode)
        Gap(8)
        HorizontalDivider()

        // ── 👥 Mitlesen (opt-in · data/Community.kt + server/omm_api.py) ──
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text(tr("👥 Mitlesen", "👥 Read along"))
                Text(tr("Zeigt, wie oft eine Seite gelesen wurde und wie viele heute Amen sagen. Gesendet wird nur: welche Seite + eine Zufallszahl, die sich jeden Tag ändert. Kein Konto, keine IP gespeichert, der Server vergisst die Zufallszahl am nächsten Tag.",
                    "Shows how often a page was read and how many say Amen today. Sent: only which page + a random number that changes every day. No account, no IP stored, the server forgets the number the next day."), fontSize = 12.sp)
            }
            Switch(s.community, { vm.setCommunity(it) })
        }
        // 💬 comments: name = what others see (account is made w ur 1st comment, lives only on this fon)
        if (s.community) {
            val mx by vm.mxName.collectAsStateWithLifecycle()
            Text(tr("💬 Dazu: unter jeder Buchseite kommentieren. Kommentare sind öffentlich für alle Mitlesenden.",
                "💬 Plus: comment under every book page. Comments are public to everyone reading along."), fontSize = 12.sp)
            mx?.let { cur ->
                var edit by remember { mutableStateOf(false) }
                var draft by remember(cur) { mutableStateOf(cur) }
                if (!edit) TextButton(onClick = { edit = true }) { Text(tr("👤 Dein Name: %s ✏️", "👤 Your name: %s ✏️").f(cur)) }
                else Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(draft, { draft = it }, singleLine = true, modifier = Modifier.weight(1f))
                    TextButton(onClick = { vm.rename(draft); edit = false }) { Text(tr("Speichern", "Save")) }
                }
            }
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
        Text(tr("Marx lesen wie die Bibel: jeden Tag ein Häppchen Kapital. Und nebenbei ein Lehrbuch, wie Apps Gewohnheiten bauen — schalte den Hooked-Modus ein.",
            "Read Marx like the Bible: a bite of Capital every day. And on the side a textbook on how apps build habits — switch on Hooked mode."), fontSize = 13.sp)
        Gap(6)
        Text(tr("🤖 Stolz gevibecodet von einem Zentauren: am0ebe (Mensch, will) + Claude Opus 5.5 in Claude Code (Anthropic, tippt). Übersetzungen: Qwen3, lokal auf einer RTX 3090 Ti. Entfremdete Arbeit, vollautomatisiert. Die Produktionsmittel gehören trotzdem uns.",
            "🤖 Proudly vibe-coded by a centaur: am0ebe (human, wants) + Claude Opus 5.5 in Claude Code (Anthropic, types). Translations: Qwen3, local on an RTX 3090 Ti. Alienated labour, fully automated. The means of production are still ours."), fontSize = 13.sp)
        Text("💬 omm@scagent.de", fontSize = 13.sp)
        Gap(6)
        // 📜 sources ⬄ what we (app / LLM) put on top · keep i sync w README.md § Sources
        Gap(6)
        Text(tr("📜 Quellen", "📜 Sources"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(tr("• Deutsch: Karl Marx, Das Kapital Bd. 1, 4. Aufl. 1890 (hg. Engels), Text nach MEW 23 (Dietz), über das archivierte mlwerke.de. Seitenzahlen = MEW 23. Fußnoten = Marx' eigene.",
            "• German: Karl Marx, Das Kapital vol. 1, 4th ed. 1890 (ed. Engels), text as in MEW 23 (Dietz), via the archived mlwerke.de. Page numbers = MEW 23. Footnotes = Marx's own."), fontSize = 13.sp)
        Text(tr("• Englisch: Übersetzung Samuel Moore & Edward Aveling 1887 (hg. Engels), über marxists.org.",
            "• English: translation by Samuel Moore & Edward Aveling 1887 (ed. Engels), via marxists.org."), fontSize = 13.sp)
        Text(tr("• Porträt: J. J. E. Mayall, London 1875. Alles gemeinfrei.",
            "• Portrait: J. J. E. Mayall, London 1875. All public domain."), fontSize = 13.sp)
        Text(tr("• Mechanik: abgeschaut bei Bibel-Apps, erklärt in Nir Eyal, „Hooked“ (2014), Kap. 7 — nur kurz zitiert.",
            "• Mechanics: borrowed from bible apps, explained in Nir Eyal, “Hooked” (2014), ch. 7 — only briefly quoted."), fontSize = 13.sp)
        Gap(6)
        Text(tr("🤖 Was die App obendrauf macht", "🤖 What the app adds on top"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(tr("• Absätze Deutsch ⬄ Englisch automatisch zugeordnet — kann mal danebenliegen.",
            "• Paragraphs German ⬄ English paired automatically — may be off now and then."), fontSize = 13.sp)
        Text(tr("• Italienisch, Spanisch, Portugiesisch, Chinesisch, Arabisch: das ganze Buch + Fußnoten von einem lokalen LLM (Qwen3) aus dem Deutschen übersetzt, mit einem Glossar der üblichen Fachbegriffe (Mehrwert, Ware …) — keine veröffentlichte Ausgabe, kann holpern.",
            "• Italian, Spanish, Portuguese, Chinese, Arabic: the whole book + footnotes translated from German by a local LLM (Qwen3), with a glossary of the standard terms (surplus value, commodity …) — not a published edition, may stumble."), fontSize = 13.sp)
        Text(tr("• Zitate: ein lokales LLM (Qwen3) wählt und bewertet sie, der Wortlaut ist aber immer Marx' Originalsatz (automatisch geprüft).",
            "• Quotes: a local LLM (Qwen3) picks and rates them, but the wording is always Marx's original sentence (checked automatically)."), fontSize = 13.sp)
        Text(tr("• Andachten: vom LLM geschrieben (Deutsch + Englisch).",
            "• Devotionals: written by the LLM (German + English)."), fontSize = 13.sp)
        Text(tr("• IT · ES · PT · 中文 · عربي: App, Zitate, Andachten + Pläne maschinell übersetzt (Qwen3). Der volle Kapital-Text mit Fußnoten bleibt Deutsch + Englisch — in diesen Sprachen liest du ihn auf Englisch.",
            "• IT · ES · PT · 中文 · عربي: app, quotes, devotionals + plans machine-translated (Qwen3). The full Capital text with footnotes stays German + English — in these languages you read it in English."), fontSize = 13.sp)
        Text(tr("Irrtümer vorbehalten, Marx hat immer recht.", "Errors reserved, Marx is always right."), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Gap(6)
        Text(tr("🔓 Quellcode: github.com/am0ebe/oh-my-marx (GPL-3.0) — selbst bauen statt APK vertrauen.",
            "🔓 Source code: github.com/am0ebe/oh-my-marx (GPL-3.0) — build it yourself instead of trusting the APK."), fontSize = 13.sp)
    }

    if (pickTime) TimeWheelDialog(s.notifHour, s.notifMinute, onDismiss = { pickTime = false }) { h, m ->
        vm.setNotif(s.notifOn, h, m); pickTime = false
    }
}
