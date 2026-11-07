package de.strix.marxversion

import de.strix.marxversion.logic.Orden
import de.strix.marxversion.logic.Stats
import de.strix.marxversion.logic.bestStreak
import de.strix.marxversion.logic.blackFriday
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** 🎖️ Orden: pure rules fr Stats */
class OrdenTest {
    private val d0 = LocalDate.of(2026, 3, 2)
    private fun run(n: Int, from: LocalDate = d0) = (0 until n).map { from.plusDays(it.toLong()) }.toSet()

    @Test fun nothingAtStart() = assertTrue(Orden.earned(Stats()).isEmpty())

    @Test fun bestStreakFindsLongestRun() {
        assertEquals(0, bestStreak(emptySet()))
        assertEquals(7, bestStreak(run(3) + run(7, d0.plusDays(10))))
    }

    @Test fun readingMedals() {
        val days = run(30)
        val e = Orden.earned(Stats(dates = days, best = bestStreak(days)))
        assertTrue(Orden.FIRST in e && Orden.WEEK in e && Orden.DAYS30 in e && Orden.STREAK30 in e)
        assertFalse(Orden.DAYS100 in e)
    }

    @Test fun secretDates() {
        assertEquals(LocalDate.of(2026, 11, 27), blackFriday(2026))
        val e = Orden.earned(Stats(dates = setOf(LocalDate.of(2026, 5, 1), blackFriday(2025))))
        assertTrue(Orden.MAYDAY in e && Orden.FRIDAY in e)
        assertFalse(Orden.BIRTHDAY in e)
    }

    @Test fun socialAndFlags() {
        val e = Orden.earned(Stats(shares = 10, kreise = 1, flags = setOf("founder", "night")))
        assertTrue(listOf(Orden.SHARE1, Orden.SHARE10, Orden.KREIS, Orden.FOUNDER, Orden.NIGHT).all { it in e })
        assertFalse(Orden.INVITE in e)
    }

    @Test fun everyMedalHasTextAndUniqueEmojiPerGroup() {
        Orden.entries.forEach { assertTrue(it.title.first.isNotBlank() && it.how.second.isNotBlank()) }
        Orden.entries.groupBy { it.group }.values.forEach { g -> assertEquals(g.size, g.map { it.emoji }.toSet().size) }
    }
}
