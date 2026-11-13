package de.strix.marxversion.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import de.strix.marxversion.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * 🆕 self-update 4 friends who installed th APK by hand (QA r5 @1113: "must nvr sit on a stale version").
 *  1) version.json next 2 th download page (bin/lib.sh writes it on evy deploy/ship: version · code · apk · sha256)
 *     stable app ➡ <web>/version.json · DEV app (.dev) ➡ <web>/dev/version.json = th channel it came from
 *  2) checked on app start (Today banner) + ~daily i th background (notify/UpdateWorker.kt)
 *  3) by installer (Via): Obtainium ➡ poke it (obtainium://refresh) · self ➡ download (sha256 checked) + PackageInstaller
 *     session: 1st ti 1 confirm, then WE r installer o record ➡ Android 12+ installs silently (D-023)
 *     same signing key ➡ installs over th old app, data stays
 * Skipped when an app store installed us (it updates th app itself).
 * Fails soft evywhere: offline / garbage / 404 = no update, nvr a crash.
 */
object Update {
    private const val WEB = "https://scagent.de/oh-my-marx/"
    const val page = WEB

    /** 🧪 DEV app = own channel (dev builds on <web>/dev/) */
    val DEV = BuildConfig.APPLICATION_ID.endsWith(".dev")

    /** where this app's channel lives (version.json + th apk) */
    fun base(dev: Boolean = DEV) = if (dev) WEB + "dev/" else WEB

    data class Latest(val version: String, val code: Int, val apk: String? = null, val sha256: String? = null)

    /** app stores th update th app by themselves, nothing 2 do (F-Droid · Neo Store · Droid-ify · Aurora · Play) */
    private val STORES = listOf("org.fdroid.", "com.machiav3lli.fdroid", "com.looker.droidify", "com.aurora.store", "com.android.vending")
    private const val OBTAINIUM = "dev.imranr.obtainium"   // + its ".fdroid" flavour

    /**
     * who brings th next version (am0 @1113: "use Obtainium if it's there, else th website apk"):
     *  OBTAINIUM = it installed us ➡ we only poke it (`obtainium://refresh?id=…`) · its own bg updates (6 h) usually win
     *  SELF = browser / file manager / adb / our own earlier self-update ➡ we download + install (silent once we're installer)
     *  STORE = a store ➡ nothing
     */
    enum class Via { OBTAINIUM, SELF, STORE }

    /** 🧪 seam */
    internal fun via(installer: String?): Via = when {
        installer == null -> Via.SELF
        installer.startsWith(OBTAINIUM) -> Via.OBTAINIUM
        STORES.any { installer.startsWith(it) } -> Via.STORE
        else -> Via.SELF
    }

    /** installer o record (null = adb / unknown) */
    fun installer(ctx: Context): String? = runCatching {
        if (Build.VERSION.SDK_INT >= 30) ctx.packageManager.getInstallSourceInfo(ctx.packageName).installingPackageName
        else @Suppress("DEPRECATION") ctx.packageManager.getInstallerPackageName(ctx.packageName)
    }.getOrNull()

    fun via(ctx: Context) = via(installer(ctx))

    /** banner / notification makes sense unless a store handles it */
    fun wanted(ctx: Context): Boolean = via(ctx) != Via.STORE

    /** 🤫 Android 12+ installs w/o asking only if WE r th installer o record (= aft our 1st, confirmed self-update) */
    internal fun silentOk(installer: String?, me: String, sdk: Int) = sdk >= 31 && installer == me
    fun silentOk(ctx: Context) = silentOk(installer(ctx), ctx.packageName, Build.VERSION.SDK_INT)

    /** 🔄 Obtainium: check this 1 app now (wiki "Deep Links": obtainium://refresh?id=<app id>, id = package) */
    fun obtainiumRefresh(ctx: Context): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse("obtainium://refresh?id=" + ctx.packageName)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** Obtainium users: its bg check (6 h) gets a head start ➡ we only nag aft GRACE_H hours o th same version */
    const val GRACE_H = 8

    /** 🧪 DEV app only: newest build on <web>/dev/ (null = offline / not DEV) · th app says honestly if it's stale (@1107) */
    suspend fun devLatest(): Latest? = withContext(Dispatchers.IO) {
        if (!DEV) return@withContext null
        runCatching { parse(fetchText(base(true) + "version.json")) }.getOrNull()
    }

    private fun open(url: String): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 8000; readTimeout = 15000
        setRequestProperty("Cache-Control", "no-cache")
    }

    internal fun fetchText(url: String): String = open(url).inputStream.bufferedReader().use { it.readText() }

    /** ❌ last install failure (notify/InstallReceiver) ➡ UpdateDialog shows it + th web fallback */
    val installError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    /** 🧪 seam (JVM-testable, kotlinx json: org.json is stubbed in unit tests) · throws on garbage / missing version */
    internal fun parse(text: String): Latest {
        val o = Json.parseToJsonElement(text).jsonObject
        fun str(k: String) = o[k]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        return Latest(o.getValue("version").jsonPrimitive.content, o["code"]?.jsonPrimitive?.intOrNull ?: 0,
            str("apk")?.takeIf { safeName(it) }, str("sha256")?.lowercase())
    }

    /** apk = a plain file name next 2 version.json (no path, no scheme) ➡ a tampered json can't point us elsewhere */
    internal fun safeName(apk: String) = Regex("[A-Za-z0-9._+-]+\\.apk").matches(apk)

    /** 🧪 seam: newer than `installed` ➡ Latest · older / equal / garbage / fetch throws (offline) ➡ null */
    internal fun newer(installed: Int, get: () -> String): Latest? =
        runCatching { parse(get()).takeIf { it.code > installed } }.getOrNull()

    /** newer than what's installed ➡ Latest, else null · `get` = 🧪 fake version.json i tests */
    suspend fun check(ctx: Context, get: (String) -> String = ::fetchText): Latest? = withContext(Dispatchers.IO) {
        if (!wanted(ctx)) return@withContext null
        newer(BuildConfig.VERSION_CODE) { get(base() + "version.json") }
    }

    /** 🔔 bg nag rule: a NEW version ➡ notify now · same version still not installed ➡ again evy RENAG days (not daily spam) */
    const val RENAG = 2
    internal fun shouldNotify(code: Int, lastCode: Int, lastDay: Long, today: Long) = code != lastCode || today - lastDay >= RENAG

    // ── download + install ──

    fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }

    internal fun sha256(f: File): String = MessageDigest.getInstance("SHA-256").let { md ->
        f.inputStream().use { s -> val buf = ByteArray(1 shl 16); while (true) { val n = s.read(buf); if (n < 0) break; md.update(buf, 0, n) } }
        hex(md.digest())
    }

    /** ⬇️ th apk ➡ cache/update/ · progress 0..1 (-1 = size unknown) · wrong sha256 ➡ file deleted + IOException */
    suspend fun download(ctx: Context, l: Latest, progress: (Float) -> Unit = {}): File = withContext(Dispatchers.IO) {
        val name = l.apk ?: throw IOException("no apk in version.json")
        val dir = File(ctx.cacheDir, "update").apply { mkdirs(); listFiles()?.forEach { it.delete() } }
        val out = File(dir, name)
        val c = open(base() + name)
        val total = c.contentLengthLong
        c.inputStream.use { inp ->
            out.outputStream().use { o ->
                val buf = ByteArray(1 shl 16); var done = 0L
                while (true) {
                    val n = inp.read(buf); if (n < 0) break
                    o.write(buf, 0, n); done += n
                    progress(if (total > 0) done.toFloat() / total else -1f)
                }
            }
        }
        if (l.sha256 != null && sha256(out) != l.sha256) { out.delete(); throw IOException("sha256 mismatch") }
        out
    }

    /** Android 8+: th usr must allow "install unknown apps" 4 us once */
    fun canInstall(ctx: Context) = Build.VERSION.SDK_INT < 26 || ctx.packageManager.canRequestPackageInstalls()

    /** ⚙️ th system page where th usr allows it (comes back 2 th app aft) */
    fun allowIntent(ctx: Context) = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + ctx.packageName))

    /** 📦 Android's install dialog 4 th downloaded apk (FileProvider ➡ res/xml/file_paths.xml "update") · fallback only */
    fun installIntent(ctx: Context, apk: File): Intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(FileProvider.getUriForFile(ctx, ctx.packageName + ".files", apk), "application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * 📦 install via a PackageInstaller session = WE become th installer o record ➡ nxt update can be silent (Android 12+).
     * always asks 4 USER_ACTION_NOT_REQUIRED: not eligible (1st ti, < 12) ➡ Android asks th usr anyway (InstallReceiver
     * shows its dialog) · result ➡ notify/InstallReceiver.kt · `bg` = no screen 2 show a dialog on ➡ notification instead
     */
    fun installSession(ctx: Context, apk: File, bg: Boolean) {
        val pi = ctx.packageManager.packageInstaller
        val p = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(ctx.packageName)
            if (Build.VERSION.SDK_INT >= 31) setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val id = pi.createSession(p)
        pi.openSession(id).use { s ->
            s.openWrite("omm.apk", 0, apk.length()).use { o -> apk.inputStream().use { it.copyTo(o) }; s.fsync(o) }
            val i = Intent(ctx, de.strix.marxversion.notify.InstallReceiver::class.java).putExtra(EXTRA_BG, bg)
            // MUTABLE: th system adds status + th confirm intent 2 it (explicit intent ➡ safe)
            s.commit(PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT).intentSender)
        }
    }
    const val EXTRA_BG = "bg"
}
