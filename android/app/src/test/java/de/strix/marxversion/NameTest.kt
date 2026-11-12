package de.strix.marxversion

import de.strix.marxversion.ui.comradeName
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** 🎲 QA r3: default comrade names = "<first> <year>", varied, no regime names (D-014) */
class NameTest {
    @Test fun shape() = repeat(50) { assertTrue(Regex("^\\p{L}+ \\d{4}$").matches(comradeName(Random(it)))) }
    @Test fun varied() = assertTrue((0 until 50).map { comradeName(Random(it)) }.toSet().size > 10)
    @Test fun noRegimeNames() = repeat(200) { val n = comradeName(Random(it)); assertTrue(listOf("Lenin", "Stalin", "Mao").none { n.startsWith(it) }) }
}
