package de.strix.marxversion

import de.strix.marxversion.data.DayLog
import de.strix.marxversion.logic.planIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** 📖 reading plan = continuous: nvr th same day 2× */
class PlanTest {
    private fun log(d: String, day: Int) = DayLog(d, "p", day)

    @Test fun startsAtZero() = assertEquals(0, planIndex(emptyList(), 5, "2026-11-01"))

    @Test fun nextDayAdvances() = assertEquals(1, planIndex(listOf(log("2026-11-01", 0)), 5, "2026-11-02"))

    @Test fun todayStaysAfterDone() = assertEquals(1, planIndex(listOf(log("2026-11-01", 0), log("2026-11-02", 1)), 5, "2026-11-02"))

    @Test fun skippedDaysDontRepeat() =
        assertEquals(3, planIndex(listOf(log("2026-11-01", 0), log("2026-11-02", 1), log("2026-11-03", 2)), 5, "2026-11-09"))

    @Test fun finishedPlanNeverRepeatsLastPage() {
        val all = (0..4).map { log("2026-11-0${it + 1}", it) }
        assertNull(planIndex(all, 5, "2026-11-06"))        // old: showed day 5 again + a 2nd "Tag geschafft"
        assertEquals(4, planIndex(all, 5, "2026-11-05"))   // th day it finished stays visible
    }
}
