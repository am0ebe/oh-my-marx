package appkit.selfupdate

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// 🆕 "new version" i th background:
//   WorkManager ~daily (network) + once on evy app start ➡ own channel's version.json ➡ by installer (Update.Via):
//     OBTAINIUM ➡ notification at once, tap = obtainium://refresh
//     SELF + "auto-update" ON (UpdateConfig.auto) + we r installer o record (Android 12+) ➡ download + install SILENTLY · UpdatedReceiver says "installed ✔️"
//     SELF otherwise ➡ notification ➡ tap = th app's update dialog (UpdateConfig.openUpdate: download + 1 confirm ➡ we become installer)
//     STORE ➡ nothing
//   nag: new version ➡ at once · same one still not installed ➡ again evy Update.RENAG days

private const val CHANNEL = "update"
const val NOTIF_UPDATE = 30
private val cfg get() = Update.config

/** th app subclasses this (own class name, UpdateConfig.worker) ➡ WorkManager's stored job keeps resolving */
open class SelfUpdateWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        checkAndNotify(applicationContext)
        return Result.success()   // offline / garbage = just try again tomorrow (no retry storm)
    }
}

enum class Outcome { NONE, NOTIFIED, SILENT }

/** 🧪 seam: fake version.json · installer · sdk · clock · silent installer */
suspend fun checkAndNotify(
    ctx: Context,
    get: ((String) -> String)? = null,
    installer: String? = Update.installer(ctx),
    sdk: Int = Build.VERSION.SDK_INT,
    now: Long = System.currentTimeMillis(),
    silent: suspend (Update.Latest) -> Unit = { l -> Update.installSession(ctx, Update.download(ctx, l), bg = true) },
    auto: Boolean = runCatching { cfg.auto(ctx) }.getOrDefault(false),
): Outcome {
    val via = Update.via(installer)
    if (via == Update.Via.STORE) return Outcome.NONE
    val fetch = get ?: Update::fetchText
    val l = Update.newer(cfg.versionCode) { fetch(Update.base() + "version.json") } ?: return Outcome.NONE
    val sp = ctx.getSharedPreferences(cfg.prefs, Context.MODE_PRIVATE)
    if (sp.getInt("seen_code", 0) != l.code) sp.edit { putInt("seen_code", l.code).putLong("seen_at", now) }
    val de = isDe(ctx)

    if (auto && via == Update.Via.SELF && l.apk != null && Update.silentOk(installer, ctx.packageName, sdk)) {
        sp.edit { putString("silent", l.version) }   // ➡ UpdatedReceiver aft th swap
        if (runCatching { silent(l) }.isSuccess) return Outcome.SILENT
        sp.edit { remove("silent") }                 // download/install failed ➡ fall back 2 asking
    }
    val today = LocalDate.now().toEpochDay()
    if (!Update.shouldNotify(l.code, sp.getInt("code", 0), sp.getLong("day", Long.MIN_VALUE / 2), today)) return Outcome.NONE
    val tap = if (via == Update.Via.OBTAINIUM) Update.obtainiumRefresh(ctx) else cfg.openUpdate(ctx)
    val text = when {
        via == Update.Via.OBTAINIUM -> if (de) "Antippen = in Obtainium aktualisieren" else "Tap = update in Obtainium"
        else -> if (de) "Antippen = laden + installieren · deine Daten bleiben" else "Tap = download + install · your data stays"
    }
    val title = if (de) "🆕 ${cfg.appName} %s ist da".format(l.version) else "🆕 ${cfg.appName} %s is out".format(l.version)
    if (!post(ctx, title, text, tap, de)) return Outcome.NONE
    sp.edit { putInt("code", l.code).putLong("day", today) }
    return Outcome.NOTIFIED
}

private fun isDe(ctx: Context) = runCatching { cfg.de(ctx) }.getOrDefault(false)

