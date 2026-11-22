package de.strix.marxversion

import de.strix.marxversion.data.Matrix
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.net.URLStreamHandlerFactory
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 💬 Matrix client vs a fake homeserver (JDK HttpServer on localhost) · ❌ never touches scagent.de
 * Matrix.HS is a private const "https://scagent.de/…" ➡ a URLStreamHandlerFactory (once per JVM) reroutes
 * th https handler 2 th fake. Only https is replaced, th fake itself is plain http.
 */
class MatrixTest {
    class Req(val method: String, val path: String, val auth: String?, val body: JSONObject)

    companion object {
        lateinit var server: ServerSocket
        val seen = CopyOnWriteArrayList<Req>()
        /** per test: path-prefix ➡ handler(req) = (status, json) · longest matching prefix wins */
        val routes = LinkedHashMap<String, (Req) -> Pair<Int, JSONObject>>()

        @BeforeClass @JvmStatic fun up() {
            server = ServerSocket(0, 50, InetAddress.getLoopbackAddress())
            // raw sockets: com.sun.net.httpserver isn't visible on th Android unit-tst compile classpath
            thread(isDaemon = true) { while (!server.isClosed) runCatching { server.accept().use { handle(it) } } }
            val port = server.localPort
            URL.setURLStreamHandlerFactory(URLStreamHandlerFactory { proto ->
                if (proto != "https") null else object : URLStreamHandler() {
                    override fun openConnection(u: URL): URLConnection =
                        URL("http://127.0.0.1:$port${u.file}").openConnection() as HttpURLConnection
                }
            })
        }

        @AfterClass @JvmStatic fun down() { server.close() }

        /** 1 request per connection: request line + headers + Content-Length body ➡ route ➡ JSON reply, Connection: close */
        private fun handle(sock: Socket) {
            val inp = sock.getInputStream().buffered()
            fun line(): String { val sb = StringBuilder(); while (true) { val b = inp.read(); if (b < 0 || b == '\n'.code) break; if (b != '\r'.code) sb.append(b.toChar()) }; return sb.toString() }
            val (method, target) = line().split(" ").let { it[0] to it[1] }
            val hdr = HashMap<String, String>()
            while (true) { val l = line(); if (l.isEmpty()) break; hdr[l.substringBefore(':').lowercase()] = l.substringAfter(':').trim() }
            val raw = ByteArray(hdr["content-length"]?.toInt() ?: 0).also { var n = 0; while (n < it.size) n += inp.read(it, n, it.size - n).also { r -> if (r < 0) return@also } }
            val req = Req(method, target, hdr["authorization"], runCatching { JSONObject(String(raw)) }.getOrDefault(JSONObject()))
            seen += req
            val h = routes.entries.filter { req.path.startsWith(it.key) }.maxByOrNull { it.key.length }?.value
            val (code, json) = h?.invoke(req) ?: (404 to JSONObject().put("errcode", "M_NOT_FOUND"))
            val out = json.toString().toByteArray()
            sock.getOutputStream().apply {
                write("HTTP/1.1 $code X\r\nContent-Type: application/json\r\nContent-Length: ${out.size}\r\nConnection: close\r\n\r\n".toByteArray())
                write(out); flush()
            }
        }
    }

    private val S = Matrix.Session("@leo-ab12:scagent.de", "tok123")
    private val P = "/_matrix/client/v3"

    @Before fun reset() { seen.clear(); routes.clear() }

    @Test fun registerWalksTheThreeUiaSteps() {
        routes["$P/register"] = { r ->
            val auth = r.body.optJSONObject("auth")
            when (auth?.optString("type")) {
                null -> 401 to JSONObject().put("session", "S1").put("completed", JSONArray())
                "m.login.registration_token" ->
                    if (auth.optString("token") == "secret" && auth.optString("session") == "S1")
                        401 to JSONObject().put("session", "S1").put("completed", JSONArray().put("m.login.registration_token"))
                    else 401 to JSONObject().put("session", "S1").put("completed", JSONArray())
                "m.login.dummy" -> 200 to JSONObject().put("user_id", "@x:scagent.de").put("access_token", "AT")
                else -> 400 to JSONObject()
            }
        }
        routes["$P/profile/"] = { 200 to JSONObject() }
        val (sess, pw) = runBlocking { Matrix.register("Karl Marx!", "secret") }!!
        assertEquals(Matrix.Session("@x:scagent.de", "AT"), sess)
        assertEquals(32, pw.length)
        val regs = seen.filter { it.path == "$P/register" }
        assertEquals(listOf(null, "m.login.registration_token", "m.login.dummy"), regs.map { it.body.optJSONObject("auth")?.optString("type") })
        assertTrue(regs[0].body.getString("username").matches(Regex("karlmarx-[0-9a-f]{4}")))  // slug: lowercase, alnum only
        val name = seen.single { it.method == "PUT" }
        assertEquals("Karl Marx!", name.body.getString("displayname"))
        assertEquals("Bearer AT", name.auth)
        assertTrue(name.path.startsWith("$P/profile/%40x%3Ascagent.de/displayname"))  // userId url-encoded
    }

