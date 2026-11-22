package de.strix.marxversion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.T
import de.strix.marxversion.data.Update
import de.strix.marxversion.data.f

/**
 * 📜 release notes (⚙️ ➡ Was ist neu) · newest 1st · add th nxt release on top b4 bin/ship.sh
 * items = T(de, en) ➡ bin/translate.py --only ui translates them like any UI string
 */
data class Release(val version: String, val date: String, val items: List<Pair<String, String>>)

val RELEASES = listOf(
    // 0.13.0 = C-lanes (C1-C7) · eggs · Matrix fixes (D-075/076/077) · #325 stable 4 th updater tst
    Release("0.13.0", "2026-11-22", listOf(
        T("🥚 30 Feiertage: Marx verkleidet sich — in der Leiste oben, auf „Heute“, auf der Zitatkarte und im Widget", "🥚 30 feast days: Marx dresses up — in the top bar, on “Today”, on the quote card and in the widget"),
        T("🧮 Serien-Spiel: Meilensteine, ✊ Streiktag, Stempeluhr und Beförderungen · Level mit Rang-Vorteilen", "🧮 Streak game: milestones, ✊ strike day, time clock and promotions · levels with rank perks"),
        T("📜 Wie ein heiliges Buch: rote Initialen, Kolumnentitel, Siegel am Kapitelende", "📜 Like a holy book: red initials, running heads, a seal at the end of each chapter"),
        T("✋ Lange drücken = Menü in beiden Lesearten · Antippen blättert um", "✋ Hold = menu in both reading modes · a tap turns the page"),
        T("❓ Jeder Plantag endet mit einer Frage für morgen", "❓ Every plan day ends with a question for tomorrow"),
        T("🏷️ „Was nervt dich?“: die Zitate passen jetzt besser zum Thema", "🏷️ “What bugs you?”: the quotes now fit the topic better"),
        T("💬 Kommentare und Lesekreise gehen wieder · neben fremden Namen steht die echte Konto-ID", "💬 Comments and reading circles work again · the real account id sits next to other people's names"),
        T("🗑️ ⚙️ Konto löschen: löscht deine Kommentare und Kreis-Nachrichten, dann das Konto", "🗑️ ⚙️ Delete account: deletes your comments and circle messages, then the account"),
    )),
    // 0.12.0 = qa_1117 ⚡ lanes (L1-L5)
    Release("0.12.0", "2026-11-18", listOf(
        T("📖 Ruhigere Seiten: keine Quellenzeile mehr über jedem Absatz", "📖 Calmer pages: no more reference line above every paragraph"),
        T("📜 Der Lesemodus-Knopf sagt jetzt, was er tut — und zeigt kurz, wie man blättert oder scrollt", "📜 The reading-mode button now says what it does — and briefly shows how to turn or scroll"),
        T("🪝 Hooked-Modus: die Erklärungen verdecken deine Serie nicht mehr", "🪝 Hooked mode: the explanations no longer cover your streak"),
        T("🔤 Keine abgeschnittenen Knöpfe mehr („Karte teilen“) · Über: Quellcode und Lizenz klar beschriftet", "🔤 No more cut-off buttons (“Share card”) · About: source and licence clearly labelled"),
        T("★ Schließen-Stern: kleiner und mittig im Kreis", "★ Close star: smaller and centred in its circle"),
    )),
    // 0.11.0 = qa_1116 lanes · "full passages" line corrects 0.10.0's "read more" (r2.1 rm'd it)
    Release("0.11.0", "2026-11-17", listOf(
        T("📖📜 Zwei Arten zu lesen: Buch mit Seiten oder endlos scrollen — umschalten oben im Kapitel", "📖📜 Two ways to read: a book with pages or endless scroll — switch at the top of a chapter"),
        T("☭ Hammer und Sichel sind zurück: wenn du das Tagespensum deines Leseplans schaffst · Zitat des Tages = kurzer Haken", "☭ Hammer and sickle are back: when you finish your reading plan's daily portion · quote of the day = a quick tick"),
        T("📤 Neues Teilen-Fenster: echte Logos, Farbe, QR-Code", "📤 New share panel: real logos, colour, QR code"),
        T("👆 Seitenrand antippen = umblättern · das Notizfeld bleibt über der Tastatur", "👆 Tap the page edge to turn · the note field stays above the keyboard"),
        T("✂️ Kein „weiterlesen“ mehr: auf „Heute“ steht der ganze Absatz", "✂️ No more “read more”: “Today” shows the whole passage"),
        T("🌍 Alle Knöpfe in allen 13 Sprachen · „Und heute?“-Texte im Maschinenstürmer-Plan übersetzt", "🌍 Every button in all 13 languages · “And today?” notes in the Luddites plan translated"),
        T("🇬🇧 Englisch: jetzt wirklich jeder Absatz auf Englisch", "🇬🇧 English: now truly every paragraph in English"),
        T("⚡ Schnellerer Start: Sprachen werden erst geladen, wenn du sie brauchst", "⚡ Faster start: languages load only when you need them"),
    )),
    Release("0.10.0", "2026-11-16", listOf(
        T("🎨 Neues Design: Papier-Look, ruhigere Knöpfe, Kapitel ohne Leisten — liest sich wie ein Buch", "🎨 New design: paper look, calmer buttons, chapters without bars — reads like a book"),
        T("🆕 Neue Version? Du erfährst es sofort · ⚙️ „Automatisch aktualisieren“ = ohne Nachfrage (APK von der Webseite)", "🆕 New version? You hear at once · ⚙️ “Update automatically” = no asking (APK from the website)"),
        T("✅ Amen fühlt sich besser an: kurzer Haken + Vibration, die große Feier nur zu besonderen Momenten", "✅ Amen feels better: a quick tick + buzz, the big celebration only for special moments"),
        T("⚙️ Über: nur noch die neueste Version, schlichter Abspann, Symbolreihe mittig", "⚙️ About: newest version only, simpler credits, centred icon row"),
        T("🔖 Lesezeichen mit eigener Überschrift", "🔖 Bookmarx with their own heading"),
        T("🆕 Updates melden sich selbst: Benachrichtigung bei neuer Version, antippen = laden + installieren", "🆕 Updates announce themselves: a notification when a new version is out, tap = download + install"),
        T("📖 Umblättern: wischen, doppelt tippen oder das Eselsohr unten rechts", "📖 Turning pages: swipe, double-tap or the dog-ear bottom right"),
        T("✍️ Notizen sehen aus wie Kommentare: eigene Farbe, Randlinie, antippen = fertig", "✍️ Notes look like comments: own colour, side line, tap = done"),
        T("📊 Lesefortschritt in Prozent", "📊 Reading progress in percent"),
        T("📜 Weiterlesen: nach dem Tagespensum geht das Buch einfach weiter — scrollen genügt", "📜 Keep reading: after your daily portion the book simply goes on — just scroll"),
        T("✂️ Lange Absätze auf „Heute“ gekürzt, „weiterlesen“ zeigt den Rest", "✂️ Long paragraphs on “Today” trimmed, “read more” shows the rest"),
        T("🤖 Neuer Leseplan „Maschinenstürmer & KI“: Kapitel 13 und was es heute heißt", "🤖 New reading plan “Luddites & AI”: chapter 13 and what it means today"),
        T("🔓 Hooked-Modus schaltet sich nach 7 Lesetagen frei — mit eigenem Abzeichen", "🔓 Hooked mode unlocks after 7 reading days — with its own badge"),
        T("🇬🇧 Englisch: Abschnittstitel auf Englisch, keine Lücken mehr, Kapitel 1 ohne Inhaltsverzeichnis-Salat", "🇬🇧 English: section titles in English, no more gaps, chapter 1 without table-of-contents clutter"),
    )),
    Release("0.9.0", "2026-11-08", listOf(
        T("🌍 6 neue Sprachen: Französisch, Russisch, Hindi, Bengalisch, Urdu, Indonesisch — das ganze Buch inklusive", "🌍 6 new languages: French, Russian, Hindi, Bengali, Urdu, Indonesian — the whole book included"),
        T("🏅 23 Abzeichen zum Verdienen — vom „Ersten Mehrwert“ bis „Hat's wirklich gelesen“, ein paar davon geheim (Mein Kapital)", "🏅 23 badges to earn — from “First surplus value” to “Actually read it”, a few of them secret (My Capital)"),
        T("🔥 Neue Streak-Karte: deine Woche auf einen Blick, „Rette deine Kette!“, Streak im roten Stern", "🔥 New streak card: your week at a glance, “Save your chain!”, streak in a red star"),
        T("☭ Amen: einfach die heutige Karte antippen (oder den großen roten Knopf)", "☭ Amen: just tap today's card (or the big red button)"),
        T("💥 Amen-Animation: Hammer und Sichel krachen zusammen", "💥 Amen animation: hammer and sickle smash together"),
        T("🎨 Lesepläne in Farbe", "🎨 Reading plans in colour"),
        T("🌍 Startet in der Sprache deines Handys, sonst auf Englisch · Deutsch steht an zweiter Stelle", "🌍 Starts in your phone's language, otherwise English · German comes second"),
        T("📜 Was ist neu: nur die neueste Version, ältere auf Antippen — wie auf der Webseite", "📜 What's new: only the newest version, older ones on tap — like on the website"),
        T("💬 Feedback-Knopf unter ⚙️ ➡ Über: Bug, Idee oder Lob direkt an uns", "💬 Feedback button under ⚙️ ➡ About: bug, idea or praise straight to us"),
        T("🌍 Sprachwechsel springt direkt in die neue Sprache (kein kurzes Englisch mehr dazwischen)", "🌍 Switching language jumps straight to the new one (no brief English in between)"),
    )),
    Release("0.8.0", "2026-11-06", listOf(
        T("📖 Das ganze Kapital jetzt auch auf Italienisch, Spanisch, Portugiesisch, Chinesisch und Arabisch — samt Fußnoten", "📖 The whole of Capital now also in Italian, Spanish, Portuguese, Chinese and Arabic — footnotes included"),
        T("🔊 Vorlesen liest den Absatz in deiner Sprache", "🔊 Read aloud reads the paragraph in your language"),
        T("🆕 Update-Hinweis nur noch, wenn du die APK selbst installiert hast — Obtainium sagt es dir sowieso", "🆕 Update notice only if you installed the APK yourself — Obtainium tells you anyway"),
        T("🤖 Übersetzt von einem lokalen LLM aus dem Deutschen — keine veröffentlichte Ausgabe (⚙️ ➡ Über ➡ Quellen)", "🤖 Translated from German by a local LLM — not a published edition (⚙️ ➡ About ➡ Sources)"),
    )),
    Release("0.7.0", "2026-11-04", listOf(
        T("👥 Lesekreise: gründen, Code teilen, beitreten · wer hat heute Amen gesagt · Chat", "👥 Reading circles: found, share the code, join · who said Amen today · chat"),
        T("🎟️ Gemeinsame Stempelkarte: ein Stempel nur, wenn alle gelesen haben", "🎟️ Shared stamp card: a stamp only when everyone has read"),
        T("📨 Einladung als Link: antippen ➡ App öffnet den Kreis", "📨 Invite as a link: tap ➡ the app opens the circle"),
        T("🔔 Neue Kreis-Nachrichten + abends: „Rosa hat heute gelesen — du noch nicht“", "🔔 New circle messages + in the evening: “Rosa read today — not you yet”"),
        T("🆕 Neue Version? Die App sagt Bescheid — antippen, drüber installieren, Daten bleiben", "🆕 New version? The app tells you — tap, install over it, your data stays"),
        T("🔘 Karten-Knöpfe einheitlich mit Beschriftung · Teilen nicht mehr so laut", "🔘 Card buttons all alike, with labels · Share not so loud anymore"),
        T("✨ Kein Flackern mehr, wenn du antippst, wo du schon bist", "✨ No more flicker when you tap where you already are"),
        T("📜 Über: welche Quellen, was LLM + App obendrauf gemacht haben · Quellcode offen (GPL-3.0)", "📜 About: which sources, what the LLM + app added on top · source code open (GPL-3.0)"),
    )),
    Release("0.6.0", "2026-11-04", listOf(
        T("💬 Kommentare unter jeder Buchseite — Name erst beim ersten Kommentar, eigene lange drücken = löschen", "💬 Comments under every book page — name only at your first comment, hold your own = delete"),
        T("👥 Mitlesen (an): „34× gelesen“ pro Seite + „Heute lesen 12 mit“", "👥 Read along (on): “read 34×” per page + “12 reading along today”"),
        T("🌅 Teilen aus dem Buch zeigt genau den Absatz, den du liest", "🌅 Sharing from the book shows exactly the paragraph you read"),
        T("📉 Krise! in allen Sprachen · 📜 diese Seite hier", "📉 Crisis! in every language · 📜 this page right here"),
    )),
    Release("0.5.2", "2026-11-03", listOf(
        T("🔗 Neue Adresse: scagent.de/oh-my-marx", "🔗 New address: scagent.de/oh-my-marx"),
    )),
    Release("0.5.1", "2026-11-03", listOf(
        T("☭ Neuer Name: Oh My Marx", "☭ New name: Oh My Marx"),
    )),
    Release("0.5.0", "2026-11-03", listOf(
        T("🃏 Start = Kartenstapel: wischen für frühere Losungen, Teilen + Amen direkt darunter", "🃏 Home = card deck: swipe for earlier verses, Share + Amen right below"),
        T("📖 Das Buch: echte MEW-Seiten, altes Papier, umblättern", "📖 The book: real MEW pages, old paper, page turns"),
        T("📳 Schütteln = Krise (rot, laut, eigene Karte) · ☭-Wirbel beim Amen", "📳 Shake = crisis (red, loud, its own card) · ☭ swirl on Amen"),
        T("🔖 Doppeltipp = Lesezeichen · Notizen direkt am Absatz", "🔖 Double-tap = bookmark · notes right at the paragraph"),
    )),
    Release("0.4.0", "2026-11-03", listOf(
        T("👆 Gesten: Losung als Stories, zwei Finger = Schrift, runterziehen = Orakel", "👆 Gestures: verse as stories, two fingers = font, pull down = oracle"),
        T("🚀 App-Icon lange drücken = Abkürzungen · Kachel in den Schnelleinstellungen · 🎉 Konfetti", "🚀 Long-press the icon = shortcuts · Quick Settings tile · 🎉 confetti"),
    )),
    Release("0.3.2", "2026-11-03", listOf(
        T("🌅 Losung bildschirmfüllend · 🔊 Stimmen-Hörprobe · ⏰ 24-Stunden-Rad · 😇 neues Icon", "🌅 Full-screen verse · 🔊 voice samples · ⏰ 24-hour wheel · 😇 new icon"),
    )),
    Release("0.3.1", "2026-11-02", listOf(
        T("🔊 Heilige Stimmen: Gottvater Marx, Gott, Marx im Lesesaal", "🔊 Holy voices: God the Father Marx, God, Marx in the reading room"),
    )),
    Release("0.3.0", "2026-11-01", listOf(
        T("🎉 12 Feiertage · 🔥 Streak mit Sabbat-Joker · 🎟️ Stempelkarte · 📱 Widget · 🪝 Hooked-Modus", "🎉 12 holidays · 🔥 streak with Sabbath joker · 🎟️ stamp card · 📱 widget · 🪝 Hooked mode"),
    )),
    Release("0.2.0", "2026-10-31", listOf(
        T("🌍 7 Sprachen · 🌅 tägliche Losung · Testmodus", "🌍 7 languages · 🌅 daily verse · test mode"),
    )),
)

