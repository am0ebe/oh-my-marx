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
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f
import de.strix.marxversion.data.t
import de.strix.marxversion.logic.today
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.util.concurrent.TimeUnit

// 🔔 Lesekreis notifications wo a push server: WorkManager polls th circles ~hourly (Android batches it, battery-friendly).
//   1) new chat msgs fr others since last seen ➡ "👥 Kapital-Küche · Rosa: …"
//   2) evening nudge (≥ 19:00, max 1/day): others read today, I didn't ➡ "Rosa + Max haben heute gelesen — du noch nicht ☭"
//      Hooked: social trigger (th tribe, not th app, reminds u) · only if u r i a circle = u asked 4 it

private const val CHANNEL = "kreis"
private const val WORK = "marx_kreis"
private const val NOTIF_MSG = 20
private const val NOTIF_NUDGE = 21

private val NEW_MSG = T("👥 %s · %s: %s", "👥 %s · %s: %s")
private val MORE = T("(+%d weitere)", "(+%d more)")
private val NUDGE_T = T("👥 Dein Lesekreis hat heute gelesen", "👥 Your reading circle read today")
private val NUDGE_X = T("%s — du noch nicht. Ein Vers reicht ☭", "%s — not you yet. One verse is enough ☭")

class KreisWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MarxApp
        val s = app.prefs.mxSession() ?: return Result.success()
        val kreise = app.prefs.kreise.first().ifEmpty { return Result.success() }
        val lang = app.prefs.settings.first().lang
        app.content.load()
        val table = app.content.tr(lang)
        val day = today()
        val readOthers = mutableSetOf<String>()
        for (k in kreise) {
            val tl = Matrix.timeline(s, k.room)
            // 1) chat
            val seen = app.prefs.kreisSeen(k.room)
            val newest = tl.maxOfOrNull { it.ts } ?: 0
            if (seen == null) { app.prefs.setKreisSeen(k.room, newest); continue }  // 1st run: no backlog flood
            val fresh = tl.filter { it.type == "m.room.message" && it.sender != s.userId && it.ts > seen }
            fresh.lastOrNull()?.let { m ->
                val more = if (fresh.size > 1) " " + table.t(lang, MORE.first, MORE.second).f(fresh.size - 1) else ""
                notify(applicationContext, NOTIF_MSG + k.room.hashCode() % 1000,
                    table.t(lang, NEW_MSG.first, NEW_MSG.second).f(k.name, m.name, m.text) + more, m.text)
            }
            if (newest > seen) app.prefs.setKreisSeen(k.room, newest)
            // 2) who read today
            tl.filter { it.type == Matrix.AMEN && it.day == day && it.sender != s.userId }.forEach { readOthers += it.name }
        }
        val iRead = app.db.logs().days().first().any { it.date == day }
        if (!iRead && readOthers.isNotEmpty() && LocalTime.now().hour >= 19 && app.prefs.nudgeDay() != day) {
            app.prefs.setNudgeDay(day)
            notify(applicationContext, NOTIF_NUDGE, table.t(lang, NUDGE_T.first, NUDGE_T.second),
                table.t(lang, NUDGE_X.first, NUDGE_X.second).f(readOthers.take(3).joinToString(" + ")))
        }
        return Result.success()
    }
}

private fun notify(ctx: Context, id: Int, title: String, text: String) {
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val ch = NotificationChannel(CHANNEL, "Lesekreise", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Neue Nachrichten + wer heute gelesen hat" }
    ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    val open = PendingIntent.getActivity(ctx, 3, Intent(ctx, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.EXTRA_POP, "kreis"),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val n = NotificationCompat.Builder(ctx, CHANNEL).setSmallIcon(R.drawable.ic_launcher_fg)
        .setContentTitle(title).setContentText(text).setStyle(NotificationCompat.BigTextStyle().bigText(text))
        .setContentIntent(open).setAutoCancel(true).build()
    NotificationManagerCompat.from(ctx).notify(id, n)
}

/** ~hourly, only w network · KEEP = don't reset th timer on evy app start */
fun scheduleKreis(ctx: Context) {
    val req = PeriodicWorkRequestBuilder<KreisWorker>(1, TimeUnit.HOURS)
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
    WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.KEEP, req)
}
