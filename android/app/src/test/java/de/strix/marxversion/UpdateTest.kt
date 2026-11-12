package de.strix.marxversion

import de.strix.marxversion.data.Update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/** 🆕 Update glue vs a fake version.json: newer · same · older · garbage · offline (Update.newer = th seam under check()) */
class UpdateTest {
    private fun json(v: String, code: Int) = """{"version":"$v","code":$code}"""

    @Test fun newerIsReported() =
        assertEquals(Update.Latest("0.9.5", 95), Update.newer(90) { json("0.9.5", 95) })

    @Test fun sameCodeIsNoUpdate() = assertNull(Update.newer(95) { json("0.9.5", 95) })

    @Test fun olderIsNoUpdate() = assertNull(Update.newer(95) { json("0.9.0", 90) })

    @Test fun extraFieldsIgnored() =
        assertEquals("1.0.0", Update.newer(1) { """{"version":"1.0.0","code":100,"date":"x","apk":"y"}""" }?.version)

    @Test fun missingCodeIsZero() {
        assertEquals(Update.Latest("1.0.0", 0), Update.parse("""{"version":"1.0.0"}"""))
        assertNull(Update.newer(0) { """{"version":"1.0.0"}""" })
    }

    @Test fun garbageIsNull() {
        for (g in listOf("", "<html>502 Bad Gateway</html>", "not json", "[]", "{}", """{"code":99}""", """{"version":"1","code":"abc"}"""))
            assertNull("garbage: $g", Update.newer(0) { g })
    }

    @Test fun offlineIsNull() = assertNull(Update.newer(0) { throw IOException("offline") })

    @Test fun parseThrowsOnGarbage() {
        assertThrows(Exception::class.java) { Update.parse("nope") }
    }
}
