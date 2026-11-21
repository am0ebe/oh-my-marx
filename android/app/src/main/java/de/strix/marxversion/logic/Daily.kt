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

// ── 🧮 streak game (C1 · D-046 milestones + ✊ Streiktag · D-047 Abschreibung) ──────────────────────────────
// 1 fwd replay o th DayLog = th whole truth (no extra state i Prefs ➡ nothing 2 drift, reinstall-safe)

/** D-046: 7 · 14 · 30 · 50 · 100 · 200 · 365 · then evy +100 (465 · 565 …) */
val MILESTONES = listOf(7, 14, 30, 50, 100, 200, 365)
fun isMilestone(n: Int) = n in MILESTONES || (n > 365 && (n - 365) % 100 == 0)
fun nextMilestone(n: Int): Int = MILESTONES.firstOrNull { it > n } ?: (365 + ((n - 365) / 100 + 1) * 100)

/** ✊ max held · 1 earned per milestone */
const val STRIKES_MAX = 2
/** ✊ earned only on milestone days fr th C1 ship day on ➡ old streaks unchanged (no retro bridges) */
val STRIKES_FROM: LocalDate = LocalDate.of(2026, 11, 20)

/** ★ tier by streak (design § 14.1.3): 0 plain · 1 glow 7+ · 2 ring 14+ · 3 laurel 30+ · 4 stars 50+ · 5 halo 100+ · 6 ribbon 200+ · 7 Orden 365+ */
private val TIER_AT = listOf(0, 7, 14, 30, 50, 100, 200, 365)
fun tierOf(streak: Int): Int = TIER_AT.indexOfLast { streak >= it }
const val TIER_MAX = 7

data class StreakGame(
    val streak: Int = 0,
    val rest: Set<LocalDate> = emptySet(),       // 🕊️ Sabbat days i th current streak
    val struck: Set<LocalDate> = emptySet(),     // ✊ Streiktag days i th current streak
    val strikes: Int = 0,                        // ✊ held now (0..2)
    val tier: Int = 0,                           // ★ shown tier (Abschreibung: falls 1 / missed day, nvr a cliff)
    val best: Int = 0,                           // longest streak ever
    val milestoneToday: Int? = null,             // today's done day hit a milestone (➡ T3 screen)
    val strikeEarnedToday: Boolean = false,
    val strikeUsedLast: LocalDate? = null,       // ✊ last day a Streiktag bridged (➡ "Streiktag eingelöst" copy)
    val depreciating: Boolean = false,           // tier > tierOf(streak) = capital writing itself off (➡ Callout 1×)
) {
    val next get() = nextMilestone(streak)
    val toNext get() = next - streak
}

/**
 * replay day by day fr th 1st log 2 `now`:
 * - done ➡ streak +1 · milestone ➡ +1 ✊ (cap 2) · tier = max(tier, tierOf(streak))
 * - missed (past) ➡ 🕊️ Sabbat 1st (1/ISO week, only if th day b4 was read — as always) ➡ else ✊ if held ➡ else break:
 *   streak 0 · tier −1 per missed day (D-047 c) · ✊ kept (earned, nvr taken back)
 * - today not done yet = not missed (still time)
 */
