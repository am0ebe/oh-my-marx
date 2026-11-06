package de.strix.marxversion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f

/**
 * 📜 release notes (⚙️ ➡ Was ist neu) · newest 1st · add th nxt release on top b4 bin/ship.sh
 * items = T(de, en) ➡ bin/translate.py --only ui translates them like any UI string
 */
data class Release(val version: String, val date: String, val items: List<Pair<String, String>>)

val RELEASES = listOf(
    Release("0.9.0", "2026-11-06", listOf(
        T("🌍 6 neue Sprachen: Französisch, Russisch, Hindi, Bengalisch, Urdu, Indonesisch — das ganze Buch inklusive", "🌍 6 new languages: French, Russian, Hindi, Bengali, Urdu, Indonesian — the whole book included"),
        T("🔥 Neue Streak-Karte: deine Woche auf einen Blick, „Rette deine Kette!“, Streak im roten Stern", "🔥 New streak card: your week at a glance, “Save your chain!”, streak in a red star"),
        T("☭ Amen: einfach die heutige Karte antippen (oder den großen roten Knopf)", "☭ Amen: just tap today's card (or the big red button)"),
        T("💥 Amen-Animation: Hammer und Sichel krachen zusammen", "💥 Amen animation: hammer and sickle smash together"),
        T("🎨 Lesepläne in Farbe", "🎨 Reading plans in colour"),
        T("🌍 Startet in der Sprache deines Handys, sonst auf Englisch · Deutsch steht an zweiter Stelle", "🌍 Starts in your phone's language, otherwise English · German comes second"),
        T("📜 Was ist neu: nur die neueste Version, ältere auf Antippen — wie auf der Webseite", "📜 What's new: only the newest version, older ones on tap — like on the website"),
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

/** ⚙️ section: newest open, rest collapsed · marks th installed one (dev build = newer than th top release) */
/** leading emoji off (same rule as bin/notes.py --plain) · Changelog.kt keeps them (GitHub release notes use them) */
private val LEAD = Regex("^[^\\p{L}\\p{N}„“\"(]+")
private fun plain(s: String) = s.replaceFirst(LEAD, "")

@Composable
fun ReleaseNotes() {
    var all by remember { mutableStateOf(false) }
    val mine = BuildConfig.VERSION_NAME
    val dev = "-dev." in mine
    // mirrors th download page (@1106): no emoji · newest version only · tap ➡ older ones
    Text(tr("Was ist neu", "What's new"), fontWeight = FontWeight.Bold)
    if (dev) Text(tr("Dev-Build %s — neuer als jede Release, ungetestet", "Dev build %s — newer than any release, untested").f(mine),
        fontSize = 12.sp, color = Red)
    (if (all) RELEASES else RELEASES.take(1)).forEach { r ->
        val here = mine == r.version || (dev && mine.startsWith(r.version))
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .then(if (here) Modifier.background(Gold.copy(alpha = 0.16f), RoundedCornerShape(8.dp)) else Modifier).padding(6.dp)) {
            Row {
                Text("v${r.version}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("  ·  ${r.date}" + if (here) "  ·  " + tr("⬅ du hast diese", "⬅ you have this") else "",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            r.items.forEach { Text("• " + plain(tr(it)), fontSize = 13.sp) }
        }
    }
    if (RELEASES.size > 1) Text(if (all) tr("weniger ⬆", "less ⬆") else tr("Ältere Versionen ⬇", "Older versions ⬇"), fontSize = 12.sp, color = Blue,
        modifier = Modifier.clickable { all = !all }.padding(vertical = 4.dp))
}
