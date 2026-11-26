package de.strix.marxversion

import de.strix.marxversion.data.VoicePacks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** 🔊 D-085: voice packs per th audio crew's contract (pr/audio/voice/omm_contract.md) · fake server, real zip */
class VoicePackTest {
    @get:Rule val tmp = TemporaryFolder()

    private val quote = "Der Reichtum der Gesellschaften, in welchen kapitalistische Produktionsweise herrscht, erscheint als eine »ungeheure Warensammlung«"
    private fun sha256(b: ByteArray) = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }

    private fun zip(entries: Map<String, ByteArray>): ByteArray = java.io.ByteArrayOutputStream().also { bo ->
        ZipOutputStream(bo).use { z -> entries.forEach { (n, b) -> z.putNextEntry(ZipEntry(n)); z.write(b); z.closeEntry() } }
    }.toByteArray()

    private fun index(created: String = "2026-11-21", sha: String = VoicePacks.sha12(quote)) =
        """{"v":1,"persona":"marx-scholar","lang":"de","label":"KI-Stimme · marx-scholar","created":"$created",
           "items":{"q:V1,2":{"f":"3fa1c0d2e4b5a697.opus","sec":9.4,"sha":"$sha"}}}"""

    private class Server { val files = HashMap<String, ByteArray>(); var gets = 0 }
    private fun packs(s: Server, root: File = tmp.root) = VoicePacks(root,
        fetchText = { u -> s.gets++; s.files[u.substringAfterLast('/')]?.decodeToString() ?: throw IOException("404 $u") },
        fetchTo = { u, out, _ -> out.writeBytes(s.files[u.substringAfterLast('/')] ?: throw IOException("404 $u")) })

    private fun serve(s: Server, z: ByteArray, created: String = "2026-11-21", sha: String = sha256(z)) {
        s.files["marx-scholar_de.zip"] = z
        s.files["manifest.json"] = """{"v":1,"base":"https://scagent.de/oh-my-marx/voice/","packs":[{"persona":"marx-scholar","lang":"de",
            "file":"marx-scholar_de.zip","bytes":${z.size},"sha256":"$sha","items":1,"created":"$created","label":"KI-Stimme · marx-scholar"}]}""".toByteArray()
    }
    private val opus = ByteArray(300) { 7 }

    @Test fun noManifestYet_noOffer_noClip() {
        val p = packs(Server())
        assertNull("404 = old behaviour", p.offer(p.manifest(0L), "de"))
        assertNull(p.clip("de", "q:V1,2", quote))
    }

    @Test fun installThenPlayByIdOnlyWhileTheTextIsTheSame() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray(), "3fa1c0d2e4b5a697.opus" to opus)))
        val p = packs(s)
        val o = p.offer(p.manifest(0L), "de")
        assertNotNull(o); assertNull("en hs no pack", p.offer(p.manifest(0L), "en"))
        assertTrue(p.install(o!!))
        assertEquals("KI-Stimme · marx-scholar", p.installed("de")?.label)
        assertEquals(300L, p.clip("de", "q:V1,2", quote)?.length())
        assertNull("txt edited aftr th render ➡ system TTS", p.clip("de", "q:V1,2", "$quote."))
        assertNull("id not rendered ➡ system TTS", p.clip("de", "q:V9,9", quote))
        assertTrue(p.bytes("de") > 300)
        p.delete("de"); assertNull(p.installed("de")); assertNull(p.clip("de", "q:V1,2", quote))
    }

    @Test fun wrongSha256_keepsTheOldPack_writesNothing() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray(), "3fa1c0d2e4b5a697.opus" to opus)))
        val p = packs(s); assertTrue(p.install(p.offer(p.manifest(0L), "de")!!))
        serve(s, zip(mapOf("index.json" to index("2026-12-01").toByteArray())), created = "2026-12-01", sha = "0".repeat(64))
        assertFalse(p.install(p.offer(p.manifest(0L, force = true), "de")!!))
        assertEquals("old pack still plays", "2026-11-21", p.installed("de")?.created)
        assertNotNull(p.clip("de", "q:V1,2", quote))
        assertEquals("no tmp left", setOf("de", "manifest.json", "manifest.tried"), tmp.root.list()!!.toSet())
    }

    /** critic #345: a 404 was re-fetched on evy Home / ⚙️ visit ➡ now 1 try per day til a manifest exists */
    @Test fun noManifest_triedOncePerDay() {
        val s = Server(); val p = packs(s); val t0 = 1_800_000_000_000L
        p.manifest(t0); p.manifest(t0 + 60_000); p.manifest(t0 + 3_600_000)
        assertEquals(1, s.gets)
        p.manifest(t0 + 25 * 3_600_000L)
        assertEquals("nxt day", 2, s.gets)
    }

    @Test fun laterMeansAWeek() {
        val p = packs(Server()); val t0 = 1_800_000_000_000L
        assertTrue(p.shouldAsk("de", t0)); p.asked("de", t0)
        assertFalse(p.shouldAsk("de", t0 + 86_400_000L)); assertTrue("oth lang", p.shouldAsk("en", t0))
        assertTrue(p.shouldAsk("de", t0 + 8 * 86_400_000L))
    }

    /** critic #345: install held th same lock as th 🔊 lookup ➡ silence 4 th whole 40 MB download */
    @Test fun lookupDoesntWaitForADownload() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray(), "3fa1c0d2e4b5a697.opus" to opus)))
        val gate = java.util.concurrent.CountDownLatch(1)
        val p = VoicePacks(tmp.root, fetchText = { u -> s.files[u.substringAfterLast('/')]!!.decodeToString() },
            fetchTo = { u, out, _ -> gate.await(); out.writeBytes(s.files[u.substringAfterLast('/')]!!) })
        val o = p.offer(p.manifest(0L), "de")!!
        val t = Thread { p.install(o) }.apply { start() }
        val f = java.util.concurrent.Executors.newSingleThreadExecutor().submit<Any?> { p.clip("de", "q:V1,2", quote) }
        assertNull("lookup returns at once (no pack yet ➡ TTS)", f.get(2, java.util.concurrent.TimeUnit.SECONDS))
        gate.countDown(); t.join(5000)
        assertNotNull(p.clip("de", "q:V1,2", quote))
    }

    @Test fun zipCantWriteOutsideItsFolder() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray(), "3fa1c0d2e4b5a697.opus" to opus,
            "../evil.opus" to opus, "sub/x.opus" to opus, "run.sh" to opus)))
        val p = packs(s, File(tmp.root, "voice"))
        assertTrue(p.install(p.offer(p.manifest(0L), "de")!!))
        assertFalse(File(tmp.root, "evil.opus").exists()); assertFalse(File(tmp.root, "voice/evil.opus").exists())
        assertEquals(setOf("index.json", "3fa1c0d2e4b5a697.opus"), File(tmp.root, "voice/de").list()!!.toSet())
    }

    @Test fun manifestAtMostWeekly_newerCreatedOffersAnUpdate() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray(), "3fa1c0d2e4b5a697.opus" to opus)))
        val p = packs(s); val t0 = 1_800_000_000_000L
        assertTrue(p.install(p.offer(p.manifest(t0), "de")!!))
        assertNull("same created = no update", p.update(p.manifest(t0), "de"))
        val z2 = zip(mapOf("index.json" to index("2026-12-01").toByteArray(), "3fa1c0d2e4b5a697.opus" to opus))
        serve(s, z2, created = "2026-12-01")
        val before = s.gets
        assertNull("2 days later: cached manifest, no fetch", p.update(p.manifest(t0 + 2 * 86_400_000L), "de"))
        assertEquals(before, s.gets)
        val u = p.update(p.manifest(t0 + 8 * 86_400_000L), "de")
        assertEquals("2026-12-01", u?.created)
        assertTrue(p.install(u!!)); assertEquals("2026-12-01", p.installed("de")?.created)
    }

    @Test fun garbageManifestKeepsTheCachedOne() {
        val s = Server(); serve(s, zip(mapOf("index.json" to index().toByteArray())))
        val p = packs(s)
        assertNotNull(p.offer(p.manifest(0L), "de"))
        s.files["manifest.json"] = "<html>502</html>".toByteArray()
        assertNotNull(p.offer(p.manifest(0L, force = true), "de"))
    }

    /** contract § stale check: SHA-1 over th raw UTF-8 string, 1st 12 hex (python: hashlib.sha1(s.encode()).hexdigest()[:12]) */
    @Test fun sha12MatchesThePythonRenderer() = assertEquals("a9993e364706", VoicePacks.sha12("abc"))

    /** D-087: ids = omm_contract.md § item ids examples (`q:8,9` · `a:30tage:0` = day 1 o th plan, 0-based) */
    @Test fun idsMatchTheContract() {
        assertEquals("q:8,9", VoicePacks.quoteId("8,9"))
        assertEquals("a:30tage:0", VoicePacks.andachtId("30tage", 0))
    }

    /** D-087: an `a:` clip plays only while th Andacht txt is th one it was rendered from (pinned old cut · edit ➡ phone voice) */
    @Test fun andachtClipFollowsItsText() {
        val ad = "Heute: die Ware als Zelle."
        val p = VoicePacks(tmp.root, fetchText = { throw java.io.IOException() }, fetchTo = { _, _, _ -> throw java.io.IOException() })
        val d = File(tmp.root, "de").apply { mkdirs() }
        File(d, "0123456789abcdef.opus").writeBytes(byteArrayOf(1))
        File(d, "index.json").writeText("""{"v":1,"persona":"p","lang":"de","items":{"${VoicePacks.andachtId("30tage", 0)}":{"f":"0123456789abcdef.opus","sec":3.0,"sha":"${VoicePacks.sha12(ad)}"}}}""")
        assertNotNull(p.clip("de", VoicePacks.andachtId("30tage", 0), ad))
        assertNull(p.clip("de", VoicePacks.andachtId("30tage", 0), "$ad (edited)"))
        assertNull(p.clip("de", VoicePacks.andachtId("30tage", 1), ad))
    }
}
