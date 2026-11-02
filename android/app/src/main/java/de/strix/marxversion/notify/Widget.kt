package de.strix.marxversion.notify

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.strix.marxversion.MainActivity
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f
import de.strix.marxversion.data.t
import de.strix.marxversion.logic.DayTime
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.streak
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * 📱 Homescreen-Widget: Losung + 🔥 streak, background = time o day (à la Insight Timer).
 * Hooked ch2: an *owned* external trigger that needs no push — it just sits where u look 80× a day.
 * Classic RemoteViews (no Glance lib) · Android refreshes it hourly (widget_info.xml) ➡ colours follow th day;
 * app + daily worker also push fresh content (refreshWidgets).
 */
class LosungWidget : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        val done = goAsync()  // loading th JSON takes a moment ➡ off th main thread, keep th receiver alive
        CoroutineScope(Dispatchers.IO).launch {
            try { refreshWidgets(ctx) } finally { done.finish() }
        }
    }
}

private val HEAD = T("Marx-Losung", "Marx verse")
private val BOOK = T("Kapital %s", "Capital %s")

private fun DayTime.bg() = when (this) {
    DayTime.MORNING -> R.drawable.wg_morning
    DayTime.DAY -> R.drawable.wg_day
    DayTime.EVENING -> R.drawable.wg_evening
    DayTime.NIGHT -> R.drawable.wg_night
}

suspend fun refreshWidgets(ctx: Context) {
    val mgr = AppWidgetManager.getInstance(ctx)
    val ids = mgr.getAppWidgetIds(ComponentName(ctx, LosungWidget::class.java))
    if (ids.isEmpty()) return
    val app = ctx.applicationContext as MarxApp
    app.content.load()
    val lang = app.prefs.settings.first().lang
    val table = app.content.tr(lang)
    val v = todaysVerse(app) ?: return
    val st = streak(app.db.logs().days().first())
    val feast = Feast.on(LocalDate.now())
    val open = PendingIntent.getActivity(ctx, 1, Intent(ctx, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE)
    val rv = RemoteViews(ctx.packageName, R.layout.widget_losung).apply {
        setInt(R.id.root, "setBackgroundResource", DayTime.at(LocalTime.now().hour).bg())
        setTextViewText(R.id.head, "${feast?.emoji ?: "☭"} ${table.t(lang, HEAD.first, HEAD.second)}" + if (st > 0) "  ·  🔥 $st" else "")
        setTextViewText(R.id.quote, lang.quote(quoteFor(v, lang, table)))
        setTextViewText(R.id.ref, "— Karl Marx, " + table.t(lang, BOOK.first, BOOK.second).f(v.id))
        setOnClickPendingIntent(R.id.root, open)
    }
    mgr.updateAppWidget(ids, rv)
}
