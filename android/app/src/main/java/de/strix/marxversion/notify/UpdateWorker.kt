package de.strix.marxversion.notify

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
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.MainActivity
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// 🆕 "new version" i th background (QA r5 + am0 @1113 "Obtainium if it's there, else th website apk, as few taps as possible"):
//   WorkManager ~daily (network) ➡ own channel's version.json (stable o DEV) ➡ by installer (Update.Via):
//     OBTAINIUM ➡ its own 6 h bg update usually wins · still old aft GRACE_H ➡ notification, tap = obtainium://refresh
//     SELF + we r installer o record (Android 12+) ➡ download + install SILENTLY, no tap · UpdatedReceiver says "updated ✔️"
//     SELF otherwise (1st ti / < 12) ➡ notification ➡ tap = UpdateDialog (download + 1 confirm ➡ we become installer)
//     STORE ➡ nothing
//   nag: new version ➡ at once · same one still not installed ➡ again evy Update.RENAG days

private const val CHANNEL = "update"
private const val WORK = "marx_update"
const val NOTIF_UPDATE = 30
private const val SP = "omm_update"

class UpdateWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        checkAndNotify(applicationContext)
        return Result.success()   // offline / garbage = just try again tomorrow (no retry storm)
    }
}

enum class Outcome { NONE, NOTIFIED, SILENT }

/** 🧪 seam (UpdateWorkerTest): fake version.json · installer · sdk · clock · silent installer */
suspend fun checkAndNotify(
    ctx: Context,
    get: ((String) -> String)? = null,
    installer: String? = Update.installer(ctx),
    sdk: Int = Build.VERSION.SDK_INT,
    now: Long = System.currentTimeMillis(),
    silent: suspend (Update.Latest) -> Unit = { l -> Update.installSession(ctx, Update.download(ctx, l), bg = true) },
): Outcome {
    val via = Update.via(installer)
    if (via == Update.Via.STORE) return Outcome.NONE
    val fetch = get ?: Update::fetchText
    val l = Update.newer(BuildConfig.VERSION_CODE) { fetch(Update.base() + "version.json") } ?: return Outcome.NONE
    val sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE)
    if (sp.getInt("seen_code", 0) != l.code) sp.edit().putInt("seen_code", l.code).putLong("seen_at", now).apply()
    val de = isDe(ctx)

    if (via == Update.Via.SELF && l.apk != null && Update.silentOk(installer, ctx.packageName, sdk)) {
        sp.edit().putString("silent", l.version).apply()   // ➡ UpdatedReceiver aft th swap
        if (runCatching { silent(l) }.isSuccess) return Outcome.SILENT
        sp.edit().remove("silent").apply()                 // download/install failed ➡ fall back 2 asking
    }
    if (via == Update.Via.OBTAINIUM && now - sp.getLong("seen_at", now) < Update.GRACE_H * 3_600_000L) return Outcome.NONE
    val today = LocalDate.now().toEpochDay()
    if (!Update.shouldNotify(l.code, sp.getInt("code", 0), sp.getLong("day", Long.MIN_VALUE / 2), today)) return Outcome.NONE
    val tap = if (via == Update.Via.OBTAINIUM) Update.obtainiumRefresh(ctx) else appUpdateIntent(ctx)
    val text = when {
        via == Update.Via.OBTAINIUM -> if (de) "Antippen = in Obtainium aktualisieren" else "Tap = update in Obtainium"
        else -> if (de) "Antippen = laden + installieren · deine Daten bleiben" else "Tap = download + install · your data stays"
    }
    val title = if (de) "🆕 Oh My Marx %s ist da".format(l.version) else "🆕 Oh My Marx %s is out".format(l.version)
    if (!post(ctx, title, text, tap, de)) return Outcome.NONE
    sp.edit().putInt("code", l.code).putLong("day", today).apply()
    return Outcome.NOTIFIED
}

private fun appUpdateIntent(ctx: Context) = Intent(ctx, MainActivity::class.java)
    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.EXTRA_POP, "update")

/** app lang DE? (receivers + worker: DataStore read, ≤ 0.5 s) */
private fun isDe(ctx: Context): Boolean = runCatching {
    runBlocking { withTimeoutOrNull(500) { (ctx.applicationContext as MarxApp).prefs.settings.first().lang == Lang.DE } }
}.getOrNull() ?: false

private fun post(ctx: Context, title: String, text: String, tap: Intent, de: Boolean, quiet: Boolean = false): Boolean {
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
    val ch = NotificationChannel(CHANNEL, if (de) "App-Updates" else "App updates", NotificationManager.IMPORTANCE_DEFAULT)
        .apply { description = if (de) "Neue Version von Oh My Marx" else "New version of Oh My Marx" }
    ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    val open = PendingIntent.getActivity(ctx, 4, tap, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val n = NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(R.drawable.ic_update).setSilent(quiet)
        .setContentTitle(title).setContentText(text).setContentIntent(open).setAutoCancel(true).build()
    NotificationManagerCompat.from(ctx).notify(NOTIF_UPDATE, n)
    return true
}

/** ~daily, only w network · KEEP = don't reset th timer on evy app start */
fun scheduleUpdate(ctx: Context) {
    val req = PeriodicWorkRequestBuilder<UpdateWorker>(1, TimeUnit.DAYS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
    WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
}

/**
 * 📦 PackageInstaller session result (Update.installSession) · needs th usr ➡ app on screen: Android's confirm dialog ·
 * bg: a notification (no screen 2 pop up on) · failure ➡ Update.installError (UpdateDialog shows it + th web fallback)
 */
class InstallReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        val bg = i.getBooleanExtra(Update.EXTRA_BG, false)
        when (val st = i.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = if (Build.VERSION.SDK_INT >= 33) i.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                    else @Suppress("DEPRECATION") i.getParcelableExtra(Intent.EXTRA_INTENT)
                if (!bg && confirm != null && runCatching { ctx.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }.isSuccess) return
                val de = isDe(ctx)
                ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit().remove("silent").apply()
                post(ctx, if (de) "🆕 Update bereit" else "🆕 Update ready",
                    if (de) "Antippen = installieren" else "Tap = install", appUpdateIntent(ctx), de)
            }
            PackageInstaller.STATUS_SUCCESS -> {}   // we r being replaced ➡ UpdatedReceiver takes over
            else -> {
                ctx.getSharedPreferences(SP, Context.MODE_PRIVATE).edit().remove("silent").apply()
                Update.installError.value = (i.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "") + " ($st)"
            }
        }
    }
}

/** ✔️ aft a SILENT update th new version starts here (MY_PACKAGE_REPLACED) ➡ quiet "updated 2 X" so it's not a mystery */
class UpdatedReceiver : BroadcastReceiver() {
    override fun onReceive(ctx: Context, i: Intent) {
        if (i.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val sp = ctx.getSharedPreferences(SP, Context.MODE_PRIVATE)
        sp.getString("silent", null) ?: return
        sp.edit().remove("silent").apply()
        val de = isDe(ctx)
        post(ctx, if (de) "✔️ Oh My Marx %s installiert".format(BuildConfig.VERSION_NAME) else "✔️ Oh My Marx %s installed".format(BuildConfig.VERSION_NAME),
            if (de) "Von selbst aktualisiert — deine Daten sind noch da" else "Updated by itself — your data is still there",
            Intent(ctx, MainActivity::class.java), de, quiet = true)
    }
}
