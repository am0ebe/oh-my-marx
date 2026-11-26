package appkit.selfupdate

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.work.ListenableWorker
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
 * ⚙️ evy app-specific bit o th updater · set once i Application.onCreate: `Update.init(UpdateConfig(…))` (b4 schedule())
 *  web        stable channel = `<web>version.json` + th apk next 2 it · DEV app = `<web>dev/` (ends w "/")
 *  dev        this build = th DEV app (applicationId ends ".dev") ➡ own channel
 *  appName    i notifications ("🆕 <appName> 1.2.0 is out")
 *  icon       small notification icon (drawable res of th app)
 *  worker     th app's `class UpdateWorker(c, p) : SelfUpdateWorker(c, p)` · own class name = WorkManager keeps finding it
 *  openUpdate notification tap ➡ th app's update dialog (an Intent 2 its activity) · openApp = aftr a silent update
 *  de / auto  app lang = German? · ⚙️ "update automatically" on? (both: read th app's settings, ≤ 0.5 s)
 *             auto = th STORED toggle, null = usr nvr touched it ➡ autoDefault · app's ⚙️ row: `Update.autoOn(stored)`
 *  autoDefault th toggle's dflt (omm D-083 @1126: OFF, was ON D-078) · a usr who set it ON/OFF keeps it
 *  onReplaced extra work aftr evy self-update (MY_PACKAGE_REPLACED), b4 th "installed ✔️" notification
 *  prefs/work/sessionName = storage names · keep them once shipped (renaming = lost nag state + a 2nd periodic job)
 */
class UpdateConfig(
    val web: String,
    val versionCode: Int,
    val versionName: String,
    val dev: Boolean,
    val appName: String,
    val icon: Int,
    val worker: Class<out ListenableWorker>,
    val openUpdate: (Context) -> Intent,
    val openApp: (Context) -> Intent,
    val de: (Context) -> Boolean = { false },
    val auto: (Context) -> Boolean? = { null },
    val onReplaced: (Context) -> Unit = {},
    val prefs: String = "selfupdate",
    val work: String = "selfupdate",
    val sessionName: String = "update.apk",
    val autoDefault: Boolean = Update.AUTO_DEFAULT,
)

/**
 * 🆕 self-update 4 ppl who installed th APK by hand (outside a store).
 *  1) version.json next 2 th download page (apptool writes it on evy deploy/ship: version · code · apk · sha256)
 *     stable app ➡ <web>/version.json · DEV app (.dev) ➡ <web>/dev/version.json = th channel it came from
 *  2) checked on app start (th app's banner) + ~daily i th background (SelfUpdateWorker)
 *  3) by installer (Via): Obtainium ➡ poke it (obtainium://refresh) · self ➡ download (sha256 checked) + PackageInstaller
 *     session: 1st ti 1 confirm, then WE r installer o record ➡ Android 12+ installs silently
 *     same signing key ➡ installs over th old app, data stays
 * Skipped when an app store installed us (it updates th app itself).
 * Fails soft evywhere: offline / garbage / 404 = no update, nvr a crash.
 */
object Update {
    private lateinit var cfg: UpdateConfig
    fun init(c: UpdateConfig) { cfg = c }
    val config: UpdateConfig get() = cfg

    /** ⚙️ "update automatically" dflt 4 evy app (omm D-083 @1126: OFF · silent install failed on GrapheneOS, banner ➡ Install = th main path) */
    const val AUTO_DEFAULT = false
    /** stored toggle ➡ effective · null = nvr touched ➡ th app's autoDefault (o AUTO_DEFAULT b4 init) */
    fun autoOn(stored: Boolean?): Boolean = stored ?: if (::cfg.isInitialized) cfg.autoDefault else AUTO_DEFAULT

    val page get() = cfg.web

    /** 🧪 DEV app = own channel (dev builds on <web>/dev/) */
    val DEV get() = cfg.dev

    /** where this app's channel lives (version.json + th apk) */
    fun base(dev: Boolean = DEV) = if (dev) cfg.web + "dev/" else cfg.web

    data class Latest(val version: String, val code: Int, val apk: String? = null, val sha256: String? = null)

    /** app stores th update th app by themselves, nothing 2 do (F-Droid · Neo Store · Droid-ify · Aurora · Play) */
    private val STORES = listOf("org.fdroid.", "com.machiav3lli.fdroid", "com.looker.droidify", "com.aurora.store", "com.android.vending")
    private const val OBTAINIUM = "dev.imranr.obtainium"   // + its ".fdroid" flavour

    /**
     * who brings th next version:
     *  OBTAINIUM = it installed us ➡ we only poke it (`obtainium://refresh?id=…`) · its own bg updates (6 h) usually win
     *  SELF = browser / file manager / adb / our own earlier self-update ➡ we download + install (silent once we're installer)
     *  STORE = a store ➡ nothing
     */
    enum class Via { OBTAINIUM, SELF, STORE }

    /** 🧪 seam */
    fun via(installer: String?): Via = when {
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
    fun silentOk(installer: String?, me: String, sdk: Int) = sdk >= 31 && installer == me
    fun silentOk(ctx: Context) = silentOk(installer(ctx), ctx.packageName, Build.VERSION.SDK_INT)

    /** 🔄 Obtainium: check this 1 app now (wiki "Deep Links": obtainium://refresh?id=<app id>, id = package) */
    fun obtainiumRefresh(ctx: Context): Intent =
        Intent(Intent.ACTION_VIEW, ("obtainium://refresh?id=" + ctx.packageName).toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** 🧪 DEV app only: newest build on <web>/dev/ (null = offline / not DEV) · th app can say honestly if it's stale */
    suspend fun devLatest(): Latest? = withContext(Dispatchers.IO) {
        if (!DEV) return@withContext null
        runCatching { parse(fetchText(base(true) + "version.json")) }.getOrNull()
    }

    private fun open(url: String): HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 8000; readTimeout = 15000
        setRequestProperty("Cache-Control", "no-cache")
    }

    fun fetchText(url: String): String = open(url).inputStream.bufferedReader().use { it.readText() }

    /** ❌ last install failure (InstallReceiver) ➡ th app's update dialog shows it + th web fallback */
    val installError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    /** 🧪 seam (JVM-testable, kotlinx json: org.json is stubbed in unit tests) · throws on garbage / missing version */
    fun parse(text: String): Latest {
        val o = Json.parseToJsonElement(text).jsonObject
        fun str(k: String) = o[k]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
        return Latest(o.getValue("version").jsonPrimitive.content, o["code"]?.jsonPrimitive?.intOrNull ?: 0,
            str("apk")?.takeIf { safeName(it) }, str("sha256")?.lowercase())
    }

    /** apk = a plain file name next 2 version.json (no path, no scheme) ➡ a tampered json can't point us elsewhere */
    fun safeName(apk: String) = Regex("[A-Za-z0-9._+-]+\\.apk").matches(apk)

    /** 🧪 seam: newer than `installed` ➡ Latest · older / equal / garbage / fetch throws (offline) ➡ null */
    fun newer(installed: Int, get: () -> String): Latest? =
        runCatching { parse(get()).takeIf { it.code > installed } }.getOrNull()

    /** newer than what's installed ➡ Latest, else null · `get` = 🧪 fake version.json i tests */
    suspend fun check(ctx: Context, get: (String) -> String = ::fetchText): Latest? = withContext(Dispatchers.IO) {
        if (!wanted(ctx)) return@withContext null
        newer(cfg.versionCode) { get(base() + "version.json") }
    }

    /** 🔔 bg nag rule: a NEW version ➡ notify now · same version still not installed ➡ again evy RENAG days (not daily spam) */
    const val RENAG = 2
    fun shouldNotify(code: Int, lastCode: Int, lastDay: Long, today: Long) = code != lastCode || today - lastDay >= RENAG

    // ── download + install ──

    fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }

    fun sha256(f: File): String = MessageDigest.getInstance("SHA-256").let { md ->
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

    /** th usr must allow "install unknown apps" 4 us once (minSdk 26 = alw asked) */
    fun canInstall(ctx: Context) = ctx.packageManager.canRequestPackageInstalls()

    /** ⚙️ th system page where th usr allows it (comes back 2 th app aft) */
    fun allowIntent(ctx: Context) = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, ("package:" + ctx.packageName).toUri())

    /**
     * 📦 Android's install dialog 4 th downloaded apk · fallback only · needs th app's FileProvider
     * `${applicationId}.files` w `<cache-path name="update" path="update/" />` (README § adopt)
     */
    fun installIntent(ctx: Context, apk: File): Intent = Intent(Intent.ACTION_VIEW)
        .setDataAndType(FileProvider.getUriForFile(ctx, ctx.packageName + ".files", apk), "application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * 📦 install via a PackageInstaller session = WE become th installer o record ➡ nxt update can be silent (Android 12+).
     * always asks 4 USER_ACTION_NOT_REQUIRED: not eligible (1st ti, < 12) ➡ Android asks th usr anyway (InstallReceiver
     * shows its dialog) · result ➡ InstallReceiver · `bg` = no screen 2 show a dialog on ➡ notification instead
     */
    fun installSession(ctx: Context, apk: File, bg: Boolean) {
        val pi = ctx.packageManager.packageInstaller
        val p = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(ctx.packageName)
            if (Build.VERSION.SDK_INT >= 31) setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        val id = pi.createSession(p)
        pi.openSession(id).use { s ->
            s.openWrite(cfg.sessionName, 0, apk.length()).use { o -> apk.inputStream().use { it.copyTo(o) }; s.fsync(o) }
            val i = Intent(ctx, InstallReceiver::class.java).putExtra(EXTRA_BG, bg)
            // MUTABLE: th system adds status + th confirm intent 2 it (explicit intent ➡ safe)
            s.commit(PendingIntent.getBroadcast(ctx, id, i, PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT).intentSender)
        }
    }
    const val EXTRA_BG = "bg"
}
