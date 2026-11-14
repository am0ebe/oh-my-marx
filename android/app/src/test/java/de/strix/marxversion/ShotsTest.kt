package de.strix.marxversion

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
        tap("Read", true); shot("read")
        tap("p. 49"); shot("chapter")
        tap("Capital 1,"); shot("verse_menu")
        tap("Note"); shot("note_edit")
        rule.activity.onBackPressedDispatcher.let { d -> rule.runOnUiThread { d.onBackPressed() } }
        tap("Circle", true); shot("kreis")
        tap("My Capital", true); shot("mine")
        scroll(); shot("mine_2")
        rule.onAllNodesWithContentDescription("Settings").onFirst().performClick(); shot("settings")
        scroll(); shot("settings_2")
        scroll(); shot("settings_3")
        tap("Today", true); tap("In book", true); shot("in_book_flash")   // ✨ passage pulses (QA r4)
        // L3 @1114: plan detail (back fr th reader 1st ➡ bottom nav, whatever th reader chrome looks like) · kreis_detail needs a server ➡ ❌ shot
        rule.activity.onBackPressedDispatcher.let { d -> rule.runOnUiThread { d.onBackPressed() } }
        tap("Plans", true); tap("Luddites & AI"); shot("plan_detail")
    }

    /** big round #4: day done ➡ 📜 Weiterlesen feed at th tail o Home (scroll ➡ more loads) */
    //   named z… = runs LAST: Room's marx.db survives between tests i 1 JVM ➡ a done day would leak into `screens`
    @Test fun zFeed() {
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Today").fetchSemanticsNodes().isNotEmpty() }
        scroll(); scroll()
        tap("Day complete!")
        rule.mainClock.advanceTimeBy(3000)
        rule.onAllNodesWithContentDescription("Close").onFirst().performClick()   // ★ celebrate dialog (own window: back press doesn't reach it) · ★ = drawn since @1114, no glyph
        rule.mainClock.advanceTimeBy(3000)     // 1st reading day ➡ "new badge" dialog aft 2.6 s
        if (rule.onAllNodesWithText("Keep going!").fetchSemanticsNodes().isNotEmpty()) tap("Keep going!")
        repeat(4) { scroll() }
        shot("feed")
        repeat(4) { scroll() }
        shot("feed_2")
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
