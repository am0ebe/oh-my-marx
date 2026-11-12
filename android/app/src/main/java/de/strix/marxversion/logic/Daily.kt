package de.strix.marxversion.logic

import de.strix.marxversion.data.DayLog
import de.strix.marxversion.data.Verse
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlin.random.Random

// pure functions, no Android ➡ ez 2 reason about (+ unit-testable later)

fun today(): String = LocalDate.now().toString()

/** ⭐ verses (best ~1/6 per section, picked by bin/rank.py) = th "Vers des Tages" pool */
fun dailyPool(all: List<Verse>) = all.filter { it.star && it.standalone >= 3 }
    .ifEmpty { all.filter { it.juice >= 4 && it.quoteDe.isNotBlank() } }

/**
 * Hooked: variable reward. Same verse all day (date-seeded), new one at midnight —
 * th YouVersion usr who stayed up past 00:00 4 her nxt verse would approve.
 */
fun verseOfDay(pool: List<Verse>, date: String = today(), reroll: Int = 0): Verse? =
    if (pool.isEmpty()) null else pool[Random(date.hashCode() + reroll * 7919).nextInt(pool.size)]  // reroll = 🧪 test mode

/** ISO week key 4 th Sabbat-Joker (max 1 rest day / week) */
private fun week(d: LocalDate) = d.get(IsoFields.WEEK_BASED_YEAR) * 100 + d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

/**
 * consecutive reading days, counting back fr today (or yesterday if today not done yet).
 * 🕊️ Sabbat-Joker: 1 missed day per ISO week is bridged (= rest, not counted) — streak anxiety kills habits too.
 * returns (streak, bridged rest days)
 */
fun streakInfo(days: List<DayLog>, now: LocalDate = LocalDate.now()): Pair<Int, Set<LocalDate>> {
    val done = days.map { LocalDate.parse(it.date) }.toSet()
    val rest = mutableSetOf<LocalDate>()
    var d = if (now in done) now else now.minusDays(1)
    var n = 0
    while (true) {
        when {
            d in done -> n++
            d.minusDays(1) in done && rest.none { week(it) == week(d) } -> rest += d
            else -> break
        }
        d = d.minusDays(1)
    }
    return n to rest
}

fun streak(days: List<DayLog>, now: LocalDate = LocalDate.now()) = streakInfo(days, now).first

enum class Cell { DONE, REST, EMPTY }

/** last 14 days 4 th "don't break th chain" row: ☭ read · 🕊️ Sabbat · · nothing */
fun chain(days: List<DayLog>, now: LocalDate = LocalDate.now()): List<Cell> {
    val done = days.map { it.date }.toSet()
    val rest = streakInfo(days, now).second
    return (13 downTo 0).map { now.minusDays(it.toLong()) }.map {
        when { it.toString() in done -> Cell.DONE; it in rest -> Cell.REST; else -> Cell.EMPTY }
    }
}

/**
 * 🎟️ Stempelkarte = endowed progress (Hooked ch3, car-wash study): 10 fields, th 1st card comes w 2 gift stamps
 * ➡ 8 real days 2 fill it, same as a blank 8-card — but 82 % more ppl finish. Evy full card = 1 rank up.
 * Counts *total* reading days (not consecutive) ➡ a broken streak never empties th card.
 */
const val CARD = 10
const val GIFT = 2

data class StampCard(val no: Int, val filled: Int, val gift: Int) {
    val left get() = CARD - filled
}

fun stampCard(totalDays: Int): StampCard =
    if (totalDays < CARD - GIFT) StampCard(1, totalDays + GIFT, GIFT)
    else (totalDays - (CARD - GIFT)).let { r -> StampCard(2 + r / CARD, r % CARD, 0) }

/** 📖 plan day 2 show today: today's logged day · else last done + 1 · null = plan finished (nvr repeat th last page) */
fun planIndex(done: List<DayLog>, planSize: Int, now: String = today()): Int? {
    done.firstOrNull { it.date == now }?.let { return it.planDay.coerceIn(0, planSize - 1) }
    val next = (done.maxOfOrNull { it.planDay } ?: -1) + 1
    return if (next >= planSize) null else next
}

/** distinct days w any reading (plan day or Losung "Amen") */
fun readingDays(days: List<DayLog>) = days.map { it.date }.distinct().size

/** full cards so far = rank (0 = none yet) */
fun cardsDone(totalDays: Int) = if (totalDays < CARD - GIFT) 0 else 1 + (totalDays - (CARD - GIFT)) / CARD

/** true if th day just added completed a card (➡ celebrate rank up) */
fun cardJustFilled(totalDays: Int) = totalDays >= CARD - GIFT && stampCard(totalDays).filled == 0

/** "Was nervt?" chips ➡ verse tags (Forer effect: vague enough 2 feel personal = "Marxoskop") */
val NERVT = linkedMapOf(
    "chef" to listOf("kapitalist", "ausbeutung"),
    "ueberstunden" to listOf("arbeitszeit"),
    "lohn" to listOf("lohn", "geld"),
    "miete" to listOf("wohnen", "gesundheit"),
    "konsum" to listOf("ware", "konsum"),
    "ki" to listOf("maschine", "fabrik"),
    "krise" to listOf("krise", "arbeitslosigkeit"),
)

/** truly random each tap = hunt reward */
fun marxoskop(all: List<Verse>, key: String): Verse? {
    val tags = NERVT[key] ?: return null
    return all.filter { v -> v.juice >= 3 && v.quoteDe.isNotBlank() && v.tags.any { it in tags } }.randomOrNull()
}

/**
 * Endowed progress (Hooked ch3, car-wash cards: 2 free stamps ➡ +82% completion).
 * Vorworte "geschenkt" + Kap 8 Arbeitstag "gelebt — du arbeitest ja schon".
 */
val ENDOWED = setOf("V", "8")

/** chapter counts as done at ≥ 90% verses read · returns kap ➡ done */
fun chapterDone(all: List<Verse>, readIds: Set<String>): Map<String, Boolean> =
    all.groupBy { if (it.kap.startsWith("V")) "V" else it.kap }.mapValues { (kap, vs) ->
        kap in ENDOWED || vs.count { it.id in readIds } >= vs.size * 0.9
    }