private fun post(ctx: Context, title: String, text: String, tap: Intent, de: Boolean, quiet: Boolean = false): Boolean {
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
    val ch = NotificationChannel(CHANNEL, if (de) "App-Updates" else "App updates", NotificationManager.IMPORTANCE_DEFAULT)
        .apply { description = if (de) "Neue Version von ${cfg.appName}" else "New version of ${cfg.appName}" }
    ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    val open = PendingIntent.getActivity(ctx, 4, tap, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val n = NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(cfg.icon).setSilent(quiet)
        .setContentTitle(title).setContentText(text).setContentIntent(open).setAutoCancel(true).build()
    NotificationManagerCompat.from(ctx).notify(NOTIF_UPDATE, n)
    return true
}

/** ~daily, only w network · KEEP = don't reset th timer on evy app start · + 1 check now */
fun scheduleUpdate(ctx: Context) {
    val net = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val req = PeriodicWorkRequest.Builder(cfg.worker, 1, TimeUnit.DAYS).setConstraints(net).build()
    WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(cfg.work, ExistingPeriodicWorkPolicy.KEEP, req)
    // a new version shows up at once, not up 2 a day later · same version = no re-nag (RENAG)
    val now = OneTimeWorkRequest.Builder(cfg.worker).setConstraints(net).build()
    WorkManager.getInstance(ctx).enqueueUniqueWork("${cfg.work}-now", ExistingWorkPolicy.REPLACE, now)
}

/**
 * 📦 PackageInstaller session result (Update.installSession) · needs th usr ➡ app on screen: Android's confirm dialog ·
 * bg: a notification (no screen 2 pop up on) · failure ➡ Update.installError (th app's dialog shows it + th web fallback)
 */
class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val bg = i.getBooleanExtra(Update.EXTRA_BG, false)
        when (val st = i.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= 33) i.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    else @Suppress("DEPRECATION") i.getParcelableExtra(Intent.EXTRA_INTENT)
                // usr left th app mid-download ➡ Android 10+ drops a bg activity start SILENTLY (no throw) ➡ only
                //   pop th confirm while we're on screen, else th notification below
                if (!bg && confirm != null && onScreen() &&
                    runCatching { ctx.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return
                val de = isDe(ctx)
                ctx.getSharedPreferences(cfg.prefs, Context.MODE_PRIVATE).edit { remove("silent") }
                post(ctx, if (de) "🆕 Update bereit" else "🆕 Update ready",
                    if (de) "Antippen = installieren" else "Tap = install", cfg.openUpdate(ctx), de)
            }
            PackageInstaller.STATUS_SUCCESS -> {}   // we r being replaced ➡ UpdatedReceiver takes over
            else -> {
                ctx.getSharedPreferences(cfg.prefs, Context.MODE_PRIVATE).edit { remove("silent") }
                Update.installError.value = (i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "") + " ($st)"
            }
        }
    }
}

/** an activity o ours is visible (= Android lets us start th install confirm) */
private fun onScreen(): Boolean = runCatching {
    android.app.ActivityManager.RunningAppProcessInfo().also { android.app.ActivityManager.getMyMemoryState(it) }.importance <=
        android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
}.getOrDefault(true)

/** ✔️ aft a SILENT update th new version starts here (MY_PACKAGE_REPLACED) ➡ quiet "installed X" so it's not a mystery */
class UpdatedReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        if (i.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        runCatching { cfg.onReplaced(ctx) }
        val sp = ctx.getSharedPreferences(cfg.prefs, Context.MODE_PRIVATE)
        sp.getString("silent", null) ?: return
        sp.edit { remove("silent") }
        val de = isDe(ctx)
        post(ctx, if (de) "✔️ ${cfg.appName} %s installiert".format(cfg.versionName) else "✔️ ${cfg.appName} %s installed".format(cfg.versionName),
            if (de) "Von selbst aktualisiert — deine Daten sind noch da" else "Updated by itself — your data is still there",
            cfg.openApp(ctx), de, quiet = true)
    }
}
