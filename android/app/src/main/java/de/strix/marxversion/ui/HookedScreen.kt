package de.strix.marxversion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.T

/**
 * 🪝 Hooked erklärt — th app as a textbook 4 Nir Eyal's "Hooked" (src: go/re/boox/hooked/hooked.md).
 * 4 phases: what th book says · how YouVersion (Bible App, Hooked ch7) does it · where it hides i THIS app.
 * go(dest) = jump 2 th spot: "settings" · "mine" · "today" · "popup"
 */
private class Phase(
    val title: Pair<String, String>,
    val what: Pair<String, String>,
    val bible: Pair<String, String>,
    val here: Pair<String, String>,
    val dest: String,
    val color: Color,
)

private val PHASES = listOf(
    Phase(
        T("⚡ 1 · Trigger", "⚡ 1 · Trigger"),
        T("Etwas sagt dir, was zu tun ist. Externe Trigger (Push, Widget, App-Icon) liegen in deiner Umgebung. Das Ziel ist ein interner Trigger: ein Gefühl, bei dem du von selbst zur App greifst — oft ein negatives (Langeweile, Frust, Einsamkeit).",
            "Something tells you what to do. External triggers (push, widget, app icon) sit in your environment. The goal is an internal trigger: a feeling that makes you reach for the app on your own — often a negative one (boredom, frustration, loneliness)."),
        T("Bibel-App: tägliche Push zum Leseplan, rotes Badge am Icon. Gewohnheitsleser öffnen sie, wenn es ihnen schlecht geht.",
            "Bible App: a daily push for your reading plan, a red badge on the icon. Habitual readers open it when they feel low."),
        T("Hier: 🔔 Losung zur selbst gewählten Uhrzeit · 📱 Widget auf dem Homescreen · 🌅 Farben + Gruß nach Tageszeit · 🎉 Feiertagsmotive · 🔮 Marxoskop „Was nervt?“ — der interne Trigger (Ärger über Chef oder Miete) direkt als Knopf.",
            "Here: 🔔 daily verse at a time you choose · 📱 home-screen widget · 🌅 colours + greeting by time of day · 🎉 holiday motifs · 🔮 Marxoscope “What's bugging you?” — the internal trigger (anger at the boss or the rent) as a button."),
        "settings", Color(0xFFFFE0B2),
    ),
    Phase(
        T("👆 2 · Aktion", "👆 2 · Action"),
        T("Die einfachste Handlung in Erwartung einer Belohnung. Fogg: B = MAT — Verhalten passiert nur, wenn Motivation, Fähigkeit und Trigger zusammenkommen. Immer zuerst die Fähigkeit erhöhen (einfacher machen), nicht die Motivation.",
            "The simplest action in anticipation of a reward. Fogg: B = MAT — behaviour only happens when motivation, ability and trigger meet. Always raise ability first (make it easier), not motivation."),
        T("Bibel-App: Lesepläne in „Hostien-Portionen“, Audio mit einem Tipp, die spannenden Stellen zuerst.",
            "Bible App: reading plans in “communion-wafer portions”, audio with one tap, the exciting passages first."),
        T("Hier: Popup beim Öffnen, der Vers ist schon da (0 Tipps) · ein einziger Vers zählt als Lesetag · Pläne in Häppchen · Tag 1 verpasst? ➡ „Marx für Erschöpfte“ · 🔊 Vorlesen.",
            "Here: popup on open, the verse is already there (0 taps) · a single verse counts as a reading day · plans in bites · missed day 1? ➡ “Marx for the Exhausted” · 🔊 read aloud."),
        "popup", Color(0xFFC8E6C9),
    ),
    Phase(
        T("🎁 3 · Variable Belohnung", "🎁 3 · Variable reward"),
        T("Unvorhersehbare Belohnungen halten bei der Stange (Skinner, Spielautomat). Drei Arten: Stamm (sozial), Jagd (Dinge, Infos), Selbst (Meisterschaft, Vollständigkeit). Endliche Abwechslung langweilt, unendliche hält.",
            "Unpredictable rewards keep you hooked (Skinner, slot machine). Three kinds: tribe (social), hunt (things, information), self (mastery, completion). Finite variety bores, infinite variety holds."),
        T("Bibel-App: ein unbekannter Vers des Tages — eine Leserin blieb bis Mitternacht wach, um den nächsten zu sehen. Forer-Effekt: der Vers passt immer genau zu dir.",
            "Bible App: an unknown verse of the day — one reader stayed up until midnight to see the next one. Forer effect: the verse always fits you exactly."),
        T("Hier: 🎲 jeden Tag eine neue Losung · 🔮 Marxoskop = Jagd · 🎉 Feiertagsmotive · 🔥 Streak + 🎟️ Stempelkarte + Ränge = Selbst · 📤 Spruchkarte teilen = Stamm.",
            "Here: 🎲 a new verse every day · 🔮 Marxoscope = hunt · 🎉 holiday motifs · 🔥 streak + 🎟️ stamp card + ranks = self · 📤 share the verse card = tribe."),
        "today", Color(0xFFBBDEFB),
    ),
    Phase(
        T("💼 4 · Investment", "💼 4 · Investment"),
        T("Nach der Belohnung um ein bisschen Arbeit bitten. Wir überschätzen, was wir selbst gemacht haben (IKEA-Effekt), wollen konsistent bleiben und keine kognitive Dissonanz. Investment speichert Wert und lädt den nächsten Trigger.",
            "After the reward, ask for a bit of work. We overvalue what we made ourselves (IKEA effect), want to stay consistent and avoid cognitive dissonance. Investment stores value and loads the next trigger."),
        T("Bibel-App: Markieren, Notizen, Lesezeichen ➡ die App wird zur eselsohrigen Privatbibel. Wechseln? Undenkbar.",
            "Bible App: highlights, notes, bookmarks ➡ the app becomes your dog-eared personal Bible. Switch? Unthinkable."),
        T("Hier: 🖍️ Markierungen, ✍️ Notizen, 🔖 Lesezeichen ➡ „Mein Kapital“ · ⏰ du wählst die Uhrzeit der Losung (= du lädst den Trigger selbst) · „Morgen: …“-Teaser nach jedem Lesetag.",
            "Here: 🖍️ highlights, ✍️ notes, 🔖 bookmarks ➡ “My Capital” · ⏰ you pick the verse time (= you load the trigger yourself) · “Tomorrow: …” teaser after every reading day."),
        "mine", Color(0xFFE1BEE7),
    ),
)

