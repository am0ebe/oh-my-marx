package de.strix.marxversion.logic

import de.strix.marxversion.data.T
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 🎉 Feiertage = Sondermotiv: own card colours + emoji + greeting + a fitting verse (overrides th Losung).
 * Hooked ch7: YouVersion feared th "Merry Christmas" push ➡ ppl screenshotted + shared it instead.
 * colors = 4-stop card gradient (top ➡ bottom) · verse = Kapital id
 */
enum class Feast(val emoji: String, val greet: Pair<String, String>, val verse: String, val colors: IntArray) {
    NEUJAHR("🎆", T("Frohes neues Jahr! Guter Vorsatz: mehr akkumulieren — Lesekapital, versteht sich.",
        "Happy New Year! Resolution: accumulate more — reading capital, of course."), "22,56",
        c(0xFF0B1026, 0xFF2B1B4F, 0xFF6A3D9A, 0xFFE8B931)),
    VALENTIN("💘", T("Valentinstag: Die Ware liebt das Geld. Du liebst hoffentlich jemanden ohne Preisschild.",
        "Valentine's Day: the commodity loves money. Hopefully you love someone without a price tag."), "3,33",
        c(0xFF5B0E2D, 0xFFB3261E, 0xFFF06292, 0xFFFFD1DC)),
    FRAUENTAG("💐", T("Internationaler Frauentag — Kapitel 13 kannte die Doppelschicht schon.",
        "International Women's Day — chapter 13 already knew the double shift."), "13,211",
        c(0xFF4A148C, 0xFF8E24AA, 0xFFCE93D8, 0xFFFFF3E0)),
    TODESTAG("🕯️", T("14. März 1883: Marx stirbt in London. Ein Vers als Grablicht.",
        "14 March 1883: Marx dies in London. A verse as a grave candle."), "24,111",
        c(0xFF000000, 0xFF263238, 0xFF546E7A, 0xFFB0BEC5)),
    OSTERN("🐣", T("Frohe Ostern! Auferstehung, Kapital-Edition: Der Tote packt den Lebenden.",
        "Happy Easter! Resurrection, Capital edition: the dead seize the living."), "V1,8",
        c(0xFF1B5E20, 0xFF7CB342, 0xFFFFF176, 0xFFFFFDE7)),
    ERSTER_MAI("✊", T("1. Mai — Tag der Arbeit. Den Normalarbeitstag gab's nicht geschenkt (Kap. 8).",
        "May Day. The normal working day was not a gift (ch. 8)."), "8,197",
        c(0xFF7F0000, 0xFFB3261E, 0xFFE53935, 0xFFFFCDD2)),
    GEBURTSTAG("🎂", T("Marx hat Geburtstag (*1818 in Trier)! Heute tanzen sogar die Tische.",
        "Happy birthday, Marx (born 1818, Trier)! Even the tables dance today."), "1,124",
        c(0xFF1F2F4F, 0xFFB3261E, 0xFFE8B931, 0xFFFFF8E1)),
    KAPITAL("📕", T("14. September 1867: Das Kapital erscheint. Der erste Satz, wie am ersten Tag.",
        "14 September 1867: Capital is published. The first sentence, as on day one."), "1,1",
        c(0xFF3E2723, 0xFF6D4C41, 0xFFB3261E, 0xFFF6EEDC)),
    HALLOWEEN("🎃", T("Halloween — das gruseligste Monster im Kapital ist kein Geist, sondern ein Vampir.",
        "Halloween — the scariest monster in Capital is not a ghost but a vampire."), "8,9",
        c(0xFF0D0D0D, 0xFF3E1F47, 0xFFE65100, 0xFFFFA726)),
    BLACK_FRIDAY("🛍️", T("Black Friday — Fetisch-Detox: erst lesen, dann (vielleicht) kaufen.",
        "Black Friday — fetish detox: read first, then (maybe) buy."), "1,128",
        c(0xFF000000, 0xFF212121, 0xFF424242, 0xFFE8B931)),
    WEIHNACHTEN("🎄", T("Frohe Weihnachten! Auch Marx kennt die Dreifaltigkeit: Gott Vater, Gott Sohn — und der Mehrwert.",
        "Merry Christmas! Marx has a trinity too: God the Father, God the Son — and surplus value."), "4,22",
        c(0xFF0B3D20, 0xFF1B5E20, 0xFFB3261E, 0xFFE8B931)),
    SILVESTER("🥂", T("Silvester — feier schön. Das Kapital tut's auch.",
        "New Year's Eve — party well. Capital does too."), "8,128",
        c(0xFF0B1026, 0xFF1A237E, 0xFF5C6BC0, 0xFFE8B931));

    companion object {
        fun on(d: LocalDate): Feast? {
            val md = d.monthValue * 100 + d.dayOfMonth
            return when {
                md == 101 -> NEUJAHR
                md == 214 -> VALENTIN
                md == 308 -> FRAUENTAG
                md == 314 -> TODESTAG
                d == easter(d.year) || d == easter(d.year).plusDays(1) -> OSTERN
                md == 501 -> ERSTER_MAI
                md == 505 -> GEBURTSTAG
                md == 914 -> KAPITAL
                md == 1031 -> HALLOWEEN
                d == blackFriday(d.year) -> BLACK_FRIDAY
                md in 1224..1226 -> WEIHNACHTEN
                md == 1231 -> SILVESTER
                else -> null
            }
        }

        /** Gauß/Meeus — Gregorian Easter Sunday */
        fun easter(y: Int): LocalDate {
            val a = y % 19; val b = y / 100; val c = y % 100
            val d = b / 4; val e = b % 4; val f = (b + 8) / 25; val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4; val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(y, month, day)
        }

        /** day aft th 4th Thursday o November */
        fun blackFriday(y: Int): LocalDate =
            LocalDate.of(y, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)).plusDays(1)
    }
}

/**
 * 🌅 Tageszeit-Farben (à la Insight Timer): morning / day / evening / night ➡ gradient + greeting.
 * Hooked ch2: glue th habit 2 a routine u alr have (coffee, lunch break, clocking out).
 */
enum class DayTime(val greet: Pair<String, String>, val colors: IntArray) {
    MORNING(T("Guten Morgen ☀️ — ein Vers vor der Schicht.", "Good morning ☀️ — one verse before the shift."),
        c(0xFF6A3093, 0xFFD6577A, 0xFFF29E4C)),
    DAY(T("Mahlzeit 🍞 — die Pause gehört dir, nicht dem Kapital.", "Lunch break 🍞 — the pause is yours, not capital's."),
        c(0xFF0D47A1, 0xFF1E88E5, 0xFF4FC3F7)),
    EVENING(T("Feierabend 🌆 — jetzt ist deine Zeit.", "Clocked out 🌆 — now it's your time."),
        c(0xFF2B1B4F, 0xFFB3261E, 0xFFF29E4C)),
    NIGHT(T("Gute Nacht 🌙 — das Kapital schläft nie. Du schon.", "Good night 🌙 — capital never sleeps. You do."),
        c(0xFF03061A, 0xFF0B1026, 0xFF1F2F4F));

    companion object {
        fun at(hour: Int) = when (hour) {
            in 5..10 -> MORNING
            in 11..16 -> DAY
            in 17..21 -> EVENING
            else -> NIGHT
        }
    }
}

private fun c(vararg argb: Long) = IntArray(argb.size) { argb[it].toInt() }
