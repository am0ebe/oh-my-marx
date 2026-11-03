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
    Release("0.6.0", "2026-11-03", listOf(
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
@Composable
fun ReleaseNotes() {
    var all by remember { mutableStateOf(false) }
    val mine = BuildConfig.VERSION_NAME
    val dev = "-dev." in mine
    Text(tr("📜 Was ist neu", "📜 What's new"), fontWeight = FontWeight.Bold)
    if (dev) Text(tr("🧪 Dev-Build %s — neuer als jede Release, ungetestet", "🧪 Dev build %s — newer than any release, untested").f(mine),
        fontSize = 12.sp, color = Red)
    (if (all) RELEASES else RELEASES.take(2)).forEach { r ->
        val here = mine == r.version || (dev && mine.startsWith(r.version))
        Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
            .then(if (here) Modifier.background(Gold.copy(alpha = 0.16f), RoundedCornerShape(8.dp)) else Modifier).padding(6.dp)) {
            Row {
                Text("v${r.version}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("  ·  ${r.date}" + if (here) "  ·  " + tr("⬅ du hast diese", "⬅ you have this") else "",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
            r.items.forEach { Text("• " + tr(it), fontSize = 13.sp) }
        }
    }
    if (RELEASES.size > 2) Text(if (all) tr("weniger ⬆", "less ⬆") else tr("alle Versionen ⬇", "all versions ⬇"), fontSize = 12.sp, color = Blue,
        modifier = Modifier.clickable { all = !all }.padding(vertical = 4.dp))
}
