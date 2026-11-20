package de.strix.marxversion.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import de.strix.marxversion.MarxApp
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.ui.ds.EggRes
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 🥚 D-066: on a feast day th launcher icon IS th egg (activity-alias per feast, manifest block by bin/eggs.py)
 * Android has no "set icon" API ➡ 1 alias per icon, all pointing at HomeActivity · exactly 1 enabled at a time
 *   `.MainActivity` = dflt alias (= th old launcher component ➡ home-screen icons fr b4 0.13 keep working)
 * ⚠️ limits (D-066): icons must ship i th APK (no download) · launchers take 1-10 s 2 redraw · some launchers (Samsung, old
 *   MIUI) drop th home-screen icon when its alias gets disabled ➡ ⚙️ off-switch · Android may kill th task o a disabled
 *   alias ➡ we only switch while th app is NOT on screen (app went 2 bg · midnight worker), nvr under th usr's finger
 */
object EggIcon {
    const val DEFAULT = "de.strix.marxversion.MainActivity"

    /** 🧪 Werkbank "🎉 Holiday" chip (AppVm.feastPreview) ➡ launcher follows on th nxt sync (= leave th app) · process-only */
    @Volatile var preview: Feast? = null

    fun wanted(on: Boolean, day: LocalDate = LocalDate.now()): String =
        if (!on) DEFAULT else (preview ?: Feast.on(day))?.let { EggRes.alias(it) } ?: DEFAULT

    /** enable th wanted alias, disable th rest · no-op when alr right (no launcher churn) · returns th enabled class */
    fun sync(ctx: Context, on: Boolean = enabled(ctx), day: LocalDate = LocalDate.now()): String {
        val pm = ctx.packageManager
        val want = wanted(on, day)
        val all = listOf(DEFAULT) + Feast.entries.map { EggRes.alias(it) }
        fun state(c: String) = runCatching { pm.getComponentEnabledSetting(ComponentName(ctx.packageName, c)) }.getOrNull()
        fun isOn(c: String) = state(c).let { s ->
            s == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || (c == DEFAULT && s == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
        }
        if (isOn(want) && all.none { it != want && isOn(it) }) return want
        // enable 1st, then disable ➡ th app nvr has 0 launcher entries (not even 4 a moment)
        set(pm, ctx, want, true)
        all.filter { it != want }.forEach { if (isOn(it)) set(pm, ctx, it, false) }
        return want
    }

    private fun set(pm: PackageManager, ctx: Context, c: String, on: Boolean) = runCatching {
        // dflt alias: DEFAULT = manifest state (enabled) · eggs: DEFAULT = manifest state (disabled)
        val s = when {
            on && c == DEFAULT -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
            on -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            c == DEFAULT -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            else -> PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        }
        pm.setComponentEnabledSetting(ComponentName(ctx.packageName, c), s, PackageManager.DONT_KILL_APP)
    }

    /** ⚙️ "Feast icon" (dflt on) · worker + lifecycle callers: DataStore read ≤ 0.5 s */
    fun enabled(ctx: Context): Boolean = runCatching {
        runBlocking { withTimeoutOrNull(500) { (ctx.applicationContext as MarxApp).prefs.settings.first().eggIcon } }
    }.getOrNull() ?: true

    /** 🌙 next run = 00:01 local ➡ th icon flips overnight, also on days th app isn't opened */
    fun schedule(ctx: Context, now: LocalDateTime = LocalDateTime.now()) {
        val next = now.toLocalDate().plusDays(1).atTime(0, 1)
        val req = OneTimeWorkRequestBuilder<EggWorker>().setInitialDelay(Duration.between(now, next).toMillis(), TimeUnit.MILLISECONDS).build()
        WorkManager.getInstance(ctx).enqueueUniqueWork(WORK, ExistingWorkPolicy.REPLACE, req)
    }

    private const val WORK = "egg-icon"
}

class EggWorker(ctx: Context, p: WorkerParameters) : CoroutineWorker(ctx, p) {
    override suspend fun doWork(): Result {
        EggIcon.sync(applicationContext)
        EggIcon.schedule(applicationContext)
        return Result.success()
    }
}