    @Test fun registerWithWrongTokenFailsSoft() {
        routes["$P/register"] = { 401 to JSONObject().put("session", "S").put("completed", JSONArray()) }  // never advances
        assertNull(runBlocking { Matrix.register("a", "bad") })
        assertEquals(4, seen.size)  // 1st call + max 3 steps, then gives up (no endless loop)
    }

    @Test fun joinReturnsRoomIdAndEncodesAlias() {
        routes["$P/join/"] = { 200 to JSONObject().put("room_id", "!r:scagent.de") }
        assertEquals("!r:scagent.de", runBlocking { Matrix.join(S, Matrix.alias("V1.2")) })
        val r = seen.single()
        assertEquals("$P/join/%23omm-kap-v1.2%3Ascagent.de", r.path)  // # and : encoded
        assertEquals("Bearer tok123", r.auth)
    }

    @Test fun joinFailsSoft() {
        routes["$P/join/"] = { 403 to JSONObject().put("errcode", "M_FORBIDDEN") }
        assertNull(runBlocking { Matrix.join(S, "#x:scagent.de") })
        routes.clear()  // no route ➡ 404 ➡ null too
        assertNull(runBlocking { Matrix.join(S, "#y:scagent.de") })
    }

    private fun msg(id: String, sender: String, body: String?, name: String? = null, page: Int? = null, type: String = "m.room.message", day: String? = null): JSONObject {
        val c = JSONObject()
        body?.let { c.put("body", it).put("msgtype", "m.text") }
        name?.let { c.put("de.ohmymarx.name", it) }
        page?.let { c.put("de.ohmymarx.page", it) }
        day?.let { c.put("day", it) }
        return JSONObject().put("event_id", id).put("sender", sender).put("type", type).put("origin_server_ts", 1000L).put("content", c)
    }

    // dir=b ➡ server sends NEWEST first · client must return oldest first
    private fun messages(vararg e: JSONObject) { routes["$P/rooms/"] = { 200 to JSONObject().put("chunk", JSONArray(e.toList())) } }

    @Test fun commentsParseOrderAndDropRedacted() {
        messages(
            msg("e3", "@c:scagent.de", "third", page = 7),
            msg("e2", "@b:scagent.de", null),                        // redacted: content = {}
            msg("e1", "@a:scagent.de", "first", name = "Anna"),
            JSONObject().put("event_id", "e0").put("sender", "@a:scagent.de").put("type", "m.room.member").put("content", JSONObject().put("membership", "join")),
        )
        val c = runBlocking { Matrix.comments(S, "!r:scagent.de") }
        assertEquals(listOf("e1", "e3"), c.map { it.id })               // oldest 1st, redacted + non-message gone
        assertEquals("Anna", c[0].name)                                  // custom name
        assertEquals("c", c[1].name)                                     // fallback: localpart of sender
        assertNull(c[0].page); assertEquals(7, c[1].page)
        assertTrue(seen.single().path.endsWith("/messages?dir=b&limit=300"))
    }

    @Test fun timelineKeepsChatAndAmensDropsRest() {
        messages(
            msg("e4", "@b:scagent.de", "hi"),
            msg("e3", "@b:scagent.de", null, type = Matrix.AMEN, day = "2026-11-04"),
            msg("e2", "@a:scagent.de", null),                                        // redacted
            msg("e1", "@a:scagent.de", null, type = Matrix.AMEN, day = "2026-11-03"),
            msg("e0", "@a:scagent.de", "x", type = "m.room.topic"),
        )
        val t = runBlocking { Matrix.timeline(S, "!r:scagent.de") }
        assertEquals(listOf("e1", "e3", "e4"), t.map { it.id })
        assertEquals(listOf("2026-11-03", "2026-11-04", null), t.map { it.day })
        assertEquals(Matrix.AMEN, t[0].type); assertEquals("hi", t[2].text)
        assertFalse(t.any { it.id == "e2" })
    }

    @Test fun serverErrorsGiveEmptyNotCrash() {
        routes["$P/rooms/"] = { 500 to JSONObject() }
        assertTrue(runBlocking { Matrix.comments(S, "!r:scagent.de") }.isEmpty())
        assertTrue(runBlocking { Matrix.timeline(S, "!r:scagent.de") }.isEmpty())
        routes["$P/rooms/"] = { 200 to JSONObject() }  // 200 but no "chunk" ➡ parse error ➡ empty
        assertTrue(runBlocking { Matrix.timeline(S, "!r:scagent.de") }.isEmpty())
    }