private val EXTRAS = listOf(
    T("🎟️ Endowed Progress (Kap. 3)", "🎟️ Endowed progress (ch. 3)") to
        T("Autowaschkarten: 8 leere Felder ⬄ 10 Felder mit 2 Gratis-Stempeln. Gleiche 8 Wäschen, aber 82 % mehr volle Karten. Darum schenkt dir die Stempelkarte 2 Stempel, und die Vorworte + Kap. 8 („Arbeitstag — kennst du schon“) gelten als gelesen.",
            "Car-wash cards: 8 blank fields ⬄ 10 fields with 2 free stamps. The same 8 washes, but 82 % more full cards. That's why the stamp card gives you 2 stamps, and the prefaces + ch. 8 (“working day — you know that one”) count as read."),
    T("🕊️ Sabbat-Joker (Kap. 4: Autonomie)", "🕊️ Sabbath joker (ch. 4: autonomy)") to
        T("Wer sich gegängelt fühlt, rebelliert (Reaktanz). Streaks wirken, aber Kettenangst macht aus Lust Pflicht — Duolingo verkauft dafür „Streak Freeze“. Hier gratis: 1 Ruhetag pro Woche. „Am siebten Tage sollst du ruhen“ — und Kap. 8 kämpft für den Normalarbeitstag.",
            "Whoever feels bossed around rebels (reactance). Streaks work, but chain anxiety turns desire into duty — Duolingo sells “streak freezes” for that. Here it's free: 1 rest day per week. “On the seventh day thou shalt rest” — and ch. 8 fights for the normal working day."),
    T("🔮 Forer-Effekt", "🔮 Forer effect") to
        T("Vage Aussagen fühlen sich persönlich an — Horoskope, Glückskekse, Bibelverse. Das Marxoskop wählt zufällig aus passenden Tags. Es fühlt sich trotzdem an, als hätte Marx dich gemeint.",
            "Vague statements feel personal — horoscopes, fortune cookies, Bible verses. The Marxoscope picks at random from matching tags. It still feels like Marx meant you."),
    T("🧭 Manipulationsmatrix (Kap. 6)", "🧭 Manipulation matrix (ch. 6)") to
        T("Nutzt du dein Produkt selbst? Verbessert es Leben? Ja/Ja = Facilitator · nur Spaß = Entertainer · nur verkaufen = Peddler · weder noch = Dealer. Diese App: ehrlich gesagt ein Entertainer mit Bildungsauftrag. strix benutzt sie selbst. Ob sie dein Leben verbessert, entscheidest du — darum legt sie ihre Tricks offen.",
            "Do you use your product yourself? Does it improve lives? Yes/yes = facilitator · just fun = entertainer · just selling = peddler · neither = dealer. This app: honestly an entertainer with an educational mission. strix uses it. Whether it improves your life is up to you — that's why it shows its tricks."),
    T("🧪 Habit-Test (Kap. 8)", "🧪 Habit testing (ch. 8)") to
        T("Identifizieren ➡ Kodifizieren ➡ Modifizieren: Wie oft sollte man die App nutzen? Wer tut es wirklich (Faustregel: 5 % Gewohnheitsnutzer)? Welchen „Habit Path“ gehen die Treuen — und wie schubst man Neue auf denselben Weg?",
            "Identify ➡ codify ➡ modify: how often should people use the app? Who actually does (rule of thumb: 5 % habitual users)? Which “habit path” do the loyal ones walk — and how do you nudge newcomers onto it?"),
    T("❓ Die 5 Fragen für jeden Hook", "❓ The 5 questions for any hook") to
        T("1. Welchen Schmerz lindert es? (interner Trigger) · 2. Was bringt Leute her? (externer Trigger) · 3. Was ist die einfachste Handlung? · 4. Befriedigt die Belohnung und lässt trotzdem Lust auf mehr? · 5. Welche kleine Arbeit speichert Wert und lädt den nächsten Trigger?",
            "1. What pain does it relieve? (internal trigger) · 2. What brings people here? (external trigger) · 3. What is the simplest action? · 4. Does the reward satisfy yet leave you wanting more? · 5. What bit of work stores value and loads the next trigger?"),
)

