package de.strix.marxversion

import de.strix.marxversion.logic.cardsDone
import de.strix.marxversion.ui.RANKS
import de.strix.marxversion.ui.levelName
import de.strix.marxversion.ui.shownLevel
import org.junit.Assert.assertEquals
import org.junit.Test

/** 🧺 D-084 (🦠 @1126 "start w lvl1. It shd also have a name"): new reader = LEVEL 1 "Commodity", nvr LEVEL 0 / "No rank yet" · UI = MineLevelTest */
class LevelTest {
    @Test fun newReaderIsLevel1WithAName() {
        assertEquals(1, shownLevel(cardsDone(0)))
        assertEquals("🧺 Commodity", levelName(cardsDone(0)).second)
        assertEquals("🧺 Ware", levelName(0).first)
    }

    @Test fun eachFullCardIsOneLevelUpAndKeepsTheOldRankNames() {
        assertEquals(2, shownLevel(1)); assertEquals(RANKS[0], levelName(1))   // 1st card = Primitive accumulation, now LEVEL 2
        assertEquals(8, shownLevel(RANKS.size)); assertEquals(RANKS.last(), levelName(RANKS.size))
        assertEquals("past th last rank = stays th last name", RANKS.last(), levelName(RANKS.size + 3))
        assertEquals("1st card full = LEVEL 2", 2, shownLevel(cardsDone((1 until 400).first { cardsDone(it) == 1 })))
    }
}