    @Test fun sendCarriesPageAndName() {
        routes["$P/rooms/"] = { 200 to JSONObject().put("event_id", "\$e") }
        assertTrue(runBlocking { Matrix.send(S, "!r:scagent.de", "hello", 12, "Anna") })
        val r = seen.single()
        assertEquals("PUT", r.method)
        assertEquals(12, r.body.getInt("de.ohmymarx.page")); assertEquals("Anna", r.body.getString("de.ohmymarx.name"))
        assertEquals("hello", r.body.getString("body"))
        assertNotNull(r.auth)
    }

    // ── 👥 Lesekreise (todo ‼️ Matrix: JVM tst vs fake server) ──

    @Test fun createKreisIsPrivateAliasedAndHistoryShared() {
        routes["$P/createRoom"] = { 200 to JSONObject().put("room_id", "!k:scagent.de") }
        assertEquals("!k:scagent.de", runBlocking { Matrix.createKreis(S, "Bus 42", "AB3XYZ") })
        val b = seen.single().body
        assertEquals("omm-kreis-ab3xyz", b.getString("room_alias_name"))   // = kreisAlias("AB3XYZ") minus # + server
        assertEquals("#omm-kreis-ab3xyz:scagent.de", Matrix.kreisAlias("AB3XYZ"))
        assertEquals("private", b.getString("visibility"))                 // not i th public room directory
        assertEquals("shared", b.getJSONArray("initial_state").getJSONObject(0).getJSONObject("content").getString("history_visibility"))
        assertTrue(b.getString("topic").contains("AB3XYZ"))
    }

    @Test fun createKreisCodeTakenIsNull() {
        routes["$P/createRoom"] = { 400 to JSONObject().put("errcode", "M_ROOM_IN_USE") }
        assertNull(runBlocking { Matrix.createKreis(S, "x", "AAAAAA") })
    }

    @Test fun membersFallBackToLocalpart() {
        routes["$P/rooms/"] = { 200 to JSONObject().put("joined", JSONObject()
            .put("@anna-1:scagent.de", JSONObject().put("display_name", "Anna"))
            .put("@bob-2:scagent.de", JSONObject())) }
        assertEquals(mapOf("@anna-1:scagent.de" to "Anna", "@bob-2:scagent.de" to "bob-2"), runBlocking { Matrix.members(S, "!k:scagent.de") })
        assertTrue(seen.single().path.endsWith("/joined_members"))
        routes["$P/rooms/"] = { 403 to JSONObject() }
        assertTrue(runBlocking { Matrix.members(S, "!k:scagent.de") }.isEmpty())
    }

    @Test fun roomNameLeaveRedactSetName() {
        routes["$P/rooms/"] = { r -> 200 to (if (r.path.contains("m.room.name")) JSONObject().put("name", "Bus 42") else JSONObject()) }
        routes["$P/profile/"] = { 200 to JSONObject() }
        runBlocking {
            assertEquals("Bus 42", Matrix.roomName(S, "!k:scagent.de"))
            assertTrue(Matrix.leave(S, "!k:scagent.de"))
            assertTrue(Matrix.redact(S, "!k:scagent.de", "\$ev1"))
            assertTrue(Matrix.setName(S, "Rosa"))
        }
        val (name, leave, redact, prof) = seen.toList()
        assertEquals("GET", name.method)
        assertEquals("POST", leave.method); assertTrue(leave.path.endsWith("/leave"))
        assertEquals("PUT", redact.method); assertTrue(redact.path.contains("/redact/"))   // txn id unique per call
        assertEquals("PUT", prof.method); assertEquals("Rosa", prof.body.getString("displayname"))
        assertTrue("user id url-encoded", prof.path.contains("%40leo-ab12%3Ascagent.de"))
        routes.clear()   // all 404 ➡ false, no crash
        runBlocking { assertTrue(!Matrix.leave(S, "!k:scagent.de") && !Matrix.redact(S, "!k:scagent.de", "\$e") && !Matrix.setName(S, "x")) }
    }

