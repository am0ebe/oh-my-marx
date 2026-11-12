package de.strix.marxversion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.T

/**
 * 🧪 Testmodus = th app confesses its own Hooked tricks (Hooked ch6: be a facilitator, not a dealer).
 * "Der Fetischcharakter der Ware" (Kap 1.4): th social relation hides behind a thing. Here: behind a button.
 */
enum class Hook(val txt: Pair<String, String>) {
    SOCIAL(T(
        "🪝 Belohnung des Stammes + Social Proof (Hooked Kap. 4) — „34 lesen mit“ macht dich zum Teil einer Bewegung. Proletarier aller Länder, lest mit!",
        "🪝 Reward of the tribe + social proof (Hooked ch. 4) — “34 reading along” makes you part of a movement. Readers of all countries, unite!",
    )),
    VARIABLE(T(
        "🪝 Variable Belohnung (Hooked Kap. 4) — du weißt nie, welcher Vers kommt. Genau deshalb kommst du wieder. Spielautomat, aber mit Bart.",
        "🪝 Variable reward (Hooked ch. 4) — you never know which verse comes next. That's why you come back. A slot machine, but bearded.",
    )),
    STREAK(T(
        "🪝 Endowed Progress + Kette (Hooked Kap. 3) — dein Streak ist akkumulierte Aufmerksamkeit. Wer hier wohl den Mehrwert einstreicht?",
        "🪝 Endowed progress + chain (Hooked ch. 3) — your streak is accumulated attention. Guess who pockets the surplus value?",
    )),
    ENDOWED(T(
        "🪝 Endowed Progress — 2 Kapitel geschenkt, damit du dich näher am Ziel fühlst (Autowaschkarten-Studie: +82 % Abschluss).",
        "🪝 Endowed progress — 2 chapters for free so you feel closer to the goal (car-wash card study: +82 % completion).",
    )),
    ORDEN(T(
        "🪝 Belohnung des Selbst + Zielgradient (Hooked Kap. 4) — Abzeichen kosten nichts und fühlen sich nach Leistung an. Je näher der nächste, desto schneller liest du. Marx würde sagen: Fetischcharakter der Medaille.",
        "🪝 Reward of the self + goal gradient (Hooked ch. 4) — badges cost nothing and feel like achievement. The closer the next one, the faster you read. Marx would say: the fetish character of the medal.",
    )),
    INVESTMENT(T(
        "🪝 Investment / IKEA-Effekt (Hooked Kap. 5) — jede Markierung macht die App für dich wertvoller. Deine Arbeit, vergegenständlicht.",
        "🪝 Investment / IKEA effect (Hooked ch. 5) — every highlight makes the app more valuable to you. Your labour, objectified.",
    )),
    TRIGGER(T(
        "🪝 Externer Trigger (Hooked Kap. 2) — die tägliche Losung. Ziel: irgendwann brauchst du sie nicht mehr, weil dich der Chef von allein erinnert.",
        "🪝 External trigger (Hooked ch. 2) — the daily verse. Goal: one day you won't need it, because your boss reminds you without any app.",
    )),
    FORER(T(
        "🪝 Forer-Effekt — der Vers ist vage genug, um genau auf dich zu passen. Funktioniert bei Horoskopen, Bibeln und Marx.",
        "🪝 Forer effect — the verse is vague enough to fit you exactly. Works for horoscopes, bibles and Marx.",
    )),
    SHARE(T(
        "🪝 Teilen = Humblebrag (Hooked Kap. 7) — Selbstoffenbarung belohnt das Gehirn. Und wirbt nebenbei gratis für die App.",
        "🪝 Sharing = humblebrag (Hooked ch. 7) — self-disclosure rewards the brain. And advertises the app for free.",
    )),
    NEXT(T(
        "🪝 Nächsten Trigger laden (Hooked Kap. 5) — der Teaser für morgen ist schon gesetzt. Cliffhanger, aber mit Mehrwert.",
        "🪝 Loading the next trigger (Hooked ch. 5) — tomorrow's teaser is already set. A cliffhanger, but with surplus value.",
    )),
    POPUP(T(
        "🪝 Belohnung beim Öffnen (Hooked Kap. 4) — erst das Geschenk, dann die Arbeit. So macht's auch die Bibel-App.",
        "🪝 Reward on open (Hooked ch. 4) — first the gift, then the work. That's what the Bible App does too.",
    )),
    STAMPS(T(
        "🪝 Endowed Progress (Hooked Kap. 3) — 2 Stempel geschenkt. Autowaschkarten-Studie: 10 Felder mit 2 Gratis-Stempeln ➡ 82 % mehr volle Karten als 8 leere Felder. Gleiche Arbeit, anderes Gefühl.",
        "🪝 Endowed progress (Hooked ch. 3) — 2 stamps for free. Car-wash study: 10 fields with 2 free stamps ➡ 82 % more full cards than 8 blank fields. Same work, different feeling.",
    )),
    SABBATH(T(
        "🪝 Autonomie statt Kettenangst (Hooked Kap. 4) — 1 Ruhetag pro Woche bricht die Kette nicht. Wer um seinen Streak zittert, liest aus Pflicht statt aus Lust — und hört irgendwann ganz auf.",
        "🪝 Autonomy instead of chain anxiety (Hooked ch. 4) — 1 rest day per week doesn't break the chain. Whoever trembles for their streak reads out of duty, not desire — and eventually quits.",
    )),
    DAYTIME(T(
        "🪝 Routine-Anker (Hooked Kap. 2) — Farben + Gruß wechseln mit der Tageszeit, passend zum Tagesrhythmus. Die App klebt sich an Kaffee, Mittagspause und Feierabend.",
        "🪝 Routine anchor (Hooked ch. 2) — colours + greeting change with the time of day, to match your day. The app sticks itself to coffee, lunch break and clocking out.",
    )),
    FEAST(T(
        "🪝 Kalender-Belohnung (Hooked Kap. 7) — an Feiertagen ein Sondermotiv. YouVersion hatte Angst vor der „Frohe Weihnachten“-Push. Stattdessen haben die Leute sie gescreenshottet und geteilt.",
        "🪝 Calendar reward (Hooked ch. 7) — a special motif on holidays. YouVersion feared its “Merry Christmas” push. Instead people screenshotted and shared it.",
    )),
    TINY(T(
        "🪝 Fähigkeit vor Motivation (Hooked Kap. 3, B = MAT) — ein einziger Vers zählt schon als Lesetag. Je kleiner die Handlung, desto öfter passiert sie.",
        "🪝 Ability before motivation (Hooked ch. 3, B = MAT) — a single verse already counts as a reading day. The smaller the action, the more often it happens.",
    )),
    WIDGET(T(
        "🪝 Eigener externer Trigger (Hooked Kap. 2) — das Widget sitzt auf dem Homescreen, den du 80× am Tag siehst. Kein Push nötig: der Vers wartet einfach.",
        "🪝 Owned external trigger (Hooked ch. 2) — the widget sits on the home screen you look at 80× a day. No push needed: the verse just waits.",
    )),
}

/** tap a 🪝 note ➡ th Hooked explainer page (set by MainActivity) */
val LocalOpenHooked = staticCompositionLocalOf<() -> Unit> { {} }

@Composable
fun HookNote(hook: Hook, on: Boolean, modifier: Modifier = Modifier) {
    if (!on) return
    val open = LocalOpenHooked.current
    Surface(color = Color(0xFFFFF1B8), shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).clickable { open() }) {
        Text(tr(hook.txt) + "  ➡", fontSize = 12.sp, lineHeight = 15.sp, color = Color(0xFF3A2E00), modifier = Modifier.padding(8.dp))
    }
}
