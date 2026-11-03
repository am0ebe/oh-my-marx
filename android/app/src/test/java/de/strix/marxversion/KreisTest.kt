package de.strix.marxversion

import de.strix.marxversion.data.Matrix
import de.strix.marxversion.ui.sharedDays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** 👥 shared Stempelkarte: a day counts only if EVYONE i th circle said Amen */
class KreisTest {
    private fun amen(who: String, day: LocalDate) = Matrix.Event("e$who$day", Matrix.AMEN, who, who, "", 0, day.toString())
    private val d1 = LocalDate.now().minusDays(1)
    private val d2 = LocalDate.now().minusDays(2)

    @Test fun onlyFullDaysCount() {
        val ev = listOf(amen("a", d1), amen("b", d1), amen("a", d2))  // d2: b missing
        assertEquals(1, sharedDays(setOf("a", "b"), ev))
    }

    @Test fun chatAndOldDaysIgnored() {
        val chat = Matrix.Event("x", "m.room.message", "a", "a", "hi", 0, null)
        val old = LocalDate.now().minusDays(90)
        assertEquals(0, sharedDays(setOf("a"), listOf(chat, amen("a", old))))
        assertEquals(0, sharedDays(emptySet(), listOf(amen("a", d1))))
    }

    @Test fun codesAreReadable() {
        repeat(200) {
            val c = Matrix.newCode()
            assertEquals(6, c.length)
            assert(c.none { it in "01OIL" }) { c }
        }
    }
}