/** version compare on x.y.z (suffix like -dev.* ignored) */
private fun ver(v: String) = v.substringBefore("-").split(".").map { it.toIntOrNull() ?: 0 }
fun versionNewer(a: String, b: String): Boolean {
    val x = ver(a); val y = ver(b)
    for (i in 0 until maxOf(x.size, y.size)) { val d = x.getOrElse(i) { 0 } - y.getOrElse(i) { 0 }; if (d != 0) return d > 0 }
    return false
}
/** ⏳ next, unshipped release = top entry newer than th installed base · DEV builds only (stable ➡ null) */
fun pendingRelease(mine: String, dev: Boolean): Release? = RELEASES.firstOrNull()?.takeIf { dev && versionNewer(it.version, mine) }

/** leading emoji off (same rule as bin/notes.py --plain) · Changelog.kt keeps them (GitHub release notes use them) */
private val LEAD = Regex("^[^\\p{L}\\p{N}„“\"(]+")
private fun plain(s: String) = s.replaceFirst(LEAD, "")

@Composable
fun ReleaseNotes() {
    val mine = BuildConfig.VERSION_NAME
    val dev = "-dev." in mine
    val pending = pendingRelease(mine, dev)
    val shown = RELEASES.first { it !== pending }
    Text(tr("Was ist neu", "What's new"), fontWeight = FontWeight.Bold)
    if (dev) {
        // honest DEV status (@1107): "newer than any release" lied when Obtainium was stuck on an old build
        val latest by produceState<Update.Latest?>(null) { value = Update.devLatest() }
        val l = latest
        Text(tr("Dev-Build %s — ungetestet", "Dev build %s — untested").f(mine), fontSize = 12.sp, color = Red)
        when {
            l == null -> {}
            l.code > BuildConfig.VERSION_CODE -> Text(tr("⚠️ Neuerer Dev-Build da: %s ➡ Obtainium", "⚠️ Newer dev build out: %s ➡ Obtainium").f(l.version),
                fontSize = 12.sp, color = Red, fontWeight = FontWeight.Bold)
            else -> Text(tr("✔️ neuester Dev-Build", "✔️ newest dev build"), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        }
        // 🧪 DEV: run th daily bg update check NOW (D-023 fon test: silent path wo waiting a day) · installer shown 4 th test
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val who = remember { Update.installer(ctx) ?: "adb/?" }
        androidx.compose.material3.TextButton(onClick = {
            androidx.work.WorkManager.getInstance(ctx).enqueue(androidx.work.OneTimeWorkRequestBuilder<de.strix.marxversion.notify.UpdateWorker>().build())
            android.widget.Toast.makeText(ctx, "🧪 update check queued", android.widget.Toast.LENGTH_SHORT).show()
        }) { Text("🧪 bg update check now · installer: $who", fontSize = 12.sp) }
    }
    // ⏳ DEV only: preview of th next release
    if (pending != null) {
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .background(Blue.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(8.dp)) {
            Text(tr("⏳ In Arbeit", "⏳ In progress") + " · v${pending.version}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            pending.items.forEach { Text("• " + plain(tr(it)), fontSize = 13.sp) }
        }
    }
    // last released version only — no older fold, no scroll box
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("v${shown.version}  ·  ${shown.date}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        shown.items.forEach { Text("• " + plain(tr(it)), fontSize = 13.sp) }
    }
}
