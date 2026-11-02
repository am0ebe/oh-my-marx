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
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.strix.marxversion.MainActivity
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.T
import de.strix.marxversion.data.Tr
import de.strix.marxversion.data.Verse
import de.strix.marxversion.data.f
import de.strix.marxversion.data.t
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.dailyPool
import de.strix.marxversion.logic.verseOfDay
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

// Hooked: external *owned* trigger. WorkManager runs it ~daily even aft reboot / app killed.
// (not exact 2 th minute — Android batches background work 2 save battery; fine 4 a daily nudge)

private const val CHANNEL = "losung"
private const val WORK = "marx_losung"

class LosungWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MarxApp
        app.content.load()
        val lang = app.prefs.settings.first().lang
        val v = todaysVerse(app) ?: return Result.success()
        val table = app.content.tr(lang)
        val quote = quoteFor(v, lang, table)
        // teaser only — th rest = reward 4 opening th app (zh/ar: no spaces ➡ cut by chars)
        val teaser = if (lang == Lang.ZH) quote.take(14) + "…" else quote.split(" ").take(6).joinToString(" ") + " …"
        val feast = Feast.on(LocalDate.now())?.let { it.emoji + " " } ?: ""
        show(applicationContext, feast + table.t(lang, TITLE.first, TITLE.second).f(v.id), lang.quote(teaser))
        refreshWidgets(applicationContext)  // new day ➡ new verse on th widget too
        return Result.success()
    }
}

private val TITLE = T("Deine Marx-Losung ☭ Kapital %s", "Your Marx verse ☭ Capital %s")

/** th Losung outside th UI (notif + widget): 🎉 feast verse, else th date-seeded ⭐ verse */
fun todaysVerse(app: MarxApp): Verse? =
    Feast.on(LocalDate.now())?.let { app.content.verse(it.verse) } ?: verseOfDay(dailyPool(app.content.verses))

fun quoteFor(v: Verse, lang: Lang, table: Tr?): String = when (lang) {
    Lang.DE -> v.quoteDe
    Lang.EN -> v.quoteEn.ifBlank { v.quoteDe }
    else -> table?.q?.get(v.id) ?: v.quoteEn.ifBlank { v.quoteDe }
}

fun ensureChannel(ctx: Context) {
    val ch = NotificationChannel(CHANNEL, "Marx-Losung", NotificationManager.IMPORTANCE_DEFAULT)
    ch.description = "Täglicher Vers aus dem Kapital"
    ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
}

fun show(ctx: Context, title: String, text: String) {
    if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val open = PendingIntent.getActivity(
        ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE,
    )
    val n = NotificationCompat.Builder(ctx, CHANNEL)
        .setSmallIcon(R.drawable.ic_launcher_fg)
        .setContentTitle(title)
        .setContentText(text)
        .setContentIntent(open)
        .setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(ctx).notify(1, n)
}

/** (re)plan th daily work so it first fires at hh:mm, then evy 24h */
fun schedule(ctx: Context, on: Boolean, hour: Int, minute: Int) {
    val wm = WorkManager.getInstance(ctx)
    if (!on) { wm.cancelUniqueWork(WORK); return }
    val now = LocalDateTime.now()
    var next = now.withHour(hour).withMinute(minute).withSecond(0)
    if (next.isBefore(now)) next = next.plusDays(1)
    val req = PeriodicWorkRequestBuilder<LosungWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(Duration.between(now, next).toMinutes(), TimeUnit.MINUTES)
        .build()
    wm.enqueueUniquePeriodicWork(WORK, ExistingPeriodicWorkPolicy.UPDATE, req)
}
