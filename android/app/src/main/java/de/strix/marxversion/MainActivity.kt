package de.strix.marxversion

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.T
import de.strix.marxversion.logic.Orden
import de.strix.marxversion.notify.schedule
import de.strix.marxversion.ui.APP_NAME
import de.strix.marxversion.ui.AppVm
import de.strix.marxversion.ui.CelebrationHost
import de.strix.marxversion.ui.CelebrationQueue
import de.strix.marxversion.ui.ChapterScreen
import de.strix.marxversion.ui.DevBlue
import de.strix.marxversion.ui.HookedScreen
import de.strix.marxversion.ui.HookedUnlockDialog
import de.strix.marxversion.ui.IS_DEV
import de.strix.marxversion.ui.KreisDetail
import de.strix.marxversion.ui.KreiseScreen
import de.strix.marxversion.ui.LocalCelebrations
import de.strix.marxversion.ui.LocalOpenHooked
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.MineScreen
import de.strix.marxversion.ui.NewOrdenDialog
import de.strix.marxversion.ui.PlanDetail
import de.strix.marxversion.ui.PlansScreen
import de.strix.marxversion.ui.ReadScreen
import de.strix.marxversion.ui.RewardSlot
import de.strix.marxversion.ui.SettingsScreen
import de.strix.marxversion.ui.TodayScreen
import de.strix.marxversion.ui.UpdateDialog
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.tapOutsideNote
import de.strix.marxversion.ui.tr

/** th only Activity · evythg else = composables swapped by th NavHost */
class MainActivity : ComponentActivity() {
    private val vm: AppVm by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val s by vm.settings.collectAsStateWithLifecycle()
            val loaded by vm.loaded.collectAsStateWithLifecycle()
            val full by vm.full.collectAsStateWithLifecycle()
            MarxTheme(s.lang, if (loaded) vm.content.tr(s.lang) else null, full?.takeIf { it.first == s.lang }?.second) {
                if (!loaded) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                else App(vm)
            }
        }
        if (savedInstanceState == null) openLosung(intent)  // not again on rotate (same intent comes back)
    }

    /** app alr running (SINGLE_TOP) ➡ notif / widget tap lands here */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openLosung(intent)
    }

    private fun openLosung(i: Intent?) {
        if (i?.getBooleanExtra(EXTRA_LOSUNG, false) == true) { vm.forcePopup.value = true; i.removeExtra(EXTRA_LOSUNG) }
        // 🚀 launcher shortcuts (res/xml/shortcuts.xml) — string extras
        when (val p = i?.getStringExtra(EXTRA_POP)) {
            "losung" -> vm.forcePopup.value = true
            "oracle", "krise" -> vm.popRequest.value = p
            "kreis" -> vm.openKreise.value++          // 👥 Kreis notification tap
            "update" -> vm.openUpdate()               // 🆕 update notification tap (notify/UpdateWorker.kt)
        }
        i?.removeExtra(EXTRA_POP)
        // 👥 invite link: ohmymarx://kreis/K7M2Q9 ➡ Kreis tab w th code ready
        i?.data?.takeIf { it.scheme == "ohmymarx" && it.host == "kreis" }?.lastPathSegment?.let { code ->
            vm.pendingKreis.value = code.uppercase().filter { it.isLetterOrDigit() }.take(8)
            vm.openKreise.value++
            i.data = null
        }
    }

    companion object { const val EXTRA_LOSUNG = "losung"; const val EXTRA_POP = "pop" }
}

// QA r5 (sweep #2): monochrome icons (were colour emoji next 2 a grey ☭ = mixed look) · ☭ stays ours, tinted like th rest · null = ☭
private val TABS = listOf(
    Triple("today", R.drawable.ic_nav_today, T("Heute", "Today")),
    Triple("plans", R.drawable.ic_nav_plans, T("Pläne", "Plans")),
    Triple("read", R.drawable.ic_book, T("Lesen", "Read")),
    Triple("kreis", R.drawable.ic_nav_group, T("Kreis", "Circle")),
    Triple("mine", null, T("Mein Kapital", "My Capital")),
)

