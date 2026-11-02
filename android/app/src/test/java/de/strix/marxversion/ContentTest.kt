package de.strix.marxversion

import de.strix.marxversion.data.Chapter
import de.strix.marxversion.data.DayLog
import de.strix.marxversion.data.Note
import de.strix.marxversion.data.Plan
import de.strix.marxversion.data.Verse
import de.strix.marxversion.logic.Cell
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.StampCard
import de.strix.marxversion.logic.cardJustFilled
import de.strix.marxversion.logic.chain
import de.strix.marxversion.logic.stampCard
import de.strix.marxversion.logic.chapterDone
import de.strix.marxversion.logic.dailyPool
import de.strix.marxversion.logic.marxoskop
import de.strix.marxversion.logic.streak
import de.strix.marxversion.logic.verseOfDay
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate

/** plain JVM tests (no phone needed): `./gradlew test` — checks th Python-built JSON matches th Kotlin classes */
class ContentTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun asset(name: String) = File("src/main/assets/content/$name").readText()

    private val verses by lazy { json.decodeFromString<List<Verse>>(asset("verses.json")) }

    @Test fun contentParses() {
        val plans = json.decodeFromString<List<Plan>>(asset("plans.json"))
        val chapters = json.decodeFromString<List<Chapter>>(asset("chapters.json"))
        val notes = json.decodeFromString<List<Note>>(asset("notes.json"))
        assertTrue("verses", verses.size > 1500)
        assertEquals(31, chapters.size)  // 6 Vorworte + 25 Kap
        assertTrue("notes", notes.size > 1000)
        val ids = verses.map { it.id }.toSet()
        plans.forEach { p ->
            assertTrue("${p.id} has days", p.days.isNotEmpty())
            p.days.flatMap { it.verseIds }.forEach { assertTrue("${p.id}: $it exists", it in ids) }
        }
    }

    @Test fun verseOfDayIsStablePerDay() {
        val pool = dailyPool(verses)
        assertTrue(pool.isNotEmpty())
        assertEquals(verseOfDay(pool, "2026-10-28"), verseOfDay(pool, "2026-10-28"))
        assertNotNull(marxoskop(verses, "ueberstunden"))
    }

    @Test fun streakAndChain() {
        val now = LocalDate.parse("2026-10-28")
        val d = { s: String -> DayLog(s, "p", 0) }
        assertEquals(0, streak(emptyList(), now))
        assertEquals(2, streak(listOf(d("2026-10-27"), d("2026-10-26")), now))      // today not done yet ➡ still alive
        // 🕊️ Sabbat-Joker: 25th missing (wk 43) is bridged, not counted
        assertEquals(4, streak(listOf(d("2026-10-28"), d("2026-10-27"), d("2026-10-26"), d("2026-10-24")), now))
        // 2 gaps i th same ISO week (29 + 27, wk 44) ➡ only 1 joker ➡ breaks at 27
        assertEquals(2, streak(listOf(d("2026-10-30"), d("2026-10-28"), d("2026-10-26")), LocalDate.parse("2026-10-30")))
        assertEquals(listOf(Cell.DONE), chain(listOf(d("2026-10-28")), now).takeLast(1))
        assertEquals(Cell.REST, chain(listOf(d("2026-10-28"), d("2026-10-26")), now)[12])
    }

    @Test fun stampCardEndowed() {
        assertEquals(StampCard(1, 2, 2), stampCard(0))   // 2 gift stamps on day 0
        assertEquals(StampCard(1, 9, 2), stampCard(7))
        assertEquals(StampCard(2, 0, 0), stampCard(8))   // card 1 full aft 8 real days
        assertTrue(cardJustFilled(8) && cardJustFilled(18) && !cardJustFilled(9))
    }

    @Test fun feasts() {
        assertEquals(LocalDate.parse("2026-04-05"), Feast.easter(2026))
        assertEquals(LocalDate.parse("2026-11-27"), Feast.blackFriday(2026))
        assertEquals(Feast.HALLOWEEN, Feast.on(LocalDate.parse("2026-10-31")))
        assertEquals(null, Feast.on(LocalDate.parse("2026-11-01")))
        val ids = verses.map { it.id }.toSet()
        Feast.entries.forEach { f ->
            assertTrue("${f.name}: ${f.verse} exists", f.verse in ids)
            assertTrue("${f.name}: has quote", verses.first { it.id == f.verse }.quoteDe.isNotBlank())
        }
    }

    @Test fun endowedProgress() {
        val done = chapterDone(verses, emptySet())
        assertEquals(true, done["V"])
        assertEquals(true, done["8"])  // "schon gelebt"
        assertEquals(2, done.values.count { it })
    }
}
