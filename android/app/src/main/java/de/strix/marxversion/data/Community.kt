package de.strix.marxversion.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 👥 "Mitlesen" = th community counter (server/omm_api.py on th VPS, behind Caddy).
 * Only called when th usr switched ⚙️ Mitlesen on. Sends: a random token that changes daily + page keys.
 * Evy call fails soft (null / false) ➡ offline o server down = th counters just don't show.
 * HttpURLConnection = no extra library 4 3 tiny JSON calls.
 */
object Community {
    const val BASE = "https://scagent.de/omm/api/v1"

    data class Stats(val reads: Map<String, Int>, val amenToday: Int, val readersToday: Int)

    /** key 4 a MEW page (book reader) */
    fun pageKey(page: Int) = "p:$page"

    suspend fun read(token: String, keys: List<String>): Boolean =
        post("/read", JSONObject().put("token", token).put("keys", JSONArray(keys.take(20))))

    suspend fun amen(token: String): Boolean = post("/amen", JSONObject().put("token", token))

    suspend fun stats(keys: List<String> = emptyList()): Stats? = withContext(Dispatchers.IO) {
        runCatching {
            val q = URLEncoder.encode(keys.take(20).joinToString("|"), "UTF-8")
            val c = open("/stats?keys=$q")
            c.inputStream.bufferedReader().use { r ->
                val o = JSONObject(r.readText())
                val reads = o.optJSONObject("reads")
                Stats(
                    reads = reads?.keys()?.asSequence()?.associateWith { reads.getInt(it) } ?: emptyMap(),
                    amenToday = o.optInt("amen_today"), readersToday = o.optInt("readers_today"),
                )
            }
        }.getOrNull()
    }

    private suspend fun post(path: String, body: JSONObject): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val c = open(path).apply {
                requestMethod = "POST"; doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            c.outputStream.use { it.write(body.toString().toByteArray()) }
            c.responseCode == 200
        }.getOrDefault(false)
    }

    private fun open(path: String) = (URL(BASE + path).openConnection() as HttpURLConnection).apply {
        connectTimeout = 4000; readTimeout = 4000
    }
}