/**
 * 🧭 app chrome per route (design_system.md § 6 · D-031 b):
 * Root = tab root: red bar + white title · Sub = pushed screen: paper bar, ink title, back arrow · Reader = chapter: no bars,
 * status bar = page colour w dark icons (th slim paper head lives i ChapterScreen, lane L2)
 */
internal enum class Chrome { Root, Sub, Reader }

internal fun chromeFor(route: String?): Chrome = when {
    route == null || TABS.any { it.first == route } -> Chrome.Root
    route.startsWith("chapter/") -> Chrome.Reader
    else -> Chrome.Sub
}

/** reward keys i th celebration queue (ui/Fx.kt) */
private const val R_UNLOCK = "unlock"
private const val R_ORDEN = "orden"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(vm: AppVm) {
    val nav = rememberNavController()
    val s by vm.settings.collectAsStateWithLifecycle()
    val forcePopup by vm.forcePopup.collectAsStateWithLifecycle()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val chrome = chromeFor(route)
    // 🎉 1 celebration at a ti (D-034): fx + reward dialogs queue here, screens play into it via LocalCelebrations
    val fx = remember { CelebrationQueue() }

    // Android 13+ runtime permission 4 notifications
    val notifPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val askNotif = { if (Build.VERSION.SDK_INT >= 33) notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }
    LaunchedEffect(Unit) {
        // 1st start: ask once + plan th daily Losung w th saved time
        if (s.notifOn) { askNotif(); schedule(vm.getApplication(), true, s.notifHour, s.notifMinute) }
    }
    val openChapter = { kap: String, v: String -> nav.navigate("chapter/$kap?v=$v") }
    val openHooked = { nav.open("hooked") }
    // 🏠 Marx icon / title / "Heute" tab ➡ Home + back 2 th 1st card · alr home ➡ only glide 2 card 1 (no re-nav = no flicker)
    val home: () -> Unit = { nav.go("today"); vm.homeJump.value++ }
    // 🚀 shortcut card (Orakel / Krise) lives on Today ➡ go there 1st
    val popReq by vm.popRequest.collectAsStateWithLifecycle()
    LaunchedEffect(popReq) { if (popReq != null && route != "today") nav.go("today") }
    // 👥 invite link / Kreis notification ➡ th Kreis tab
    val openKreise by vm.openKreise.collectAsStateWithLifecycle()
    LaunchedEffect(openKreise) { if (openKreise > 0) nav.go("kreis") }

    StatusBar(chrome)

    val focusMgr = androidx.compose.ui.platform.LocalFocusManager.current
    val slop = androidx.compose.ui.platform.LocalViewConfiguration.current.touchSlop
    CompositionLocalProvider(LocalOpenHooked provides openHooked, LocalCelebrations provides fx) {
    Scaffold(
        // ✍️ tap outside an open margin note = save it (ui/VerseView.kt NoteFocus)
        modifier = Modifier.tapOutsideNote(focusMgr, slop),
        // reader: no top bar ➡ th status bar strip shows th container = page colour
        containerColor = if (chrome == Chrome.Reader) Ds.page else MaterialTheme.colorScheme.background,
        topBar = {
            when (chrome) {
                Chrome.Root -> TopAppBar(
                    // tap Marx (or th name) = home
                    // 🧪 DEV app: Marx on a blue disc (= its launcher icon) ➡ nvr confuse it w th stable one
                    navigationIcon = {
                        Image(painterResource(R.mipmap.ic_marx_fg), contentDescription = tr("Heute", "Today"),
                            modifier = Modifier.padding(start = 4.dp).size(44.dp)
                                .then(if (IS_DEV) Modifier.background(DevBlue, CircleShape) else Modifier)
                                .clickable(onClick = home))
                    },
                    title = { Text(APP_NAME, style = MaterialTheme.typography.titleMedium, modifier = Modifier.clickable(onClick = home)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ds.red, titleContentColor = Ds.white,
                        actionIconContentColor = Ds.white),
                    actions = {
                        if (s.testMode) IconButton(onClick = openHooked) {
                            Icon(painterResource(R.drawable.ic_hook), tr("Hooked-Modus", "Hooked mode"), Modifier.size(24.dp))
                        }
                        LangButton(s.lang) { vm.setLang(it) }
                        IconButton(onClick = { nav.open("settings") }) {
                            Icon(painterResource(R.drawable.ic_settings), tr("Einstellungen", "Settings"))
                        }
                    },
                )
                Chrome.Sub -> TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) { Icon(painterResource(R.drawable.ic_back), tr("Zurück", "Back")) }
                    },
                    title = { Text(subTitle(route), style = MaterialTheme.typography.titleMedium, maxLines = 1) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Ds.paper, titleContentColor = Ds.ink,
                        navigationIconContentColor = Ds.ink),
                )
                Chrome.Reader -> {}
            }
        },
        bottomBar = {
            if (chrome != Chrome.Reader) Column {
                HorizontalDivider(thickness = 1.dp, color = Ds.line)
                NavigationBar(containerColor = Ds.paperDeep, tonalElevation = 0.dp) {
                    TABS.forEach { (r, icon, label) ->
                        NavigationBarItem(
                            selected = route == r,
                            onClick = { if (r == "today") home() else nav.go(r) },
                            icon = {
                                if (icon != null) Icon(painterResource(icon), null, Modifier.size(24.dp))
                                else Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {   // ☭ glyph = same tint as th icons
                                    Text("☭", style = MaterialTheme.typography.titleLarge, modifier = Modifier.wrapContentSize(unbounded = true))
                                }
                            },
                            label = { Text(tr(label), style = MaterialTheme.typography.labelMedium, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Ds.red, selectedTextColor = Ds.red, indicatorColor = Ds.redTint,
                                unselectedIconColor = Ds.inkMuted, unselectedTextColor = Ds.inkMuted,
                            ),
                        )
                    }
                }
            }
        },
    ) { pad ->
        NavHost(nav, startDestination = "today", modifier = Modifier.padding(pad)) {
            composable("today") { TodayScreen(vm, askNotif, openChapter) { nav.go("plans") } }
            composable("plans") { PlansScreen(vm) { nav.navigate("plan/$it") } }
            composable("plan/{id}") { e -> PlanDetail(vm, e.arguments?.getString("id")!!) { nav.go("today") } }
            composable("read") { ReadScreen(vm) { nav.navigate("chapter/$it") } }
            composable("chapter/{kap}?v={v}") { e ->
                // 👉 swipe 2 a neighbour chapter = replace this one (back still goes 2 th list, not thru evy chapter)
                ChapterScreen(vm, e.arguments?.getString("kap")!!, e.arguments?.getString("v")) { k ->
                    nav.navigate("chapter/$k") { popUpTo("chapter/{kap}?v={v}") { inclusive = true } }
                }
            }
            composable("mine") { MineScreen(vm, openChapter) }
            // 👥 Lesekreise (ui/KreisScreen.kt) · {i} = index i th sorted list
            composable("kreis") { KreiseScreen(vm) { i -> nav.navigate("kreis/$i") } }
            composable("kreis/{i}") { e -> KreisDetail(vm, e.arguments?.getString("i")?.toIntOrNull() ?: 0) { nav.popBackStack() } }
            composable("settings") { SettingsScreen(vm, askNotif) }
            composable("hooked") {
                HookedScreen { dest ->
                    if (dest == "popup") { nav.go("today"); vm.forcePopup.value = true }
                    else if (dest == "settings") nav.open("settings")
                    else nav.go(dest)
                }
            }
        }
    }

    // 🃏 th cards r Home now (no more popup on open): notif / widget / tile / 🪝 chip ➡ Home, 1st card
    LaunchedEffect(forcePopup) {
        if (forcePopup) { nav.go("today"); vm.homeJump.value++; vm.popupDone() }
    }

    // 🎖️ new medal(s) ➡ queued behind whatever celebrates now (Amen fx) ➡ no fixed wait · 🔓 Hooked unlock = own moment 1st (D-025)
    val newOrden by vm.newOrden.collectAsStateWithLifecycle()
    val shownOrden = remember { mutableSetOf<Orden>() }   // this session: nvr twice, even b4 th prefs write lands
    LaunchedEffect(newOrden) {
        val fresh = newOrden - shownOrden
        if (Orden.HOOKED in fresh) fx.reward(R_UNLOCK)
        if (fresh.any { it != Orden.HOOKED }) fx.reward(R_ORDEN)
    }
    RewardSlot(R_UNLOCK) { done ->
        HookedUnlockDialog({ vm.setTestMode(true) }) { shownOrden += Orden.HOOKED; vm.ordenSeen(listOf(Orden.HOOKED)); done() }
    }
    RewardSlot(R_ORDEN) { done ->
        val list = remember { newOrden.filter { it != Orden.HOOKED && it !in shownOrden } }
        if (list.isEmpty()) LaunchedEffect(Unit) { done() }
        else NewOrdenDialog(list) { shownOrden += list; vm.ordenSeen(list); done() }
    }
    CelebrationHost(fx)

    // 🆕 download + install (data/Update.kt)
    val showUpdate by vm.showUpdate.collectAsStateWithLifecycle()
    if (showUpdate) UpdateDialog(vm) { vm.showUpdate.value = false }
    }
}

