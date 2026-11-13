package de.strix.marxversion.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import de.strix.marxversion.MainActivity
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Update
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

// 🆕 "new version" i th background (QA r5 @1113): friends who installed th APK by hand must nvr sit on a stale build.
//   WorkManager ~daily (only w network, Android batches it) ➡ Update.check (own channel: stable o DEV) ➡ system notification
//   ➡ tap = app opens th UpdateDialog (download + Android's install dialog) · store/Obtainium installs: Update.wanted = false
//   nag: new version ➡ at once · same one still not installed ➡ again evy Update.RENAG days

private const val CHANNEL = "update"
private const val WORK = "marx_update"
const val NOTIF_UPDATE = 30

class UpdateWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        checkAndNotify(applicationContext)
        return Result.success()   // offline / garbage = just try again tomorrow (no retry storm)
    }
}

/** 🧪 seam (UpdateWorkerTest): `get` = fake version.json · returns th version it notified about (null = none) */
suspend fun checkAndNotify(ctx: Context, get: ((String) -> String)? = null): Update.Latest? {
    val l = (if (get != null) Update.check(ctx, get) else Update.check(ctx)) ?: return null
    val sp = ctx.getSharedPreferences("omm_update", Context.MODE_PRIVATE)
    val today = LocalDate.now().toEpochDay()
    if (!Update.shouldNotify(l.code, sp.getInt("code", 0), sp.getLong("day", Long.MIN_VALUE / 2), today)) return null
    val de = runCatching { (ctx as MarxApp).prefs.settings.first().lang == Lang.DE }.getOrDefault(false)
    if (!postUpdate(ctx, l, de)) return null
    sp.edit().putInt("code", l.code).putLong("day", today).apply()
    return l
}

private fun postUpdate(ctx: Context, l: Update.Latest, de: Boolean): Boolean {
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return false
    val ch = NotificationChannel(CHANNEL, if (de) "App-Updates" else "App updates", NotificationManager.IMPORTANCE_DEFAULT)
        .apply { description = if (de) "Neue Version von Oh My Marx" else "New version of Oh My Marx" }
    ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    val open = PendingIntent.getActivity(ctx, 4, Intent(ctx, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.EXTRA_POP, "update"),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val title = if (de) "🆕 Oh My Marx %s ist da".format(l.version) else "🆕 Oh My Marx %s is out".format(l.version)
    val text = if (de) "Antippen = laden + installieren · deine Daten bleiben" else "Tap = download + install · your data stays"
    val n = NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(R.drawable.ic_update)
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