@Composable
fun HookedScreen(go: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(tr("🪝 Hooked — wie dich diese App anfixt", "🪝 Hooked — how this app gets you hooked"), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Gap(6)
        Text(tr("Nir Eyal, „Hooked“ (2014): Wie Produkte Gewohnheiten erzeugen. Kapitel 7 seziert die Bibel-App (YouVersion, 100 Mio. Installationen). Diese App klaut jeden Trick — und sagt dir hier, wo.",
            "Nir Eyal, “Hooked” (2014): how products create habits. Chapter 7 dissects the Bible App (YouVersion, 100 M installs). This app steals every trick — and tells you where."),
            fontSize = 14.sp, lineHeight = 20.sp)
        Gap()
        // th loop
        Text(tr("⚡ Trigger ➡ 👆 Aktion ➡ 🎁 Belohnung ➡ 💼 Investment ➡ ⚡ …", "⚡ Trigger ➡ 👆 Action ➡ 🎁 Reward ➡ 💼 Investment ➡ ⚡ …"),
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF1B8)).padding(12.dp))
        Text(tr("Genug Runden ➡ der externe Trigger wird überflüssig, du kommst von selbst. Das ist das Ziel. Von Eyal. Nicht unbedingt von dir.",
            "Enough laps ➡ the external trigger becomes unnecessary, you come back on your own. That's the goal. Eyal's goal. Not necessarily yours."),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
        Gap()

        PHASES.forEach { p ->
            Card(colors = CardDefaults.cardColors(containerColor = p.color), modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(tr(p.title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Gap(4)
                    Text(tr(p.what), fontSize = 14.sp, lineHeight = 19.sp)
                    Gap(6)
                    Text("📖 " + tr(p.bible), fontSize = 13.sp, lineHeight = 18.sp, color = Blue)
                    Gap(6)
                    Text("☭ " + tr(p.here), fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                    Row { TextButton(onClick = { go(p.dest) }) { Text(tr("Zeig's mir ➡", "Show me ➡")) } }
                }
            }
        }
        Gap()
        Text(tr("Weitere Tricks", "More tricks"), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        EXTRAS.forEach { (title, body) ->
            Gap(8)
            Text(tr(title), fontWeight = FontWeight.Bold)
            Text(tr(body), fontSize = 13.sp, lineHeight = 18.sp)
        }
        Gap()
        Text(tr("💡 Im Hooked-Modus klebt an jedem Trick ein gelber 🪝-Zettel. Antippen ➡ du landest hier.",
            "💡 In Hooked mode every trick wears a yellow 🪝 note. Tap it ➡ you land here."),
            fontSize = 13.sp, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFF1B8)).padding(10.dp))
        Gap(40)
    }
}
