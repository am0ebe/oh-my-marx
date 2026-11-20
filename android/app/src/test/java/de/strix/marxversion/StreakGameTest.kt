package de.strix.marxversion

import de.strix.marxversion.data.DayLog
import de.strix.marxversion.logic.Cell
import de.strix.marxversion.logic.STRIKES_FROM
import de.strix.marxversion.logic.chain
import de.strix.marxversion.logic.isMilestone
import de.strix.marxversion.logic.milestoneHit
import de.strix.marxversion.logic.nextMilestone
import de.strix.marxversion.logic.streakGame
import de.strix.marxversion.logic.tierOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

/** 🧮 C1 streak game (D-046 milestones + ✊ Streiktag · D-047 Abschreibung) · logic/Daily.kt streakGame */
class StreakGameTest {
    // a Monday aftr STRIKES_FROM ➡ strikes can be earned
    private val mon: LocalDate = STRIKES_FROM.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    private fun logs(vararg d: LocalDate) = d.map { DayLog(it.toString(), "p", 0) }
    private fun run(from: LocalDate, n: Int) = (0 until n).map { from.plusDays(it.toLong()) }

    @Test fun milestones() {
        listOf(7, 14, 30, 50, 100, 200, 365, 465, 565).forEach { assertTrue("$it", isMilestone(it)) }
        listOf(1, 6, 8, 15, 99, 300, 400, 466).forEach { assertFalse("$it", isMilestone(it)) }
        assertEquals(7, nextMilestone(0)); assertEquals(14, nextMilestone(7)); assertEquals(465, nextMilestone(365))
    }

    @Test fun earnAt7And14_cap2() {
        val d = run(mon, 7)
        assertEquals(7, milestoneHit(logs(*d.toTypedArray()), d.last()))
        assertEquals(1, streakGame(logs(*d.toTypedArray()), d.last()).strikes)
        val d14 = run(mon, 14)
        assertEquals(2, streakGame(logs(*d14.toTypedArray()), d14.last()).strikes)
        val d30 = run(mon, 30)
        val g = streakGame(logs(*d30.toTypedArray()), d30.last())
        assertEquals("max 2 held", 2, g.strikes)
        assertEquals(30, g.milestoneToday)
        assertNull("day 31 = no milestone", milestoneHit(logs(*run(mon, 31).toTypedArray()), mon.plusDays(30)))
    }

    @Test fun strikeUsedOnlyAftrSabbat() {
        // 7 days ➡ 1 ✊ · then miss 1 day (Sabbat bridges) ➡ ✊ still held
        val d = run(mon, 7)
        val miss1 = d.last().plusDays(1)
        val back = miss1.plusDays(1)
        var g = streakGame(logs(*(d + back).toTypedArray()), back)
        assertTrue("Sabbat 1st", miss1 in g.rest)
        assertEquals(1, g.strikes)
        assertEquals(8, g.streak)
        // 2-day gap: Sabbat can't (day b4 not read) ➡ ✊ bridges th 2nd o them … needs 2 ➡ break if only 1
        val gap2 = d.last().plusDays(3)
        g = streakGame(logs(*(d + gap2).toTypedArray()), gap2)
        assertEquals("1 ✊ + Sabbat bridge a 2-day gap", 8, g.streak)
        assertEquals(0, g.strikes)
        assertEquals(1, g.struck.size)
        // same week, 2nd miss ➡ Sabbat spent ➡ ✊ used
        val w = run(mon, 7) + mon.plusDays(8) + mon.plusDays(10)
        g = streakGame(logs(*w.toTypedArray()), mon.plusDays(10))
        assertEquals(setOf(mon.plusDays(7)), g.rest)
        assertEquals(setOf(mon.plusDays(9)), g.struck)
        assertEquals(9, g.streak)
        assertEquals(Cell.STRIKE, chain(logs(*w.toTypedArray()), mon.plusDays(10))[13 - 1])
    }

    @Test fun tierFalls1PerDay() {
        val d = run(mon.minusDays(60), 30)   // 30 days ➡ tier 3 · b4 STRIKES_FROM? (no ✊ ➡ clean break)
        val last = d.last()
        val noStrikes = d.all { it.isBefore(STRIKES_FROM) }
        val days = logs(*d.toTypedArray())
        assertEquals(3, streakGame(days, last).tier)
        // Sabbat bridges day +1 · day +2 = 1st unbridged miss (seen fr day +3 on: "today" = still open)
        val g1 = streakGame(days, last.plusDays(2))
        val g2 = streakGame(days, last.plusDays(3))
        val g3 = streakGame(days, last.plusDays(4))
        val g9 = streakGame(days, last.plusDays(9))
        if (noStrikes) {
            assertEquals("today open ➡ not broken yet", 30, g1.streak)
            assertEquals(3, g1.tier)
            assertEquals(0, g2.streak)
            assertEquals(2, g2.tier)
            assertEquals(1, g3.tier)
            assertTrue(g3.depreciating)
            assertEquals("floor 0", 0, g9.tier)
        }
        // read again aftr 1 lost tier ➡ held at 2 (not 0, not 3) til th new streak passes it
        val again = last.plusDays(3)
        val g = streakGame(logs(*(d + again).toTypedArray()), again)
        if (noStrikes) { assertEquals(1, g.streak); assertEquals(2, g.tier) }
    }

    @Test fun tiers() {
        assertEquals(listOf(0, 0, 1, 2, 3, 4, 5, 6, 7), listOf(0, 6, 7, 14, 30, 50, 100, 200, 365).map(::tierOf))
    }

    /** old streaks unchanged: b4 STRIKES_FROM no ✊ ever ➡ same as th old backward walk */
    @Test fun oldStreaksUnchanged() {
        val r = Random(7)
        repeat(300) {
            val end = STRIKES_FROM.minusDays(1)
            val ds = (0 until 60).map { end.minusDays(it.toLong()) }.filter { r.nextInt(10) < 8 }
            val now = end.minusDays(r.nextLong(0, 3))
            val l = logs(*ds.toTypedArray())
            assertEquals(oldStreak(ds.toSet(), now), streakGame(l, now).streak)
        }
    }

    // ⚓ copy o th pre-C1 backward walk (logic/Daily.kt @ e813452)
    private fun week(d: LocalDate) = d.get(IsoFields.WEEK_BASED_YEAR) * 100 + d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    private fun oldStreak(done: Set<LocalDate>, now: LocalDate): Int {
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
        return n
    }
}
