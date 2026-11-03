package de.strix.marxversion.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom

/**
 * 💬 comments = Matrix (own homeserver on th VPS, server/matrix/) · tiny client-server API subset, no SDK:
 *   register (registration token ➡ dummy) · join #omm-kap-<kap> · send m.text w th MEW page · read /messages
 * 1 room per chapter, comments carry `de.ohmymarx.page` ➡ th app groups them by page.
 * Bonus: any Matrix app (Element …) can join th same rooms 4 long discussions.
 * Evy call fails soft (null / false / empty).
 */
object Matrix {
    private const val HS = "https://scagent.de/_matrix/client/v3"
    const val SERVER = "scagent.de"

    data class Session(val userId: String, val token: String)
    data class Comment(val id: String, val sender: String, val name: String, val text: String, val ts: Long, val page: Int?)

    fun alias(kap: String) = "#omm-kap-${kap.lowercase()}:$SERVER"

    /** new account: username fr th display name + 4 random hex · pw random (kept on th fon only) */
    suspend fun register(name: String, regToken: String): Pair<Session, String>? = withContext(Dispatchers.IO) {
        runCatching {
            val slug = name.lowercase().replace(Regex("[^a-z0-9]"), "").take(12).ifEmpty { "leser" }
            val user = slug + "-" + hex(2)
            val pw = hex(16)
            val body = JSONObject().put("username", user).put("password", pw).put("initial_device_display_name", "Oh My Marx")
            // UIA: 1st call ➡ 401 + session · then registration_token · then dummy
            var (code, r) = call("POST", "/register", body)
            var stage = 0
            while (code == 401 && stage < 3) {
                val session = r.optString("session")
                val done = r.optJSONArray("completed")?.let { a -> List(a.length()) { a.getString(it) } } ?: emptyList()
                val auth = if ("m.login.registration_token" !in done)
                    JSONObject().put("type", "m.login.registration_token").put("token", regToken).put("session", session)
                else JSONObject().put("type", "m.login.dummy").put("session", session)
                val res = call("POST", "/register", JSONObject(body.toString()).put("auth", auth))
                code = res.first; r = res.second; stage++
            }
            if (code != 200) return@runCatching null
            val s = Session(r.getString("user_id"), r.getString("access_token"))
            setName(s, name)
            s to pw
        }.getOrNull()
    }

    suspend fun setName(s: Session, name: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { call("PUT", "/profile/${enc(s.userId)}/displayname", JSONObject().put("displayname", name), s.token).first == 200 }.getOrDefault(false)
    }

    /** join by alias (idempotent) ➡ room id */
    suspend fun join(s: Session, alias: String): String? = withContext(Dispatchers.IO) {
        runCatching { call("POST", "/join/${enc(alias)}", JSONObject(), s.token).takeIf { it.first == 200 }?.second?.getString("room_id") }.getOrNull()
    }

