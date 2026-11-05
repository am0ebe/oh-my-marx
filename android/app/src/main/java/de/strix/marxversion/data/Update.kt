package de.strix.marxversion.data

import de.strix.marxversion.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 🆕 "new version available" check · no self-install (that needs the REQUEST_INSTALL_PACKAGES permission):
 * the app only reads version.json next to the download page (written by bin/lib.sh on every deploy/ship)
 * and, if newer, shows a banner that opens the page ➡ the user installs over the old app (same key = data kept).
 * Stable app ➡ stable page · DEV app ➡ /dev/ · fails soft (offline = no banner).
 */
object Update {
    private const val WEB = "https://scagent.de/oh-my-marx/"
    val page: String get() = if (BuildConfig.APPLICATION_ID.endsWith(".dev")) WEB + "dev/" else WEB

    data class Latest(val version: String, val code: Int)

    /** newer than what's installed ➡ Latest, else null */
    suspend fun check(): Latest? = withContext(Dispatchers.IO) {
        runCatching {
            val c = URL(page + "version.json").openConnection() as HttpURLConnection
            c.connectTimeout = 5000; c.readTimeout = 5000
            c.setRequestProperty("Cache-Control", "no-cache")
            val o = c.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            Latest(o.getString("version"), o.optInt("code")).takeIf { it.code > BuildConfig.VERSION_CODE }
        }.getOrNull()
    }
}
