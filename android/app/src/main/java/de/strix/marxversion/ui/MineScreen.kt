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
        Text(tr("Homescreen lang drücken ➡ Widgets ➡ bibleApp: Losung + 🔥 Streak, Farbe wechselt mit der Tageszeit.",
            "Long-press the home screen ➡ Widgets ➡ bibleApp: daily verse + 🔥 streak, colour changes with the time of day."),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        HookNote(Hook.WIDGET, s.testMode)
        Gap(8)
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

        // ── about ──
        Gap()
        Text(tr("Über", "About"), fontWeight = FontWeight.Bold)
        Text("$APP_NAME · " + tr("Version %s", "Version %s").f(BuildConfig.VERSION_NAME), fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
        Gap(6)
        Text(tr("Marx lesen wie die Bibel: jeden Tag ein Häppchen Kapital. Und nebenbei ein Lehrbuch, wie Apps Gewohnheiten bauen — schalte den Hooked-Modus ein.",
            "Read Marx like the Bible: a bite of Capital every day. And on the side a textbook on how apps build habits — switch on Hooked mode."), fontSize = 13.sp)
        Gap(6)
        Text(tr("🤖 Stolz gevibecodet mit Claude Code (Anthropic, Claude Opus) — strix hat gewünscht, Claude hat getippt. Seiten-OCR: gemma3 + GLM-OCR, Übersetzungen: Qwen3, alles lokal auf einer RTX 3090 Ti. Entfremdete Arbeit, vollautomatisiert. Die Produktionsmittel gehören trotzdem uns.",
            "🤖 Proudly vibe-coded with Claude Code (Anthropic, Claude Opus) — strix wished, Claude typed. Page OCR: gemma3 + GLM-OCR, translations: Qwen3, all local on an RTX 3090 Ti. Alienated labour, fully automated. The means of production are still ours."), fontSize = 13.sp)
        Gap(6)
        Text(tr("Text: Marx, Das Kapital Bd. 1, 4. Aufl. 1890 (MEW 23, Seitenzahlen = Dietz 1968). Englisch: Moore/Aveling 1887. Gemeinfrei.",
            "Text: Marx, Capital vol. 1, 4th ed. 1890 (MEW 23, page numbers = Dietz 1968). English: Moore/Aveling 1887. Public domain."), fontSize = 13.sp)
        Text(tr("Mechanik: abgeschaut bei Bibel-Apps, erklärt in Nir Eyal, „Hooked“, Kap. 7.",
            "Mechanics: borrowed from bible apps, explained in Nir Eyal, “Hooked”, ch. 7."), fontSize = 13.sp)
        Text(tr("Zitate, Andachten + Übersetzungen (IT · ES · PT · 中文 · عربي): von einem lokalen LLM — Irrtümer vorbehalten, Marx hat immer recht.",
            "Quotes, devotionals + translations (IT · ES · PT · 中文 · عربي): by a local LLM — errors reserved, Marx is always right."), fontSize = 13.sp)
    }

    if (pickTime) TimeWheelDialog(s.notifHour, s.notifMinute, onDismiss = { pickTime = false }) { h, m ->
        vm.setNotif(s.notifOn, h, m); pickTime = false
    }
}