fun streakGame(days: List<DayLog>, now: LocalDate = LocalDate.now()): StreakGame {
    val done = days.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }.filter { !it.isAfter(now) }.toSortedSet()
    if (done.isEmpty()) return StreakGame()
    var n = 0; var best = 0; var strikes = 0; var tier = 0
    var rest = mutableSetOf<LocalDate>(); var struck = mutableSetOf<LocalDate>()
    var mile: Int? = null; var earned = false; var used: LocalDate? = null
    var d = done.first()
    while (!d.isAfter(now)) {
        when {
            d in done -> {
                n++; best = maxOf(best, n); tier = maxOf(tier, tierOf(n))
                val hit = isMilestone(n)
                if (hit && !d.isBefore(STRIKES_FROM) && strikes < STRIKES_MAX) { strikes++; if (d == now) earned = true }
                if (d == now && hit) mile = n
            }
            d == now -> {}   // today, not read yet ➡ still open
            n > 0 && d.minusDays(1) in done && rest.none { week(it) == week(d) } -> rest += d
            n > 0 && strikes > 0 -> { strikes--; struck += d; used = d }
            // 🕊️ lazy: this week's Sabbat alr spent on an earlier 1-day gap `r` ➡ th break moves 2 r, Sabbat 2 d
            //   (= th old backward walk: th LATEST gap o a week gets th Sabbat ➡ old streaks unchanged)
            n > 0 && d.minusDays(1) in done -> {
                val r = rest.first { week(it) == week(d) }
                n = done.count { it.isAfter(r) && it.isBefore(d) }
                rest = mutableSetOf(d); struck = struck.filterTo(mutableSetOf()) { it.isAfter(r) }
                tier = maxOf(maxOf(0, tier - 1), tierOf(n))
            }
            else -> { if (n > 0) { rest = mutableSetOf(); struck = mutableSetOf() }; n = 0; tier = maxOf(0, tier - 1) }
        }
        d = d.plusDays(1)
    }
    return StreakGame(n, rest, struck, strikes, tier, best, mile, earned, used, tier > tierOf(n))
}

/** consecutive reading days (🕊️ + ✊ bridged, not counted) · returns (streak, bridged rest days) — old API */
fun streakInfo(days: List<DayLog>, now: LocalDate = LocalDate.now()): Pair<Int, Set<LocalDate>> =
    streakGame(days, now).let { it.streak to it.rest }

fun streak(days: List<DayLog>, now: LocalDate = LocalDate.now()) = streakInfo(days, now).first

/** C1 API (design § 14.6): milestone hit by today's reading day, else null */
fun milestoneHit(days: List<DayLog>, now: LocalDate = LocalDate.now()): Int? = streakGame(days, now).milestoneToday
fun starTier(days: List<DayLog>, now: LocalDate = LocalDate.now()): Int = streakGame(days, now).tier
fun strikes(days: List<DayLog>, now: LocalDate = LocalDate.now()): Int = streakGame(days, now).strikes

enum class Cell { DONE, REST, STRIKE, EMPTY }

/** last 14 days 4 th "don't break th chain" row: ☭ read · 🕊️ Sabbat · ✊ Streiktag · · nothing */
fun chain(days: List<DayLog>, now: LocalDate = LocalDate.now()): List<Cell> {
    val done = days.map { it.date }.toSet()
    val g = streakGame(days, now)
    return (13 downTo 0).map { now.minusDays(it.toLong()) }.map {
        when { it.toString() in done -> Cell.DONE; it in g.rest -> Cell.REST; it in g.struck -> Cell.STRIKE; else -> Cell.EMPTY }
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

/** a chip needs ≥ this many quotes, else it repeats itself ➡ loosen 1 step */
const val MARXOSKOP_MIN = 30

/**
 * truly random each tap = hunt reward · pool = quotes ABOUT th chip (retag `tw` weight ≥ 2) · < 30 ➡ weight ≥ 1 ·
 * still < 30 o no retag data yet ➡ th old padded `t` tags (score.py: ~4 per quote, "kapitalist" on 83 %)
 */
fun marxoskop(all: List<Verse>, key: String): Verse? = marxoskopPool(all, key).randomOrNull()

fun marxoskopPool(all: List<Verse>, key: String): List<Verse> {
    val tags = NERVT[key] ?: return emptyList()
    val quoted = all.filter { it.juice >= 3 && it.quoteDe.isNotBlank() }
    for (min in 2 downTo 1) {
        val pool = quoted.filter { v -> tags.any { (v.topics[it] ?: 0) >= min } }
        if (pool.size >= MARXOSKOP_MIN) return pool
    }
    return quoted.filter { v -> v.tags.any { it in tags } }
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
