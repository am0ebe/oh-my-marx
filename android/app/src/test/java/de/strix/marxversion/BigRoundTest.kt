package de.strix.marxversion

import de.strix.marxversion.data.Plan
import de.strix.marxversion.data.Verse
import de.strix.marxversion.logic.Orden
import de.strix.marxversion.logic.Stats
import de.strix.marxversion.logic.chapterPercent
import de.strix.marxversion.logic.feedStart
import de.strix.marxversion.logic.hookedDaysLeft
import de.strix.marxversion.logic.hookedUnlocked
import de.strix.marxversion.ui.SNIPPET
import de.strix.marxversion.ui.snippet
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** big round (QA r5 · M8 @1113): #3 snippet · #4 feed · #5 Hooked unlock · #6 Ludditen plan */
class BigRoundTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun asset(name: String) = File("src/main/assets/content/$name").readText()
    private val verses by lazy { json.decodeFromString<List<Verse>>(asset("verses.json")) }
    private val plans by lazy { json.decodeFromString<List<Plan>>(asset("plans.json")) }

    // ── #3 snippet + "weiterlesen" ──
    @Test fun shortTextStaysWhole() = assertNull(snippet("Kurz. Und gut."))

    @Test fun longTextCutsAtSentenceEnd() {
        val t = "Erster Satz ist ziemlich lang und erzählt von Waren und Werten und noch mehr. ".repeat(12)
        val s = snippet(t)!!
        assertTrue("≤ max", s.length <= SNIPPET)
        assertTrue("ends on a sentence", s.endsWith("."))
        assertTrue("not trivially short", s.length >= SNIPPET / 2)
    }

    @Test fun noSentenceEndCutsAtWord() {
        val s = snippet("wort ".repeat(200))!!
        assertTrue(s.endsWith(" …") && s.length <= SNIPPET + 2)
    }

    @Test fun planVersesOnTodayGetShorter() {
        val p = plans.first { it.id == "30tage" }
        val walls = p.days.flatMap { it.verseIds }.mapNotNull { id -> verses.firstOrNull { it.id == id } }.filter { it.de.length > 1500 }
        assertTrue("there r walls 2 cut", walls.isNotEmpty())
        walls.forEach { assertTrue(it.id, (snippet(it.de) ?: it.de).length <= SNIPPET) }
    }

    // ── #4 feed ──
    @Test fun feedStartsRightAfterFurthestPassage() {
        val i8 = verses.indexOfFirst { it.id == "8,9" }
        assertEquals(i8 + 1, feedStart(verses, listOf("8,3", "8,9")))
        assertEquals(verses.size, feedStart(verses, listOf("nope")))   // unknown ➡ no feed
        assertEquals(verses.size, feedStart(verses, listOf(verses.last().id)))   // end o th book ➡ no feed
    }

    @Test fun chapterPercentCounts() {
        val ch = verses.filter { it.kap == "11" }
        assertEquals(0, chapterPercent(ch, emptySet()))
        assertEquals(100, chapterPercent(ch, ch.map { it.id }.toSet()))
        assertEquals(0, chapterPercent(emptyList(), emptySet()))
    }

    // ── #5 Hooked mode unlock ──
    @Test fun hookedUnlocksAfter7DaysOrFirstPlan() {
        val d0 = LocalDate.of(2026, 11, 1)
        val days = { n: Int -> (0 until n).map { d0.plusDays(it * 2L) }.toSet() }   // gaps: total days, not a streak
        assertFalse(hookedUnlocked(Stats(dates = days(6))))
        assertTrue(hookedUnlocked(Stats(dates = days(7))))
        assertTrue(hookedUnlocked(Stats(dates = days(1), plansDone = 1)))
        assertTrue(Orden.HOOKED in Orden.earned(Stats(dates = days(7))))
        assertFalse(Orden.HOOKED in Orden.earned(Stats(dates = days(3))))
        assertEquals(4, hookedDaysLeft(3)); assertEquals(0, hookedDaysLeft(12))
    }

    // ── #6 Ludditen plan ──
    @Test fun ludditesPlanIsKap13WithBridges() {
        val p = plans.first { it.id == "ludditen" }
        assertEquals(7, p.days.size)
        val ids = verses.map { it.id }.toSet()
        p.days.forEach { d ->
            d.verseIds.forEach { assertTrue("$it exists + Kap 13", it in ids && it.startsWith("13,")) }
            assertTrue("bridge DE+EN", d.bridgeDe.isNotBlank() && d.bridgeEn.isNotBlank())
        }
        assertTrue("Luddites day", p.days.any { d -> d.verseIds.any { id -> verses.first { it.id == id }.de.contains("Ludditen") } })
        assertEquals("2nd i th list (aft th flagship)", "ludditen", plans[1].id)
    }
}
