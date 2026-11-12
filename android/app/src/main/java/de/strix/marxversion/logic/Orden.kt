package de.strix.marxversion.logic

import de.strix.marxversion.data.T
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 🎖️ Orden = badges (@1107, à la Tractive's medals, strix: "Badges all yes!"), Kapital-themed reading-room badges (@1112 tone brief: no real-socialism medals, wink not authoritarian).
 * Hooked ch4: reward of th self (mastery) + goal gradient ("3 more days 2 th next one") · Hooked mode says so (Hook.ORDEN).
 * Pure: evythg is computed fr Stats ➡ no DB table, nothing 2 migrate · th app only remembers which ones it alr announced.
 */
data class Stats(
    val dates: Set<LocalDate> = emptySet(),        // reading days (plan day or Amen)
    val streak: Int = 0,                           // current, w Sabbat joker
    val best: Int = 0,                             // longest ever
    val chaptersDone: Set<String> = emptySet(),    // kap ids w evy paragraph read
    val bookDone: Boolean = false,
    val plansDone: Int = 0,
    val marks: Int = 0,                            // highlights + notes + bookmarks
    val shares: Int = 0,
    val kreise: Int = 0,
    val flags: Set<String> = emptySet(),           // "night" · "founder" · "invite" · "krise" · "sabbath"
)

enum class OrdenGroup(val label: Pair<String, String>) {
    READ(T("Lesen", "Reading")), BOOK(T("Das Buch", "The book")), SOCIAL(T("Gemeinsam", "Together")), SECRET(T("Geheim", "Secret")),
}

enum class Orden(
    val emoji: String, val title: Pair<String, String>, val how: Pair<String, String>,
    val group: OrdenGroup, val hidden: Boolean = false, val earned: (Stats) -> Boolean,
) {
    // 📖 reading days
    FIRST("🌱", T("Erster Mehrwert", "First surplus value"), T("Dein erster Lesetag.", "Your first reading day."),
        OrdenGroup.READ, earned = { it.dates.isNotEmpty() }),
    WEEK("🗓️", T("Siebentagewoche", "Seven-day week"), T("7 Tage in Folge gelesen.", "Read 7 days in a row."),
        OrdenGroup.READ, earned = { it.best >= 7 }),
    DAYS30("📅", T("Monatslohn", "Monthly wage"), T("30 Lesetage insgesamt.", "30 reading days in total."),
        OrdenGroup.READ, earned = { it.dates.size >= 30 }),
    STREAK30("🔥", T("Nonstop-Betrieb", "Non-stop operation"), T("30 Tage in Folge gelesen.", "Read 30 days in a row."),
        OrdenGroup.READ, earned = { it.best >= 30 }),
    DAYS100("🏅", T("Bücherwurm", "Bookworm"), T("100 Lesetage insgesamt.", "100 reading days in total."),
        OrdenGroup.READ, earned = { it.dates.size >= 100 }),
    DAYS365("🏆", T("Jahresmehrwert", "Annual surplus"), T("365 Lesetage. Ein ganzes Jahr Mehrarbeit — für dich.", "365 reading days. A whole year of surplus labour — for yourself."),
        OrdenGroup.READ, earned = { it.dates.size >= 365 }),

    // 📕 th book
    CH1("💎", T("Wertform-Veteran", "Value-form veteran"), T("Kapitel 1 komplett gelesen. Respekt.", "Read all of chapter 1. Respect."),
        OrdenGroup.BOOK, earned = { "1" in it.chaptersDone }),
    CH8("⏱️", T("Normalarbeitstag", "The normal working day"), T("Kapitel 8 komplett: der Arbeitstag.", "All of chapter 8: the working day."),
        OrdenGroup.BOOK, earned = { "8" in it.chaptersDone }),
    CH24("🩸", T("Blut- und schmutztriefend", "Dripping with blood and dirt"), T("Kapitel 24 komplett: die ursprüngliche Akkumulation.", "All of chapter 24: so-called primitive accumulation."),
        OrdenGroup.BOOK, earned = { "24" in it.chaptersDone }),
    PLAN("📋", T("Leseplan gemeistert", "Reading plan mastered"), T("Einen Leseplan bis zum letzten Tag geschafft.", "Finished a reading plan to its last day."),
        OrdenGroup.BOOK, earned = { it.plansDone >= 1 }),
    MARKS("🖍️", T("Mein Kapital wächst", "My capital grows"), T("10 Markierungen, Notizen oder Lesezeichen.", "10 highlights, notes or bookmarks."),
        OrdenGroup.BOOK, earned = { it.marks >= 10 }),
    BOOK("📕", T("Hat's wirklich gelesen", "Actually read it"), T("Das ganze Kapital, Band 1. Jeder Absatz.", "All of Capital, vol. 1. Every paragraph."),
        OrdenGroup.BOOK, earned = { it.bookDone }),

    // 👥 comrades
    SHARE1("📣", T("Spruchbote", "Verse messenger"), T("Deine erste Spruchkarte geteilt.", "Shared your first verse card."),
        OrdenGroup.SOCIAL, earned = { it.shares >= 1 }),
    SHARE10("📢", T("Druckerpresse", "Printing press"), T("10 Spruchkarten geteilt.", "Shared 10 verse cards."),
        OrdenGroup.SOCIAL, earned = { it.shares >= 10 }),
    KREIS("👥", T("Mitleser:in", "Fellow reader"), T("In einem Lesekreis.", "In a reading circle."),
        OrdenGroup.SOCIAL, earned = { it.kreise >= 1 }),
    FOUNDER("🏛️", T("Kreisgründer:in", "Circle founder"), T("Einen Lesekreis gegründet.", "Founded a reading circle."),
        OrdenGroup.SOCIAL, earned = { "founder" in it.flags }),
    INVITE("✉️", T("Gastgeber:in", "Host"), T("Jemanden in deinen Lesekreis eingeladen.", "Invited someone to your reading circle."),
        OrdenGroup.SOCIAL, earned = { "invite" in it.flags }),

    // 🥚 secret: shown as ? until earned
    MAYDAY("🌹", T("Erster Mai", "May Day"), T("Am 1. Mai gelesen.", "Read on May 1st."),
        OrdenGroup.SECRET, hidden = true, earned = { s -> s.dates.any { it.monthValue == 5 && it.dayOfMonth == 1 } }),
    BIRTHDAY("🎂", T("Karls Geburtstag", "Karl's birthday"), T("Am 5. Mai gelesen (*1818).", "Read on May 5th (*1818)."),
        OrdenGroup.SECRET, hidden = true, earned = { s -> s.dates.any { it.monthValue == 5 && it.dayOfMonth == 5 } }),
    FRIDAY("🛍️", T("Fetisch-Detox", "Fetish detox"), T("Am Black Friday gelesen statt geshoppt.", "Read on Black Friday instead of shopping."),
        OrdenGroup.SECRET, hidden = true, earned = { s -> s.dates.any { it == blackFriday(it.year) } }),
    NIGHT("🦉", T("Nachtschicht", "Night shift"), T("Zwischen Mitternacht und 4 Uhr gelesen.", "Read between midnight and 4 am."),
        OrdenGroup.SECRET, hidden = true, earned = { "night" in it.flags }),
    SABBATH("🕊️", T("Sabbat gehalten", "Kept the Sabbath"), T("Den Sabbat-Joker eingelöst — Ruhe gehört dazu.", "Used the Sabbath joker — rest is part of the work."),
        OrdenGroup.SECRET, hidden = true, earned = { "sabbath" in it.flags }),
    KRISE("📉", T("Krisenfest", "Crisis-proof"), T("Das Handy 15× geschüttelt — du hast eine ganze Krise ausgelöst.", "Shook the phone 15× — you caused a whole crisis."),
        OrdenGroup.SECRET, hidden = true, earned = { s -> s.flags.count { it.startsWith("krise") } >= KRISE_SHAKES });

    companion object {
        /** shakes 4 Krisenfest (@1112: 1 shake was way 2 easy) · each shake = flag "krise:<ms>" (old "krise" = 1 shake) */
        const val KRISE_SHAKES = 15
        fun earned(s: Stats): Set<Orden> = entries.filter { it.earned(s) }.toSet()
    }
}

/** 4th Thursday o November + 1 */
fun blackFriday(year: Int): LocalDate =
    LocalDate.of(year, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY)).plusDays(1)

/** longest run o consecutive reading days ever (plain days, no joker ➡ honest) */
fun bestStreak(dates: Set<LocalDate>): Int {
    var best = 0
    for (d in dates) if (d.minusDays(1) !in dates) {   // start o a run
        var n = 1; var x = d.plusDays(1)
        while (x in dates) { n++; x = x.plusDays(1) }
        best = maxOf(best, n)
    }
    return best
}