    suspend fun send(s: Session, room: String, text: String, page: Int?, name: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val c = JSONObject().put("msgtype", "m.text").put("body", text).put("de.ohmymarx.name", name)
            page?.let { c.put("de.ohmymarx.page", it) }
            call("PUT", "/rooms/${enc(room)}/send/m.room.message/omm${System.currentTimeMillis()}", c, s.token).first == 200
        }.getOrDefault(false)
    }

    /** last ~300 msgs o a chapter room, oldest 1st · redacted ones (empty content) dropped */
    suspend fun comments(s: Session, room: String): List<Comment> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, r) = call("GET", "/rooms/${enc(room)}/messages?dir=b&limit=300", null, s.token)
            if (code != 200) return@runCatching emptyList()
            val chunk = r.getJSONArray("chunk")
            (0 until chunk.length()).map { chunk.getJSONObject(it) }
                .filter { it.optString("type") == "m.room.message" && it.optJSONObject("content")?.has("body") == true }
                .map { e ->
                    val c = e.getJSONObject("content")
                    val sender = e.getString("sender")
                    Comment(e.getString("event_id"), sender, c.optString("de.ohmymarx.name").ifBlank { sender.substringAfter('@').substringBefore(':') },
                        c.getString("body"), e.optLong("origin_server_ts"), if (c.has("de.ohmymarx.page")) c.optInt("de.ohmymarx.page") else null)
                }
                .reversed()
        }.getOrDefault(emptyList())
    }

    // ── 👥 Kreise = 1 Matrix room each, joined by a 6-char code (= th room alias #omm-kreis-<code>) ──
    // join rule "public" but NOT i th room directory ➡ only who knows th code finds it (= an invite link)
    // ⚠️ not end-2-end encrypted (th app has no E2E): th server admin could read Kreis chats

    /** timeline item i a Kreis: chat (m.room.message) o an Amen (de.ohmymarx.amen, content.day = "2026-11-04") */
    data class Event(val id: String, val type: String, val sender: String, val name: String, val text: String, val ts: Long, val day: String?)

    const val AMEN = "de.ohmymarx.amen"
    fun kreisAlias(code: String) = "#omm-kreis-${code.lowercase()}:$SERVER"

    /** new Kreis ➡ room id · null = failed (eg code taken) */
    suspend fun createKreis(s: Session, name: String, code: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val body = JSONObject().put("name", name).put("room_alias_name", "omm-kreis-${code.lowercase()}")
                .put("visibility", "private").put("preset", "public_chat")
                .put("topic", "👥 Oh My Marx Lesekreis · Code $code")
                .put("initial_state", org.json.JSONArray().put(JSONObject().put("type", "m.room.history_visibility")
                    .put("state_key", "").put("content", JSONObject().put("history_visibility", "shared"))))
            val (code2, r) = call("POST", "/createRoom", body, s.token)
            if (code2 == 200) r.getString("room_id") else null
        }.getOrNull()
    }

    suspend fun roomName(s: Session, room: String): String? = withContext(Dispatchers.IO) {
        runCatching { call("GET", "/rooms/${enc(room)}/state/m.room.name", null, s.token).takeIf { it.first == 200 }?.second?.optString("name") }.getOrNull()
    }

    /** user id ➡ display name */
    suspend fun members(s: Session, room: String): Map<String, String> = withContext(Dispatchers.IO) {
        runCatching {
            val j = call("GET", "/rooms/${enc(room)}/joined_members", null, s.token).second.getJSONObject("joined")
            j.keys().asSequence().associateWith { uid -> j.getJSONObject(uid).optString("display_name").ifBlank { uid.substringAfter('@').substringBefore(':') } }
        }.getOrDefault(emptyMap())
    }

    suspend fun sendEvent(s: Session, room: String, type: String, content: JSONObject): Boolean = withContext(Dispatchers.IO) {
        runCatching { call("PUT", "/rooms/${enc(room)}/send/${enc(type)}/omm${System.nanoTime()}", content, s.token).first == 200 }.getOrDefault(false)
    }

    /** chat + Amens, oldest 1st (last ~300) */
    suspend fun timeline(s: Session, room: String): List<Event> = withContext(Dispatchers.IO) {
        runCatching {
            val (code, r) = call("GET", "/rooms/${enc(room)}/messages?dir=b&limit=300", null, s.token)
            if (code != 200) return@runCatching emptyList()
            val chunk = r.getJSONArray("chunk")
            (0 until chunk.length()).map { chunk.getJSONObject(it) }
                .filter { (it.optString("type") == "m.room.message" && it.optJSONObject("content")?.has("body") == true) || it.optString("type") == AMEN }
                .map { e ->
                    val c = e.optJSONObject("content") ?: JSONObject()
                    val sender = e.getString("sender")
                    Event(e.getString("event_id"), e.getString("type"), sender,
                        c.optString("de.ohmymarx.name").ifBlank { sender.substringAfter('@').substringBefore(':') },
                        c.optString("body"), e.optLong("origin_server_ts"), c.optString("day").ifBlank { null })
                }.reversed()
        }.getOrDefault(emptyList())
    }

    suspend fun leave(s: Session, room: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { call("POST", "/rooms/${enc(room)}/leave", JSONObject(), s.token).first == 200 }.getOrDefault(false)
    }

    /** 6 chars, no look-alikes (0/O, 1/I/L) ➡ easy 2 read out loud */
    fun newCode(): String { val a = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; val r = SecureRandom(); return (1..6).map { a[r.nextInt(a.length)] }.joinToString("") }

    /** delete my own comment (Matrix redact) */
    suspend fun redact(s: Session, room: String, eventId: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { call("PUT", "/rooms/${enc(room)}/redact/${enc(eventId)}/omm${System.currentTimeMillis()}", JSONObject(), s.token).first == 200 }.getOrDefault(false)
    }

    private fun call(method: String, path: String, body: JSONObject?, token: String? = null): Pair<Int, JSONObject> {
        val c = (URL(HS + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 6000; readTimeout = 8000
            setRequestProperty("Content-Type", "application/json")
            token?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) { doOutput = true; outputStream.use { it.write(body.toString().toByteArray()) } }
        }
        val code = c.responseCode
        val txt = (if (code >= 400) c.errorStream else c.inputStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
        return code to runCatching { JSONObject(txt) }.getOrDefault(JSONObject())
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
    private fun hex(bytes: Int) = ByteArray(bytes).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
}
