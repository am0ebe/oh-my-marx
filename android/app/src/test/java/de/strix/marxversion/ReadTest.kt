package de.strix.marxversion

import de.strix.marxversion.ui.bookPercent
import org.junit.Assert.assertEquals
import org.junit.Test

/** 📊 Read list % (QA r5): 0 only if nothing read · started = ≥ 1 · 100 only when evy paragraph is read */
class ReadTest {
    @Test fun percent() {
        assertEquals(0, bookPercent(0, 2000))
        assertEquals(1, bookPercent(1, 2000))
        assertEquals(50, bookPercent(1000, 2000))
        assertEquals(99, bookPercent(1999, 2000))
        assertEquals(100, bookPercent(2000, 2000))
        assertEquals(0, bookPercent(5, 0))
    }
}
