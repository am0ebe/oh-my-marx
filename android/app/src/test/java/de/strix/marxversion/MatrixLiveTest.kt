package de.strix.marxversion

import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.ViewModelProvider
import de.strix.marxversion.ui.AppVm
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date

/**
 * 💬 th in-app Matrix path vs th LIVE Synapse (scagent.de) = th taps 🦠 wd do on th fon, driven i th real app (Robolectric):
 *   ① 📖 ch 8 p. 245 ➡ passage menu ➡ Note ➡ 👥 "also post to th reading room" ➡ tap away = app makes its account + posts
 *   ② server: B (raw HTTP = "Element") reads it (txt + page + name) ➡ B replies ➡ app's 💬 sheet shows it ➡ hold own = delete ➡ gone 4 B
 *   ③ 👥 Circle tab: found a Kreis ➡ B joins by code ➡ app ➡ B chat · B ➡ app (Refresh) ➡ B founds one ➡ app joins it w th code
 *   ④ ⚙️ Delete account (D-072) ➡ confirm ➡ token dead · app user left th Kreis
 * Mitlesen = always on (D-020, no toggle) ➡ nothing 2 switch.
 * accounts: app = `e2erosa-<hex>` (its own signup path) · B = `e2e-<MMDD-hhmm>-b` · both + th rooms ➡ `priv/matrix_e2e_app.json`
 *   ➡ `bin/matrix_e2e.py --cleanup` deactivates them · th test deletes its comment itself
 * run: `bin/matrix_e2e.py --app` (= gradle -Plive + prints th table) · wo -Plive = skipped · ⚠️ alone: MatrixTest reroutes https per JVM
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
class MatrixLiveTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before fun gate() {
        assumeTrue("needs -Plive (bin/matrix_e2e.py --app)", System.getProperty("live") == "1")
        assumeTrue("no omm.regToken i th build", BuildConfig.OMM_REG_TOKEN.isNotBlank())
        assumeTrue("MatrixTest's fake https is active i this JVM ➡ run MatrixLiveTest alone",
            URL("https://scagent.de/").openConnection().url.host == "scagent.de")
    }

    // ── B = th "Element" side: raw client-server API, independent o data/Matrix.kt ──
    private val hs = "https://scagent.de/_matrix/client/v3"
    private fun call(method: String, path: String, body: JSONObject? = null, tok: String? = null): Pair<Int, JSONObject> {
        val c = (URL(hs + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 10_000; readTimeout = 15_000
            setRequestProperty("Content-Type", "application/json")
            tok?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) { doOutput = true; outputStream.use { it.write(body.toString().toByteArray()) } }
        }
        val code = c.responseCode
        val txt = (if (code >= 400) c.errorStream else c.inputStream)?.bufferedReader()?.use { it.readText() } ?: "{}"
        return code to runCatching { JSONObject(txt) }.getOrDefault(JSONObject())
    }
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")
    private fun send(tok: String, room: String, content: JSONObject, type: String = "m.room.message") =
        call("PUT", "/rooms/${enc(room)}/send/${enc(type)}/e2e${System.nanoTime()}", content, tok).let { (c, r) -> if (c == 200) r.optString("event_id") else null }
    private fun msgs(tok: String, room: String): List<JSONObject> {
        val (c, r) = call("GET", "/rooms/${enc(room)}/messages?dir=b&limit=100", null, tok)
        if (c != 200) return emptyList()
        val a = r.getJSONArray("chunk")
        return (0 until a.length()).map { a.getJSONObject(it) }.filter { it.optString("type") == "m.room.message" && it.optJSONObject("content")?.has("body") == true }
    }
    private fun bodies(tok: String, room: String) = msgs(tok, room).map { it.getJSONObject("content").getString("body") }
    private fun register(user: String, pw: String, name: String): JSONObject? {
        val body = JSONObject().put("username", user).put("password", pw).put("initial_device_display_name", "omm e2e app")
        var (code, r) = call("POST", "/register", body)
        repeat(3) {
            if (code != 401) return@repeat
            val done = r.optJSONArray("completed")?.let { a -> List(a.length()) { a.getString(it) } } ?: emptyList()
            val auth = if ("m.login.registration_token" !in done)
                JSONObject().put("type", "m.login.registration_token").put("token", BuildConfig.OMM_REG_TOKEN).put("session", r.optString("session"))
            else JSONObject().put("type", "m.login.dummy").put("session", r.optString("session"))
            call("POST", "/register", JSONObject(body.toString()).put("auth", auth)).let { code = it.first; r = it.second }
        }
        if (code != 200) return null
        call("PUT", "/profile/${enc(r.getString("user_id"))}/displayname", JSONObject().put("displayname", name), r.getString("access_token"))
        return r
    }

    // ── result rows + state (cleanup) ➡ priv/matrix_e2e_app.json, written aftr evy step so a crash still leaves th ids ──
    private val stamp = SimpleDateFormat("MMdd-HHmm").format(Date())
    private val state = JSONObject().put("stamp", stamp).put("users", JSONObject()).put("rooms", JSONObject())
        .put("aliases", JSONArray()).put("rows", JSONArray())
    private fun save() = File(System.getProperty("liveDir") ?: "build", "matrix_e2e_app_$stamp.json").let { f ->
        f.parentFile?.mkdirs(); f.writeText(state.toString(1)); f.setReadable(false, false); f.setReadable(true, true)
    }
    private var bad = 0
    private fun step(name: String, ok: Boolean, note: String = ""): Boolean {
        state.getJSONArray("rows").put(JSONArray().put(name).put(ok).put(note)); if (!ok) bad++
        println("${if (ok) "✔️" else "❌"} $name ${if (note.isNotBlank()) "· $note" else ""}")
        save(); return ok
    }

    // ── UI helpers (= ReaderGestureTest) ──
    private fun has(t: String, exact: Boolean = false) = rule.onAllNodesWithText(t, substring = !exact).fetchSemanticsNodes().isNotEmpty()
    private fun tap(t: String, exact: Boolean = false) {
        rule.waitUntil(30_000) { has(t, exact) }
        rule.onAllNodesWithText(t, substring = !exact).onFirst().performClick()
    }
    private fun settle() { rule.mainClock.advanceTimeBy(1500); rule.waitForIdle() }
    /** network i th app = real IO threads ➡ poll w real ti, not th paused Compose clock */
    private fun until(ms: Long = 30_000, cond: () -> Boolean): Boolean {
        val end = System.currentTimeMillis() + ms
        while (System.currentTimeMillis() < end) { rule.waitForIdle(); if (cond()) return true; Thread.sleep(300) }
        rule.waitForIdle(); return cond()
    }
    private fun back() = rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
    /** DsField's label is a Text above th field (not i its semantics) ➡ by position: Circle tab 0 = circle name · 1 = code */
    private fun field(i: Int) = rule.onAllNodes(hasSetTextAction())[i]

    @Test fun inAppPathVsLiveSynapse() {
        val vm = ViewModelProvider(rule.activity)[AppVm::class.java]
        val app = rule.activity.application as MarxApp
        val myName = "e2e Alice"
        rule.waitUntil(30_000) { has("Today") }

        // B 1st (= a friend alr on Element)
        val bPw = hex(16)
        val b = register("e2e-$stamp-b", bPw, "e2e Bob")
        if (!step("B signs up w th app token", b != null, b?.optString("user_id") ?: "")) return finish()
        val bTok = b!!.getString("access_token")
        state.getJSONObject("users").put("b", JSONObject().put("user_id", b.getString("user_id")).put("pw", bPw).put("token", bTok).put("name", "e2e Bob"))
        save()

        // ① 📖 ch 8 ➡ Note ➡ 👥 chip ➡ tap away
        runBlocking { app.prefs.setMxName(myName) }   // = ⚙️ Name field (only there, QA r5 #8)
        tap("Read", true)
        rule.onAllNodes(hasScrollToIndexAction()).onFirst().performScrollToNode(hasText("p. 245"))
        tap("p. 245", true); settle()
        val first = "We started with the supposition"
        rule.waitUntil(30_000) { has(first) }
        rule.onAllNodesWithText(first, substring = true).onFirst().performTouchInput { doubleClick() }; settle()
        tap("Note", true); settle()
        val txt = "e2e app comment $stamp (deleted by th test)"
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput(txt); settle()
        tap("also post to the reading room"); settle()
        tap("The Working-Day"); settle()   // slim head title = outside th note ➡ commit (tapOutsideNote)
        val posted = until { vm.comments.value["8"].orEmpty().any { it.text.endsWith(txt) } }
        val me = runBlocking { app.prefs.mxSession() }
        if (me != null) {
            state.getJSONObject("users").put("app", JSONObject().put("user_id", me.userId).put("pw", runBlocking { app.prefs.mxPw() })
                .put("token", me.token).put("name", myName))
            save()
        }
        if (!step("app: Note + 👥 chip ➡ own account + comment", posted && me != null, me?.userId ?: "no session")) return finish()

        // ② server side
        val (cj, rj) = call("POST", "/join/${enc("#omm-kap-8:scagent.de")}", JSONObject(), bTok)
        val room = rj.optString("room_id")
        state.getJSONObject("rooms").put("kap", room); save()
        val seen = msgs(bTok, room).firstOrNull { it.getJSONObject("content").getString("body").endsWith(txt) }?.getJSONObject("content")
        step("server: B reads it (txt · page · name)", cj == 200 && seen != null && seen.optInt("de.ohmymarx.page") == 245 &&
            seen.optString("de.ohmymarx.name") == myName, seen?.optString("body")?.take(40) ?: "HTTP $cj")
        val reply = "e2e Bob replies $stamp"
        val replyId = send(bTok, room, JSONObject().put("msgtype", "m.text").put("body", reply).put("de.ohmymarx.page", 245).put("de.ohmymarx.name", "e2e Bob"))
        rule.onAllNodesWithText("💬", substring = true).onFirst().performClick(); settle()   // slim head 💬 N ➡ sheet (reloads)
        step("app 💬 sheet shows B's reply", until { has(reply) }, if (replyId == null) "B send failed" else "")
        // th private margin note (same txt) sits under th sheet ➡ target th comment = "¶ 8,1 — txt"
        val mine = "¶ 8,1 — $txt"
        rule.onAllNodesWithText(mine, substring = true).onFirst().performTouchInput { longClick() }   // hold own = delete
        val gone = until { !has(mine) } && until { bodies(bTok, room).none { it.endsWith(txt) } }
        step("app: hold own ➡ deleted ➡ gone 4 B", gone)
        replyId?.let { call("PUT", "/rooms/${enc(room)}/redact/${enc(it)}/e2e${System.nanoTime()}", JSONObject(), bTok) }
        rule.onAllNodesWithContentDescription("Close").onFirst().performClick(); settle()
        back(); settle()

        // ③ 👥 Circle: found ➡ B joins ➡ chat both ways
        tap("Circle", true); settle()
        val kname = "e2e Kreis $stamp"
        field(0).performTextInput(kname); settle()
        tap("Found", true)
        val founded = until { vm.kreise.value.any { it.name == kname } }
        val k = vm.kreise.value.firstOrNull { it.name == kname }
        if (k != null) {
            val alias = "#omm-kreis-${k.code.lowercase()}:scagent.de"
            state.getJSONObject("rooms").put("kreis", k.room); state.getJSONArray("aliases").put(JSONArray().put(alias).put("app")); save()
        }
        if (!step("app founds Kreis", founded && has("Circle founded"), k?.code ?: "")) return finish()
        val (cb, rb) = call("POST", "/join/${enc("#omm-kreis-${k!!.code.lowercase()}:scagent.de")}", JSONObject(), bTok)
        val (_, nm) = call("GET", "/rooms/${enc(k.room)}/state/m.room.name", null, bTok)
        step("B joins by code · sees name", cb == 200 && rb.optString("room_id") == k.room && nm.optString("name") == kname, "code ${k.code}")
        tap(kname, true); settle()
        val hi = "Hallo Bob, hier Alice aus der App ($stamp)"
        field(0).performTextInput(hi); settle()
        tap("Send", true)
        step("app ➡ B chat", until { bodies(bTok, k.room).contains(hi) })
        val back2 = "Hallo Alice, Bob aus Element ($stamp)"
        send(bTok, k.room, JSONObject().put("msgtype", "m.text").put("body", back2).put("de.ohmymarx.name", "e2e Bob"))
        tap("Refresh", true)
        step("B ➡ app chat (Refresh)", until { has(back2) })
        step("app: members = both", until { vm.kreisMembers.value[k.room].orEmpty().values.toSet().containsAll(setOf(myName, "e2e Bob")) },
            vm.kreisMembers.value[k.room].orEmpty().values.joinToString(" · "))
        back(); settle()

        // ③b B founds ➡ app joins w th code
        val code2 = (1..6).map { "ABCDEFGHJKMNPQRSTUVWXYZ23456789"[SecureRandom().nextInt(31)] }.joinToString("")
        val bname = "e2e Bob Kreis $stamp"
        val (cc, rc) = call("POST", "/createRoom", JSONObject().put("name", bname).put("room_alias_name", "omm-kreis-${code2.lowercase()}")
            .put("visibility", "private").put("preset", "public_chat").put("topic", "👥 Oh My Marx Lesekreis · Code $code2 · e2e tst"), bTok)
        if (cc == 200) {
            state.getJSONObject("rooms").put("kreis_b", rc.getString("room_id"))
            state.getJSONArray("aliases").put(JSONArray().put("#omm-kreis-${code2.lowercase()}:scagent.de").put("b")); save()
        }
        field(1).performTextInput(code2); settle()
        rule.onAllNodesWithText("Join", substring = false).onFirst().performClick()
        step("app joins B's Kreis w code", cc == 200 && until { vm.kreise.value.any { it.code == code2 && it.name == bname } } && until { has(bname) },
            if (cc != 200) "B createRoom HTTP $cc" else code2)

        // ④ 🗑️ D-072: ⚙️ Delete account ➡ confirm ➡ server: app user gone fr th Kreis + its token dead
        //   test hygiene 1st: a deleted user's Kreis alias stays (right: friends still join by it) but no one can drop it then
        val myAlias = "#omm-kreis-${k.code.lowercase()}:scagent.de"
        if (call("DELETE", "/directory/room/${enc(myAlias)}", null, me!!.token).first == 200) {
            val a = state.getJSONArray("aliases"); (a.length() - 1 downTo 0).filter { a.getJSONArray(it).getString(0) == myAlias }.forEach { a.remove(it) }; save()
        }
        tap("My Capital", true); settle()
        rule.onAllNodesWithContentDescription("Settings").onFirst().performClick(); settle()
        rule.waitUntil(30_000) { has("Delete account", true) }   // ⚙️ = 1 verticalScroll Column ➡ all composed, just off-screen
        rule.onAllNodesWithText("Delete account", substring = false).onFirst().performScrollTo(); settle()
        tap("Delete account", true); settle()
        tap("Delete", true)
        val deleted = until { has("Account deleted") } && runBlocking { app.prefs.mxSession() } == null && vm.kreise.value.isEmpty()
        val meId = me!!.userId
        val (cw, _) = call("GET", "/account/whoami", null, me.token)
        val left = until { call("GET", "/rooms/${enc(k.room)}/joined_members", null, bTok).second.optJSONObject("joined")?.has(meId) == false }
        step("app: ⚙️ Delete account ➡ token dead · left Kreis", deleted && cw == 401 && left, "whoami HTTP $cw")
        // D-076: continuwuity ignores `erase` ➡ th app redacts 1st · B must no longer read th app's Kreis msg
        step("server: app's Kreis msg redacted 4 B (D-076)", bodies(bTok, k.room).none { it == hi })
        finish()
    }

    private fun finish() = assertEquals("❌ steps (table: bin/matrix_e2e.py --app)", 0, bad)
    private fun hex(n: Int) = ByteArray(n).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
}