    /** 🗑️ D-072: delete account = deactivate + erase, pw auth 4 MY user · unknown token = alr gone = ok · oth errors = false */
    @Test fun deactivateErasesWithPasswordAuth() {
        routes["$P/account/deactivate"] = { 200 to JSONObject() }
        assertTrue(runBlocking { Matrix.deactivate(S, "pw1") })
        val r = seen.single()
        assertEquals("POST", r.method); assertEquals("Bearer tok123", r.auth)
        assertTrue(r.body.getBoolean("erase"))
        val auth = r.body.getJSONObject("auth")
        assertEquals("m.login.password", auth.getString("type")); assertEquals("pw1", auth.getString("password"))
        assertEquals(S.userId, auth.getJSONObject("identifier").getString("user"))
        routes["$P/account/deactivate"] = { 401 to JSONObject().put("errcode", "M_UNKNOWN_TOKEN") }
        assertTrue("token alr dead = account gone", runBlocking { Matrix.deactivate(S, "pw1") })
        routes["$P/account/deactivate"] = { 401 to JSONObject().put("errcode", "M_FORBIDDEN") }
        assertFalse("wrong pw", runBlocking { Matrix.deactivate(S, "pw1") })
    }

    /** D-076: continuwuity ignores `erase` ➡ evy own msg + Amen i evy joined room redacted (paged, 429 retried) · oth ppl's + alr redacted left alone · 1 fail = false */
    @Test fun redactAllMineWalksEveryRoomAndPage() {
        fun ev(id: String, sender: String, type: String = "m.room.message", body: String? = "hi") = JSONObject().put("event_id", id).put("sender", sender)
            .put("type", type).put("content", if (body == null) JSONObject() else JSONObject().put("body", body))
        routes["$P/joined_rooms"] = { 200 to JSONObject().put("joined_rooms", JSONArray().put("!a:scagent.de").put("!b:scagent.de")) }
        var limited = false
        var redactCode = 200
        routes["$P/rooms/"] = { r ->
            when {
                "/redact/" in r.path -> if (!limited && "%24e5" in r.path) { limited = true; 429 to JSONObject().put("retry_after_ms", 200) } else redactCode to JSONObject()
                "%21a" in r.path && "from=t1" !in r.path -> 200 to JSONObject().put("end", "t1").put("chunk", JSONArray()
                    .put(ev("\$e1", S.userId)).put(ev("\$e2", "@b:scagent.de")).put(ev("\$e3", S.userId, body = null)).put(ev("\$e4", S.userId, Matrix.AMEN, null).put("content", JSONObject().put("day", "2026-11-22"))))
                "%21a" in r.path -> 200 to JSONObject().put("chunk", JSONArray())
                else -> 200 to JSONObject().put("chunk", JSONArray().put(ev("\$e5", S.userId)).put(ev("\$e6", S.userId, "m.room.member")))
            }
        }
        assertTrue(runBlocking { Matrix.redactAllMine(S) })
        val red = seen.filter { "/redact/" in it.path }
        assertEquals(listOf("e1", "e4", "e5", "e5"), red.map { it.path.substringAfter("/redact/%24").substringBefore('/') })  // e5: 429 ➡ retried
        assertEquals("txn ids unique", red.size, red.map { it.path.substringAfterLast('/') }.toSet().size)
        assertTrue("filter = me", seen.first { "/messages" in it.path }.path.contains("senders"))
        seen.clear(); redactCode = 403
        assertFalse("1 redact refused ➡ don't deactivate", runBlocking { Matrix.redactAllMine(S) })
    }

    /** D-077: shown handle = server-set id (localpart on ours, full id elsewhere) · hidden only when th name alr IS it */
    @Test fun handleShowsTheUnspoofableId() {
        assertEquals("strix-cd34", Matrix.handle("@strix-cd34:scagent.de", "strix"))
        assertEquals("@strix:matrix.org", Matrix.handle("@strix:matrix.org", "strix"))
        assertNull(Matrix.handle("@leo-ab12:scagent.de", "leo-ab12"))   // fallback name = localpart ➡ no dup
    }

    /** D-075: 401 = server forgot th token (homeserver replaced @1113) ➡ false = re-register · any oth answer ➡ null = keep th session */
    @Test fun tokenValidTellsDeadFromOffline() {
        routes["$P/account/whoami"] = { 200 to JSONObject().put("user_id", S.userId) }
        assertEquals(true, runBlocking { Matrix.tokenValid(S) })
        val r = seen.single()
        assertEquals("GET", r.method); assertEquals("Bearer tok123", r.auth)
        routes["$P/account/whoami"] = { 401 to JSONObject().put("errcode", "M_UNKNOWN_TOKEN") }
        assertEquals(false, runBlocking { Matrix.tokenValid(S) })
        routes["$P/account/whoami"] = { 502 to JSONObject() }
        assertNull("proxy hiccup ≠ dead token", runBlocking { Matrix.tokenValid(S) })
    }

    @Test fun kreisCodeIsReadableAloud() {
        repeat(200) {
            val c = Matrix.newCode()
            assertEquals(6, c.length)
            assertTrue(c, c.none { it in "0O1IL" } && c.all { it.isUpperCase() || it.isDigit() })
        }
    }
}
