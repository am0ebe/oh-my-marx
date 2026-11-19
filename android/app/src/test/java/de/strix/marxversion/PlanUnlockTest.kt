package de.strix.marxversion

import de.strix.marxversion.data.Plan
import de.strix.marxversion.data.PlanDay
import de.strix.marxversion.data.PlanLock
import de.strix.marxversion.logic.LUCKY_ODDS
import de.strix.marxversion.logic.UnlockStats
import de.strix.marxversion.logic.eventOpens
import de.strix.marxversion.logic.isOpen
import de.strix.marxversion.logic.plansDone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** 🔢 plan versioning (D-042) + 🔒 progressive unlock (D-043): logic/PlanUnlock.kt · data/Content.kt Plan.daysFor */
class PlanUnlockTest {
    private fun day(vararg ids: String) = PlanDay(ids.toList())
    private fun plan(id: String, lock: PlanLock? = null, ver: Int = 1, days: List<PlanDay> = listOf(day("1,1")),
                     old: Map<String, List<PlanDay>> = emptyMap()) = Plan(id, "📕", id, id, "", "", days, ver, old, lock)

    @Test fun daysFor_pinsTheCutUStartedOn() {
        val v1 = listOf(day("1,1"), day("1,2"), day("1,3"))
        val v2 = listOf(day("1,1", "1,2"), day("1,3"))
        val p = plan("wf", ver = 2, days = v2, old = mapOf("1" to v1))
        assertSame(v1, p.daysFor(null))   // started b4 versioning = v1
        assertSame(v1, p.daysFor(1))
        assertSame(v2, p.daysFor(2))
        assertSame(v2, p.daysFor(9))      // unknown ➡ current
    }

    @Test fun noLock_alwaysOpen_locked_opensOnAnyCondition() {
        assertTrue(isOpen(plan("a"), UnlockStats(), emptySet(), dev = false))
        val p = plan("b", PlanLock(plans = 1, streak = 20))
        assertFalse(isOpen(p, UnlockStats(plansDone = 0, best = 19), emptySet(), dev = false))
        assertTrue(isOpen(p, UnlockStats(plansDone = 1), emptySet(), dev = false))
        assertTrue(isOpen(p, UnlockStats(best = 20), emptySet(), dev = false))   // best ever ➡ a broken chain nvr re-locks
        assertTrue(isOpen(p, UnlockStats(), setOf("b"), dev = false))            // opened by an event
        assertTrue(isOpen(p, UnlockStats(), emptySet(), dev = true))             // DEV switch
    }

    @Test fun christmasGift_opensOnlyOnChristmas() {
        val p = plan("x", PlanLock(feast = "WEIHNACHTEN"))
        assertEquals(listOf("x"), eventOpens(listOf(p), LocalDate.of(2026, 12, 24), 0, emptySet(), amenDay = false))
        assertEquals(emptyList<String>(), eventOpens(listOf(p), LocalDate.of(2026, 12, 20), 0, emptySet(), amenDay = false))
        assertEquals(emptyList<String>(), eventOpens(listOf(p), LocalDate.of(2026, 12, 24), 0, setOf("x"), amenDay = false))
    }

    @Test fun luckyDraw_onlyOnAmenDays_aftrNReadingDays_sameDaySameResult() {
        val p = plan("l", PlanLock(lucky = 10))
        val d0 = LocalDate.of(2026, 1, 1)
        val days = (0L until 200L).map { d0.plusDays(it) }
        val hits = days.count { eventOpens(listOf(p), it, 10, emptySet(), amenDay = true).isNotEmpty() }
        assertTrue("~1 in $LUCKY_ODDS: $hits / 200", hits in 20..70)
        assertTrue(days.none { eventOpens(listOf(p), it, 9, emptySet(), amenDay = true).isNotEmpty() })     // < 10 reading days
        assertTrue(days.none { eventOpens(listOf(p), it, 50, emptySet(), amenDay = false).isNotEmpty() })   // app open ≠ draw
        days.forEach { assertEquals(eventOpens(listOf(p), it, 10, emptySet(), true), eventOpens(listOf(p), it, 10, emptySet(), true)) }
    }

    @Test fun plansDone_countsAnyCut() {
        val p = plan("wf", ver = 2, days = listOf(day("a"), day("b"), day("c")), old = mapOf("1" to listOf(day("a"), day("b"))))
        assertEquals(1, plansDone(listOf(p), mapOf("wf" to 2)))   // finished v1 (2 days) = done, though v2 hs 3
        assertEquals(0, plansDone(listOf(p), mapOf("wf" to 1)))
    }
}
