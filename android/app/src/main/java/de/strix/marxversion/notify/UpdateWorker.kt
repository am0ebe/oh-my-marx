package de.strix.marxversion.notify

import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import appkit.selfupdate.SelfUpdateWorker
import appkit.selfupdate.UpdateConfig
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.MainActivity
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.data.Lang
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

// 🆕 self-update = appkit :selfupdate (../appkit/README.md · D-079) · this file = omm's config only
//   rules (D-021 · D-023 · D-038): Obtainium ➡ notif at once, tap = obtainium://refresh · browser/adb ➡ notif ➡ UpdateDialog
//   (dflt: ask, 🦠 @1114) · ⚙️ auto ON + we r installer (12+) ➡ silent · store ➡ nothing

/** class name = WorkManager's stored job (since D-021) ➡ keep it, even though th work lives i th kit */
class UpdateWorker(ctx: Context, params: WorkerParameters) : SelfUpdateWorker(ctx, params)

/** omm's updater config · storage names = th pre-kit ones (omm_update · marx_update · omm.apk) ➡ no state lost */
fun ommUpdate() = UpdateConfig(
    web = "https://scagent.de/oh-my-marx/",
    versionCode = BuildConfig.VERSION_CODE,
    versionName = BuildConfig.VERSION_NAME,
    dev = BuildConfig.APPLICATION_ID.endsWith(".dev"),
    appName = "Oh My Marx",
    icon = R.drawable.ic_update,
    worker = UpdateWorker::class.java,
    openUpdate = { ctx -> Intent(ctx, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.EXTRA_POP, "update") },
    openApp = { ctx -> Intent(ctx, MainActivity::class.java) },
    de = { ctx -> setting(ctx) { it.lang == Lang.DE } },
    auto = { ctx -> setting(ctx) { it.autoUpdate } },   // ⚙️ "auto-update" (browser installs only) · dflt off = ask (🦠 @1114)
    onReplaced = ::launcherReset,
    prefs = "omm_update",
    work = "marx_update",
    sessionName = "omm.apk",
)

/** worker + receivers: DataStore read, ≤ 0.5 s, false on timeout */
private fun setting(ctx: Context, f: (de.strix.marxversion.data.Settings) -> Boolean): Boolean = runCatching {
    runBlocking { withTimeoutOrNull(500) { f((ctx.applicationContext as MarxApp).prefs.settings.first()) } }
}.getOrNull() ?: false

/**
 * 🥚 D-066 amended (🦠 #241: no launcher egg): DEV 0.13.0-dev.1120.1911 could leave `.MainActivity` DISABLED (it was an alias
 * then, an egg alias was on) ➡ that state survives th update ➡ no launcher icon · reset 2 manifest dflt + drop th egg worker
 * runs on evy self-update (kit UpdatedReceiver ➡ UpdateConfig.onReplaced) · ✂️ rm once no DEV 1911 install is left (aftr th nxt stable)
 */
internal fun launcherReset(ctx: Context) {
    runCatching {
        ctx.packageManager.setComponentEnabledSetting(android.content.ComponentName(ctx, MainActivity::class.java),
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, android.content.pm.PackageManager.DONT_KILL_APP)
        WorkManager.getInstance(ctx).cancelUniqueWork("egg-icon")
    }
}
