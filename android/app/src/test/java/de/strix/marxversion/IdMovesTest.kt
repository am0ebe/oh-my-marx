package de.strix.marxversion

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.data.DayLog
import de.strix.marxversion.data.IdMoves
import de.strix.marxversion.data.Mark
import de.strix.marxversion.data.MarxDb
import de.strix.marxversion.data.Prefs
import de.strix.marxversion.data.ReadLog
import de.strix.marxversion.data.Verse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/** 🔁 FÜFNZEHNTES fix @1113: verse ids moved (14,37 ➡ 15,1 · 15,40 ➡ 15,4 …) ➡ th usr's marks + progress must follow */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)   // plain app: no MarxApp workers
class IdMovesTest {
    private val ctx = ApplicationProvider.getApplicationContext<Application>()
    private val db = Room.inMemoryDatabaseBuilder(ctx, MarxDb::class.java).allowMainThreadQueries().build()
    private val moves = IdMoves.load(ctx)
    @After fun close() = db.close()

    @Test fun assetMapPointsFromDeadIdsToLiveOnes() {
        val ids = Json { ignoreUnknownKeys = true }.decodeFromString<List<Verse>>(File("src/main/assets/content/verses.json").readText()).map { it.id }.toSet()
        assertEquals(30, moves.size)
        assertEquals("15,1", moves["14,37"]); assertEquals("15,4", moves["15,40"]); assertEquals("15,30", moves["15,66"])
        moves.forEach { (old, new) -> assertTrue("$old is gone", old !in ids); assertTrue("$new exists", new in ids) }
        // th text really is th same passage: Kap 15 opens w th value o labour-power
        val de = File("../../data/de.jsonl").readLines().map { Json.parseToJsonElement(it).toString() }
        assertTrue(de.first { "\"id\":\"15,1\"" in it }.contains("Der Wert der Arbeitskraft ist bestimmt"))
    }

    @Test fun marksAndProgressFollowTheirPassage() = runBlocking {
        db.marks().put(Mark("14,37", bookmark = true))
        db.marks().put(Mark("15,40", note = "Lohn ⬄ Mehrwert"))
        db.marks().put(Mark("13,1", highlight = 2))                 // untouched
        listOf("15,41", "14,38", "8,1").forEach { db.logs().read(ReadLog(it)) }
        db.logs().day(DayLog("2026-11-13", "30tage", 4))

        assertEquals(4, IdMoves.apply(db, moves))                  // 2 marks + 2 reads moved
        val marks = db.marks().all().first().associateBy { it.verseId }
        assertEquals(true, marks["15,1"]?.bookmark)
        assertEquals("Lohn ⬄ Mehrwert", marks["15,4"]?.note)
        assertEquals(2, marks["13,1"]?.highlight)
        assertNull(marks["14,37"]); assertNull(marks["15,40"])
        assertEquals(setOf("15,5", "15,2", "8,1"), db.logs().readIds().first().toSet())
        assertEquals(listOf(DayLog("2026-11-13", "30tage", 4)), db.logs().days().first())   // plan progress = indices, untouched
    }

    @Test fun collisionMergesInsteadOfLosing() {
        val there = Mark("15,1", note = "neu", updatedAt = 5)
        val m = IdMoves.merge(Mark("15,1", bookmark = true, highlight = 1, note = "alt", updatedAt = 9), there)
        assertEquals(true, m.bookmark); assertEquals(1, m.highlight); assertEquals("neu\n\nalt", m.note); assertEquals(9, m.updatedAt)
    }

    @Test fun migrateRunsOncePerMovesFile() = runBlocking {
        val prefs = Prefs(ctx)
        prefs.setIdMovesDone(0)
        db.marks().put(Mark("14,39", bookmark = true))
        assertEquals(1, IdMoves.migrate(ctx, db, prefs))
        assertEquals(30, prefs.idMovesDone())
        db.marks().put(Mark("14,39", bookmark = true))             // a 2nd start: nothing re-runs
        assertEquals(0, IdMoves.migrate(ctx, db, prefs))
        assertEquals(true, db.marks().get("15,3")?.bookmark)
    }
}