/** pushed screens: short paper-bar title (th screen's own heading stays below) */
@Composable
private fun subTitle(route: String?): String = when {
    route == null -> APP_NAME
    route.startsWith("plan/") -> tr("Leseplan", "Reading plan")
    route.startsWith("kreis/") -> tr("Lesekreis", "Reading circle")
    route == "settings" -> tr("Einstellungen", "Settings")
    route == "hooked" -> "Hooked"
    else -> APP_NAME
}

/** status bar per chrome: red (tab roots, light icons) · paper (sub) · page (reader) w dark icons */
@Composable
private fun StatusBar(chrome: Chrome) {
    val view = LocalView.current
    LaunchedEffect(chrome) {
        val w = view.context.activity()?.window ?: return@LaunchedEffect
        val bar = when (chrome) { Chrome.Root -> Ds.red; Chrome.Sub -> Ds.paper; Chrome.Reader -> Ds.page }
        // API 35+ = edge-to-edge: th bar is transparent, th top bar / Scaffold container paints under it
        if (Build.VERSION.SDK_INT < 35) w.paintStatusBar(bar.toArgb())
        WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = chrome != Chrome.Root
    }
}

@Suppress("DEPRECATION")
private fun android.view.Window.paintStatusBar(argb: Int) { statusBarColor = argb }

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

