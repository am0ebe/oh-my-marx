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
/** 🧔 C3 · D-051 a: 1 base head + 1 overlay prop per feast · ⚠️ superseded by 🥚 D-066 (EggRes, bin/eggs.py): no UI draws it now ➡ ✂️ todo */
enum class Head { NONE, PARTY_HAT, SANTA_HAT, CARNATION, PHRYGIAN, HARD_HAT, CANDLE, PUMPKIN, GLASSES, HEART_GLASSES, SUNGLASSES,
    RAINBOW, DOVE, FIST, DYNAMITE, BOOK, PAMPHLET, MATCH, COCKADE, WEAVER_CAP, GLOBE, POCKETS, BAGS, EGG, GUEST }

enum class Feast(val emoji: String, val greet: Pair<String, String>, val verse: String, val colors: IntArray,
                 val head: Head = Head.NONE, val quiet: Boolean = false) {
    NEUJAHR("🎆", T("Frohes neues Jahr! Guter Vorsatz: mehr akkumulieren — Lesekapital, versteht sich.",
        "Happy New Year! Resolution: accumulate more — reading capital, of course."), "22,56",
        c(0xFF0B1026, 0xFF2B1B4F, 0xFF6A3D9A, 0xFFE8B931), Head.PARTY_HAT),
    VALENTIN("💘", T("Valentinstag: Die Ware liebt das Geld. Du liebst hoffentlich jemanden ohne Preisschild.",
        "Valentine's Day: the commodity loves money. Hopefully you love someone without a price tag."), "3,33",
        c(0xFF5B0E2D, 0xFFB3261E, 0xFFF06292, 0xFFFFD1DC), Head.HEART_GLASSES),
    FRAUENTAG("💐", T("Internationaler Frauentag — Kapitel 13 kannte die Doppelschicht schon.",
        "International Women's Day — chapter 13 already knew the double shift."), "13,211",
        c(0xFF4A148C, 0xFF8E24AA, 0xFFCE93D8, 0xFFFFF3E0), Head.GUEST),
    TODESTAG("🕯️", T("14. März 1883: Karl Marx stirbt in London. Ein Vers als Grablicht.",
        "14 March 1883: Karl Marx dies in London. A verse as a grave candle."), "24,111",
        c(0xFF000000, 0xFF263238, 0xFF546E7A, 0xFFB0BEC5), Head.CANDLE, quiet = true),
    OSTERN("🐣", T("Frohe Ostern! Auferstehung, Kapital-Edition: Der Tote packt den Lebenden.",
        "Happy Easter! Resurrection, Capital edition: the dead seize the living."), "V1,8",
        c(0xFF1B5E20, 0xFF7CB342, 0xFFFFF176, 0xFFFFFDE7), Head.EGG),
    ERSTER_MAI("✊", T("1. Mai — Tag der Arbeit. Den Normalarbeitstag gab's nicht geschenkt (Kap. 8).",
        "May Day. The normal working day was not a gift (ch. 8)."), "8,197",
        c(0xFF7F0000, 0xFFB3261E, 0xFFE53935, 0xFFFFCDD2), Head.FIST),
    GEBURTSTAG("🎂", T("Karl Marx hat Geburtstag (*1818 in Trier)! Heute tanzen sogar die Tische.",
        "Happy birthday, Karl Marx (born 1818, Trier)! Even the tables dance today."), "1,124",
        c(0xFF1F2F4F, 0xFFB3261E, 0xFFE8B931, 0xFFFFF8E1), Head.PARTY_HAT),
    KAPITAL("📕", T("14. September 1867: Das Kapital erscheint. Der erste Satz, wie am ersten Tag.",
        "14 September 1867: Capital is published. The first sentence, as on day one."), "1,1",
        c(0xFF3E2723, 0xFF6D4C41, 0xFFB3261E, 0xFFF6EEDC), Head.GLASSES),
    HALLOWEEN("🎃", T("Halloween — das gruseligste Monster im Kapital ist kein Geist, sondern ein Vampir.",
        "Halloween — the scariest monster in Capital is not a ghost but a vampire."), "8,9",
        c(0xFF0D0D0D, 0xFF3E1F47, 0xFFE65100, 0xFFFFA726), Head.PUMPKIN),
    BLACK_FRIDAY("🛍️", T("Black Friday — Fetisch-Detox: erst lesen, dann (vielleicht) kaufen.",
        "Black Friday — fetish detox: read first, then (maybe) buy."), "1,128",
        c(0xFF000000, 0xFF212121, 0xFF424242, 0xFFE8B931), Head.BAGS),
    WEIHNACHTEN("🎄", T("Frohe Weihnachten! Auch Marx kennt die Dreifaltigkeit: Gott Vater, Gott Sohn — und der Mehrwert.",
        "Merry Christmas! Marx has a trinity too: God the Father, God the Son — and surplus value."), "4,22",
        c(0xFF0B3D20, 0xFF1B5E20, 0xFFB3261E, 0xFFE8B931), Head.SANTA_HAT),
    SILVESTER("🥂", T("Silvester — feier schön. Das Kapital tut's auch.",
        "New Year's Eve — party well. Capital does too."), "8,128",
        c(0xFF0B1026, 0xFF1A237E, 0xFF5C6BC0, 0xFFE8B931), Head.DYNAMITE),

    // ── C3 · D-052 a: 18 new (design § 14.2.3) · fills th 5.5 ➡ 14.9 hole · † days = quiet (grey, no confetti) ──
    ROSA_KARL("🌹", T("15. Januar 1919: Rosa Luxemburg und Karl Liebknecht werden ermordet. Eine rote Nelke.",
        "15 January 1919: Rosa Luxemburg and Karl Liebknecht are murdered. A red carnation."), "24,105",
        c(0xFF1A1A1A, 0xFF37474F, 0xFF7F0000, 0xFFB0BEC5), Head.CARNATION, quiet = true),
    MANIFEST("📜", T("21. Februar 1848: Das Kommunistische Manifest erscheint. Erst die Flugschrift, dann das Buch.",
        "21 February 1848: The Communist Manifesto appears. First the pamphlet, then the book."), "V2,11",
        c(0xFF3E2723, 0xFF7F0000, 0xFFB3261E, 0xFFF6EEDC), Head.PAMPHLET),
    KOMMUNE("🧢", T("18. März 1871: Die Pariser Kommune. 72 Tage, die Marx nie wieder losließen.",
        "18 March 1871: The Paris Commune. 72 days Marx never stopped thinking about."), "24,111",
        c(0xFF0D1B4F, 0xFFB3261E, 0xFFE53935, 0xFFFFF3E0), Head.PHRYGIAN),
    WORKERS_MEMORIAL("⛑️", T("28. April — Gedenktag für die bei der Arbeit Getöteten. Kapitel 13 zählt sie schon.",
        "28 April — Workers' Memorial Day. Chapter 13 already counted them."), "8,108",
        c(0xFF1A1A1A, 0xFF37474F, 0xFF607D8B, 0xFFCFD8DC), Head.HARD_HAT, quiet = true),
    BEFREIUNG("🕊️", T("8. Mai 1945 — Tag der Befreiung vom Faschismus. Nie wieder.",
        "8 May 1945 — liberation from fascism. Never again."), "24,84",
        c(0xFF263238, 0xFF455A64, 0xFF7F0000, 0xFFECEFF1), Head.CARNATION, quiet = true),
    KINDERTAG("🪁", T("Kindertag! Kapitel 10 kannte Neunjährige in der Nachtschicht. Heute: Drachen steigen lassen.",
        "Children's Day! Chapter 10 knew nine-year-olds on the night shift. Today: fly a kite."), "8,40",
        c(0xFF0D47A1, 0xFF42A5F5, 0xFFFFEB3B, 0xFFFFFDE7)),
    WEBER("🧵", T("4. Juni 1844: Die schlesischen Weber stehen auf. Marx schrieb darüber — und wir lesen die Leinwand.",
        "4 June 1844: The Silesian weavers rise. Marx wrote about it — and we read the linen."), "3,28",
        c(0xFF33691E, 0xFF8D6E63, 0xFFD7CCC8, 0xFFFFF8E1), Head.WEAVER_CAP),
    LAENGSTER_TAG("😎", T("Längster Tag des Jahres. Der Kapitalist liebt ihn — du hoffentlich auch, aber aus anderen Gründen.",
        "Longest day of the year. The capitalist loves it — hopefully you do too, for other reasons."), "8,8",
        c(0xFFE65100, 0xFFFFA000, 0xFFFFEB3B, 0xFFFFFDE7), Head.SUNGLASSES),
    CSD("🌈", T("Stonewall, 28. Juni 1969 — Solidarität statt Regenbogen-Marketing.",
        "Stonewall, 28 June 1969 — solidarity, not rainbow marketing."), "1,124",
        c(0xFFB3261E, 0xFFFFA000, 0xFF2E7D32, 0xFF3949AB), Head.RAINBOW),
    MATCHGIRLS("🔥", T("5. Juli 1888: Die Streichholz-Arbeiterinnen von London streiken — und gewinnen.",
        "5 July 1888: London's matchgirls strike — and win."), "8,55",
        c(0xFF3E2723, 0xFFB3261E, 0xFFFF7043, 0xFFFFE0B2), Head.MATCH),
    BASTILLE("🇫🇷", T("14. Juli 1789: Die Bourgeoisie macht ihre eigene Revolution. Freiheit, Gleichheit, Eigentum und Bentham.",
        "14 July 1789: The bourgeoisie makes its own revolution. Freedom, Equality, Property and Bentham."), "4,78",
        c(0xFF0D47A1, 0xFF1E88E5, 0xFFFFFFFF, 0xFFE53935), Head.COCKADE),
    ENGELS_TOD("🕯️", T("5. August 1895: Friedrich Engels stirbt. Ohne ihn kein Band 2 und 3.",
        "5 August 1895: Friedrich Engels dies. Without him no volumes 2 and 3."), "V2,5",
        c(0xFF000000, 0xFF263238, 0xFF546E7A, 0xFFB0BEC5), Head.GUEST, quiet = true),
    PETERLOO("🐎", T("16. August 1819: Peterloo — Kavallerie gegen Arbeiter in Manchester.",
        "16 August 1819: Peterloo — cavalry against workers in Manchester."), "15,27",
        c(0xFF1A1A1A, 0xFF3E2723, 0xFF6D4C41, 0xFFD7CCC8), Head.NONE, quiet = true),
    ANTIKRIEG("🕊️", T("1. September — Antikriegstag. Kein Blut für Profite.",
        "1 September — Anti-War Day. No blood for profits."), "V1,10",
        c(0xFF0D47A1, 0xFF64B5F6, 0xFFE3F2FD, 0xFFFFFFFF), Head.DOVE),
    INTERNATIONALE("🌍", T("28. September 1864: Die Erste Internationale. Marx schreibt die Gründungsadresse.",
        "28 September 1864: The First International. Marx writes the inaugural address."), "8,197",
        c(0xFF7F0000, 0xFFB3261E, 0xFF1565C0, 0xFFE3F2FD), Head.GLOBE),
    ARMUT("🪙", T("Tag gegen Armut. Kapitel 23 nennt es den „Lazarusschichten“-Teil der Arbeiterklasse.",
        "Day against poverty. Chapter 23 calls it the \"Lazarus layers\" of the working class."), "23,60",
        c(0xFF3E2723, 0xFF5D4037, 0xFF8D6E63, 0xFFEFEBE9), Head.POCKETS),
    ENGELS_GEB("🤝", T("Friedrich Engels hat Geburtstag (*1820)! Ohne ihn hätte Marx die Miete nie bezahlt.",
        "Happy birthday, Friedrich Engels (born 1820)! Without him Marx would never have paid the rent."), "V4,4",
        c(0xFF1F2F4F, 0xFF1565C0, 0xFFE8B931, 0xFFFFF8E1), Head.GUEST),
    JENNY("🌹", T("2. Dezember 1881: Jenny Marx stirbt. Sie hielt alles am Laufen — auch die Manuskripte.",
        "2 December 1881: Jenny Marx dies. She kept everything running — the manuscripts too."), "V2,7",
        c(0xFF1A1A1A, 0xFF4A148C, 0xFF7B1FA2, 0xFFE1BEE7), Head.GUEST, quiet = true);

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
                md == 115 -> ROSA_KARL
                md == 221 -> MANIFEST
                md == 318 -> KOMMUNE
                md == 428 -> WORKERS_MEMORIAL
                md == 508 -> BEFREIUNG
                md == 601 -> KINDERTAG
                md == 604 -> WEBER
                md == 621 -> LAENGSTER_TAG
                md == 628 -> CSD
                md == 705 -> MATCHGIRLS
                md == 714 -> BASTILLE
                md == 805 -> ENGELS_TOD
                md == 816 -> PETERLOO
                md == 901 -> ANTIKRIEG
                md == 928 -> INTERNATIONALE
                md == 1017 -> ARMUT
                md == 1128 -> ENGELS_GEB
                md == 1202 -> JENNY
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
enum class DayTime(val greet: Pair<String, String>, val colors: IntArray, val more: List<Pair<String, String>> = emptyList()) {
    MORNING(T("Guten Morgen ☀️ — ein Vers vor der Schicht.", "Good morning ☀️ — one verse before the shift."),
        c(0xFF6A3093, 0xFFD6577A, 0xFFF29E4C), listOf(
            T("Kaffee ☕ und Kapital — beides wirkt am besten früh.", "Coffee ☕ and Capital — both work best early."),
            T("Der Wecker gehört dem Chef. Die nächsten 2 Minuten dir. 🌅", "The alarm belongs to the boss. The next 2 minutes are yours. 🌅"),
            T("Morgenandacht 🕯️ — bevor die Arbeitskraft verkauft wird.", "Morning devotion 🕯️ — before the labour-power gets sold."),
        )),
    DAY(T("Mahlzeit 🍞 — die Pause gehört dir, nicht dem Kapital.", "Lunch break 🍞 — the pause is yours, not capital's."),
        c(0xFF0D47A1, 0xFF1E88E5, 0xFF4FC3F7), listOf(
            T("Halbzeit 🥪 — ab jetzt ist alles Mehrarbeit.", "Halftime 🥪 — from here on it's all surplus labour."),
            T("Kurze Pause ☕ — Marx liest sich schneller als ein Meeting.", "Short break ☕ — Marx reads faster than a meeting."),
            T("Mittag 🍲 — zwei Minuten, die dir keiner abrechnet.", "Noon 🍲 — two minutes nobody bills you for."),
        )),
    EVENING(T("Feierabend 🌆 — jetzt ist deine Zeit.", "Clocked out 🌆 — now it's your time."),
        c(0xFF2B1B4F, 0xFFB3261E, 0xFFF29E4C), listOf(
            T("Schicht vorbei 🌆 — die Fabriktore sind zu, das Buch ist offen.", "Shift's over 🌆 — the factory gates are shut, the book is open."),
            T("Abend 🍷 — Zeit, deine Arbeitskraft zu reproduzieren. Und ein Vers.", "Evening 🍷 — time to reproduce your labour-power. And one verse."),
            T("Feierabend 🛋️ — der Mehrwert ist eingestrichen, der Vers ist gratis.", "Off the clock 🛋️ — the surplus value's pocketed, the verse is free."),
        )),
    NIGHT(T("Gute Nacht 🌙 — das Kapital schläft nie. Du schon.", "Good night 🌙 — capital never sleeps. You do."),
        c(0xFF03061A, 0xFF0B1026, 0xFF1F2F4F), listOf(
            T("Nachtschicht? 🌙 Marx schrieb auch bis spät.", "Night shift? 🌙 Marx wrote late too."),
            T("Noch wach 🦉 — ein Vers, dann ab in die Reproduktion.", "Still up 🦉 — one verse, then off to reproduce."),
            T("Mitternacht 🕯️ — ein Gespenst geht um. Lies schnell.", "Midnight 🕯️ — a spectre is haunting. Read fast."),
        ));

    /** greeting changes evy day (same all day ➡ no flicker) · strix @1106: "add some diff comments th change" */
    fun greetOn(day: java.time.LocalDate): Pair<String, String> = (listOf(greet) + more).let { it[day.dayOfYear % it.size] }

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
