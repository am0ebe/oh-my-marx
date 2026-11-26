package de.strix.marxversion

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.data.Lang
import de.strix.marxversion.ui.cardBitmap
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * 📸 headless screenshots o evy screen (QA r4 @1113: "design keeps slipping thru ➡ compare screens side by side")
 * Robolectric native graphics = th real app, real Compose, on th JVM (no emulator: no KVM here)
 * run: ./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots ➡ app/build/shots/NN_name.png · wo -Pshots = skipped
 * ⚠️ fonts/emoji = Robolectric's (≈ Roboto, emoji may be boxes) · dialogs = own window ➡ not i th root capture
 */
/** Robolectric doesn't run androidx.startup ➡ WorkManager by hand, before MarxApp schedules its workers */
class ShotsApp : MarxApp() {
    /** 🔊 D-085: UI tsts nvr hit scagent.de 4 voice packs (no manifest = old behaviour, no dialog) */
    override val voice by lazy { de.strix.marxversion.data.VoicePacks(java.io.File(filesDir, "voice"),
        fetchText = { throw java.io.IOException("offline (tst)") }, fetchTo = { _, _, _ -> throw java.io.IOException("offline (tst)") }) }
    override fun onCreate() {
        // 2nd test i th same JVM: WorkManager's static instance is still there ➡ "already initialized" = fine
        runCatching { androidx.work.WorkManager.initialize(this, androidx.work.Configuration.Builder().build()) }
        super.onCreate()
    }
}

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = ShotsApp::class)
@org.junit.FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
class ShotsTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")
    private var n = 0

    @Before fun gate() = assumeTrue("needs -Pshots", System.getProperty("shots") == "1")

    private fun save(name: String, bmp: Bitmap) {
        dir.mkdirs()
        File(dir, "%02d_%s.png".format(++n, name)).outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun shot(name: String) {
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1500)
        rule.waitForIdle()
        // captureToImage times out under Robolectric (waits 4 a HW frame) ➡ draw th decor view into a software canvas
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        save(name, b)
    }

    /** dialogs = own window ➡ screen + 50 % scrim + th newest dialog's window centred on top (L5 @1114) */
    private fun dialogShot(name: String) {
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1500)
        rule.waitForIdle()
        val d = org.robolectric.shadows.ShadowDialog.getLatestDialog() ?: return
        val v = rule.activity.window.decorView
        val dv = d.window?.decorView ?: return
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread {
            val c = android.graphics.Canvas(b)
            v.draw(c)
            c.drawColor(0x80000000.toInt())
            c.translate((v.width - dv.width) / 2f, (v.height - dv.height) / 2f)
            dv.draw(c)
        }
        save(name, b)
    }

    /** exact = whole label (tabs: "Read" ≠ "Reading plans") */
    private fun tap(text: String, exact: Boolean = false) {
        rule.waitUntil(10_000) { rule.onAllNodesWithText(text, substring = !exact).fetchSemanticsNodes().isNotEmpty() }
        rule.onAllNodesWithText(text, substring = !exact).onFirst().performClick()
    }

    /** mid-screen swipe (default starts on th nav bar = no scroll) */
    private fun scroll() = rule.onRoot().performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.2f) }

    @Test fun screens() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        shot("today")
        scroll(); shot("today_2")
        scroll(); shot("today_3")
        tap("Plans", true); shot("plans")
        repeat(6) { scroll() }; shot("plans_locked")   // 🔒 D-043 mystery cards at th end
        rule.onRoot().performTouchInput { repeat(6) { swipeDown() } }
        tap("Read", true); shot("read")
        tap("p. 49"); shot("chapter")
        // D-057: i th book a tap turns ➡ th menu = double-tap · ref line "Capital 1,1" gone i th reader (qa_1117)
        rule.onAllNodesWithText("wealth of those societies", substring = true).onFirst().performTouchInput { doubleClick() }
        shot("verse_menu")
        tap("Note"); shot("note_edit")
        // qa_1117 r2: tap away = save only ➡ th verse menu below stays open
        rule.onAllNodes(hasSetTextAction()).onFirst().performTextInput("Ware = Zelle")
        tap("Two Factors"); shot("note_saved")
        rule.activity.onBackPressedDispatcher.let { d -> rule.runOnUiThread { d.onBackPressed() } }
        tap("Circle", true); shot("kreis")
        tap("My Capital", true); shot("mine")
        scroll(); shot("mine_2")
        rule.onAllNodesWithContentDescription("Settings").onFirst().performClick(); shot("settings")
        scroll(); shot("settings_2")
        scroll(); shot("settings_3")
        tap("Today", true); tap("In book", true); shot("in_book_flash")   // ✨ passage pulses (QA r4)
        // 📤 share panel (L5): list rows + Tonal copy · chapter has no nav bar (D-031) ➡ back 2 Today
        rule.activity.onBackPressedDispatcher.let { d -> rule.runOnUiThread { d.onBackPressed() } }
        tap("Share app", true); dialogShot("share_app")
        rule.onAllNodesWithContentDescription("Close").onFirst().performClick()
        // 🆕 update dialog (L5): fake "newer version out" ➡ Primary-L Install + Text Later
        val vm = androidx.lifecycle.ViewModelProvider(rule.activity)[de.strix.marxversion.ui.AppVm::class.java]
        rule.runOnUiThread { vm.update.value = appkit.selfupdate.Update.Latest("9.9.9", 999_999, apk = "omm.apk"); vm.showUpdate.value = true }
        dialogShot("update")
        rule.runOnUiThread { vm.showUpdate.value = false; vm.update.value = null }
        // L3 @1114: plan detail · kreis_detail needs a server ➡ ❌ shot
        tap("Plans", true); tap("Luddites & AI"); shot("plan_detail")
    }

    /** day done ➡ Home ends at Marxoskop · 📜 endless feed = th reader's 📜 mode now (qa_1116 Q4/Q7) ➡ chapter_scroll */
    //   named z… = runs LAST: Room's marx.db survives between tests i 1 JVM ➡ a done day would leak into `screens`
    /** 🥚 D-066: Today on a feast ➡ top-bar head + hero + quote card = th egg (♀ · 🎃) · egg_today_*.png */
    @Test fun zEgg() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        val vm = androidx.lifecycle.ViewModelProvider(rule.activity)[de.strix.marxversion.ui.AppVm::class.java]
        listOf(de.strix.marxversion.logic.Feast.FRAUENTAG, de.strix.marxversion.logic.Feast.HALLOWEEN,
            de.strix.marxversion.logic.Feast.TODESTAG).forEach { f ->
            rule.runOnUiThread { vm.feastPreview.value = f }
            rule.mainClock.advanceTimeBy(2500)
            shot("egg_today_${f.name.lowercase()}")
        }
        rule.runOnUiThread { vm.feastPreview.value = null }
    }

    @Test fun zFeed() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        scroll(); scroll()
        tap("Day complete!")
        rule.mainClock.advanceTimeBy(3000)
        rule.onAllNodesWithContentDescription("Close").onFirst().performClick()   // ★ celebrate dialog (own window: back press doesn't reach it) · ★ = drawn since @1114, no glyph
        rule.mainClock.advanceTimeBy(3000)     // 1st reading day ➡ "new badge" dialog, queued behind th CheckPop (ui/Fx.kt)
        if (rule.onAllNodesWithText("Keep going!").fetchSemanticsNodes().isNotEmpty()) { dialogShot("orden"); tap("Keep going!") }
        repeat(4) { scroll() }
        shot("feed")   // = Home's end (Marxoskop), no feed tail
        // 📜 mode (runs last: th saved read_scroll can't leak into `screens`)
        tap("Read", true); tap("p. 49")
        rule.onAllNodesWithContentDescription("Endless scroll").onFirst().performClick()
        rule.mainClock.advanceTimeBy(1000)
        repeat(3) { scroll() }
        shot("chapter_scroll")
    }

    /** 🪝 Hooked on (DEV unlock) ➡ Today: does th top hook cover th streak hero? (qa_1117 r3) · runs aftr zFeed */
    /** 📜 C4 holy book i rtl (ar: ribbon + gilt edge mirror) + zh (no caps ➡ initial box w 1st glyph) · runs last */
    @Test fun zLangs() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        val vm = androidx.lifecycle.ViewModelProvider(rule.activity)[de.strix.marxversion.ui.AppVm::class.java]
        rule.runOnUiThread { vm.setReadScroll(false) }
        rule.mainClock.advanceTimeBy(1000)
        tap("Read", true); tap("p. 49"); rule.mainClock.advanceTimeBy(1500)
        listOf(Lang.AR to "chapter_ar", Lang.ZH to "chapter_zh").forEach { (l, name) ->
            rule.runOnUiThread { vm.setLang(l) }
            // full_xx.json loads async ➡ wait til th head chip left English
            rule.waitUntil(30_000) { rule.mainClock.advanceTimeBy(500); rule.onAllNodesWithText("Scroll", substring = true).fetchSemanticsNodes().isEmpty() }
            rule.mainClock.advanceTimeBy(2000); rule.waitForIdle()
            shot(name)
        }
        rule.runOnUiThread { vm.setLang(Lang.EN) }
        rule.mainClock.advanceTimeBy(2000)
    }

    @Test fun zHooked() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        val vm = androidx.lifecycle.ViewModelProvider(rule.activity)[de.strix.marxversion.ui.AppVm::class.java]
        rule.runOnUiThread { vm.setTestMode(true) }
        rule.mainClock.advanceTimeBy(2000)
        tap("Today", true); shot("today_hooked")
        rule.runOnUiThread { vm.setTestMode(false) }
    }

    @Test fun cards() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val q = "Accumulate, accumulate! That is Moses and the prophets!"
        save("card_today", cardBitmap(ctx, q, "Capital 22,56 · MEW p. 621", Lang.EN, title = "Quote of the day"))
        save("card_day", cardBitmap(ctx, q, "Capital 22,56 · MEW p. 621", Lang.EN))
        save("card_long", cardBitmap(ctx, q.repeat(6), "Capital 22,56", Lang.DE, title = "Zitat des Tages"))
        save("card_krise", cardBitmap(ctx, q, "Capital 22,56", Lang.EN, krise = true))
    }
}
