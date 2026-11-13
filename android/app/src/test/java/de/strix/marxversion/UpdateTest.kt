package de.strix.marxversion

import de.strix.marxversion.data.Update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
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

    // ── QA r5 self-update ──

    @Test fun apkAndShaParsed() {
        val l = Update.parse("""{"version":"0.9.1","code":5,"apk":"OhMyMarx-0.9.1.apk","sha256":"ABCD","mb":31}""")
        assertEquals("OhMyMarx-0.9.1.apk", l.apk)
        assertEquals("abcd", l.sha256)
    }

    @Test fun apkMissingIsNull() = assertNull(Update.parse("""{"version":"1","code":2}""").apk)

    /** a tampered json must not point th download elsewhere */
    @Test fun apkMustBeAPlainName() {
        for (bad in listOf("../x.apk", "https://evil/x.apk", "/sdcard/x.apk", "x.exe", "a b.apk"))
            assertNull(bad, Update.parse("""{"version":"1","code":2,"apk":"$bad"}""").apk)
        assertEquals("OhMyMarx-0.9.0-dev.1113.0412.apk", Update.parse("""{"version":"1","code":2,"apk":"OhMyMarx-0.9.0-dev.1113.0412.apk"}""").apk)
    }

    @Test fun channels() {
        assertEquals("https://scagent.de/oh-my-marx/", Update.base(false))
        assertEquals("https://scagent.de/oh-my-marx/dev/", Update.base(true))
    }

    @Test fun storesAndObtainiumUpdateThemselves() {
        assertFalse(Update.wantedFor("dev.imranr.obtainium.fdroid"))
        assertFalse(Update.wantedFor("com.android.vending"))
        assertFalse(Update.wantedFor("org.fdroid.fdroid"))
        assertTrue(Update.wantedFor(null))                                   // adb / unknown
        assertTrue(Update.wantedFor("com.google.android.packageinstaller")) // browser download ➡ tap apk
        assertTrue(Update.wantedFor("de.strix.marxversion"))               // our own self-update
    }

    @Test fun nagRule() {
        assertTrue(Update.shouldNotify(9, 0, Long.MIN_VALUE / 2, 100))   // nvr notified
        assertTrue(Update.shouldNotify(9, 8, 100, 100))                  // newer version, same day
        assertFalse(Update.shouldNotify(9, 9, 100, 101))                 // same version, 1 day later
        assertTrue(Update.shouldNotify(9, 9, 100, 100L + Update.RENAG))   // still not installed ➡ again
    }

    @Test fun sha256OfFile() {
        val f = File.createTempFile("omm", ".apk").apply { writeText("abc"); deleteOnExit() }
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Update.sha256(f))
    }
}
