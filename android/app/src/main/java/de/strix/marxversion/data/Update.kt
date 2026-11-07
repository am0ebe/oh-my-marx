package de.strix.marxversion.data

import android.content.Context
import android.os.Build
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
 * Only 4 ppl who installed th APK by hand (@1106): Obtainium / F-Droid / a store update th app themselves ➡ no
 * 2nd notice · DEV app = Obtainium o adb only ➡ never · fails soft (offline = no banner).
 */
object Update {
    private const val WEB = "https://scagent.de/oh-my-marx/"
    const val page = WEB

    data class Latest(val version: String, val code: Int)

    /** installers th update th app by themselves (Obtainium · its F-Droid flavour · F-Droid · Neo Store · Droid-ify · Aurora · Play) */
    private val UPDATERS = listOf("dev.imranr.obtainium", "org.fdroid.", "com.machiav3lli.fdroid", "com.looker.droidify",
        "com.aurora.store", "com.android.vending")

    /** banner makes sense only if nothing else will tell th usr */
    fun wanted(ctx: Context): Boolean {
        if (BuildConfig.APPLICATION_ID.endsWith(".dev")) return false
        val by = runCatching {
            if (Build.VERSION.SDK_INT >= 30) ctx.packageManager.getInstallSourceInfo(ctx.packageName).installingPackageName
            else @Suppress("DEPRECATION") ctx.packageManager.getInstallerPackageName(ctx.packageName)
        }.getOrNull() ?: return true
        return UPDATERS.none { by.startsWith(it) }
    }

    /** 🧪 DEV app only: newest build on <web>/dev/ (null = offline / not DEV) · th app says honestly if it's stale (@1107) */
    suspend fun devLatest(): Latest? = withContext(Dispatchers.IO) {
        if (!BuildConfig.APPLICATION_ID.endsWith(".dev")) return@withContext null
        runCatching { fetch(page + "dev/version.json") }.getOrNull()
    }

    private fun fetch(url: String): Latest {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 5000; c.readTimeout = 5000
        c.setRequestProperty("Cache-Control", "no-cache")
        val o = c.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        return Latest(o.getString("version"), o.optInt("code"))
    }

    /** newer than what's installed ➡ Latest, else null */
    suspend fun check(ctx: Context): Latest? = withContext(Dispatchers.IO) {
        if (!wanted(ctx)) return@withContext null
        runCatching { fetch(page + "version.json").takeIf { it.code > BuildConfig.VERSION_CODE } }.getOrNull()
    }
}