/** shows only th current language · tap ➡ menu w th oth 6 */
@Composable
private fun LangButton(cur: Lang, pick: (Lang) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(Modifier.minimumInteractiveComponentSize().clip(CircleShape).clickable(role = Role.Button) { open = true }
            .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(cur.code, color = Ds.white, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Icon(painterResource(R.drawable.ic_expand), null, tint = Ds.white, modifier = Modifier.size(20.dp))
        }
        DropdownMenu(open, { open = false }) {
            Lang.menu.forEach { l ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text((if (l == cur) "✓ " else "    ") + l.native)
                            // 0.8.0: whole book i all 7 · IT…AR = our own LLM translation ➡ say so (not a published edition)
                            if (l.ordinal > 1) Text("    " + tr("Ganzes Buch · KI-übersetzt aus dem Deutschen", "whole book · AI-translated from German"),
                                style = MaterialTheme.typography.labelMedium, color = Ds.inkMuted)
                        }
                    },
                    onClick = { open = false; pick(l) },
                )
            }
        }
    }
}

/** alr showing `route`? ➡ 🐛 @1104 re-navigating 2 th same screen re-ran it = flicker (Home, ⚙️, tabs) */
private fun NavHostController.isOn(route: String) = currentDestination?.route == route

/**
 * bottom-tab navigation: always land on th tab's root · no-op if alr there.
 * 🐛 was: saveState/restoreState ➡ tapping "Heute"/"Lesen" restored th old back stack incl. ⚙️ Settings
 */
private fun NavHostController.go(route: String) {
    if (isOn(route)) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}

/** push a screen on top (⚙️ · 🪝) · no-op if alr there */
private fun NavHostController.open(route: String) {
    if (!isOn(route)) navigate(route) { launchSingleTop = true }
}
