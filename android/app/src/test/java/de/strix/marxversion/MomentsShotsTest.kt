package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.logic.Cell
import de.strix.marxversion.logic.stampCard
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.MilestoneBar
import de.strix.marxversion.ui.MilestoneScreen
import de.strix.marxversion.ui.PromotionScreen
import de.strix.marxversion.ui.StempeluhrPane
import de.strix.marxversion.ui.ds.StreakStar
import de.strix.marxversion.ui.ds.StrikeSlots
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * 🏆 C2 moments (design § 14.1): Stempeluhr · Meilenstein · Beförderung · hero tiers
 * shots: ./gradlew testDebugUnitTest --tests '*MomentsShotsTest*' -Pshots ➡ build/shots/moment_*.png + hero_tiers.png
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class MomentsShotsTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val dir = File(System.getProperty("shotsDir") ?: "build/shots")
    private val shots get() = System.getProperty("shots") == "1"
    private val chain = List(14) { if (it == 5) Cell.REST else if (it == 9) Cell.STRIKE else Cell.DONE }
    private fun has(t: String) = rule.onAllNodesWithText(t, substring = true).fetchSemanticsNodes().isNotEmpty()

    private fun dialogShot(name: String) {
        rule.mainClock.advanceTimeBy(3000); rule.waitForIdle()
        if (!shots) return
        // evy shown window (moment + Confetti overlay = own Dialog) in order, centred
        val ds = org.robolectric.shadows.ShadowDialog.getShownDialogs().mapNotNull { it.window?.decorView }
        if (ds.isEmpty()) return
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread {
            val c = android.graphics.Canvas(b)
            c.drawColor(0xFF1F4E9A.toInt()); c.drawColor(0x80000000.toInt())
            ds.forEach { dv -> c.save(); c.translate((v.width - dv.width) / 2f, (v.height - dv.height) / 2f); dv.draw(c); c.restore() }
        }
        dir.mkdirs(); File(dir, "$name.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun shift() {
        rule.setContent { MarxTheme(Lang.EN, null) { StempeluhrPane(stampCard(5), 1, 12, 2,
            "Quota met — no overseer.", "Why does money breed young?", more = false, hooked = false) {} } }
        dialogShot("moment_shift")
        assertTrue("Clock out", has("Clock out"))
        assertTrue(has("Tomorrow: Why does money"))
    }

    /** ≥ 2 moments due ➡ "Wait … ▸" (open loop, D-049) */
    @Test fun shiftFull_more() {
        rule.setContent { MarxTheme(Lang.EN, null) { StempeluhrPane(stampCard(18), 2, 30, 3,
            "Stamped. Today the clock is yours.", null, more = true, hooked = true) {} } }
        dialogShot("moment_shift_full")
        assertTrue(has("Wait"))
    }

    @Test fun milestone14() {
        rule.setContent { MarxTheme(Lang.EN, null) { MilestoneScreen(14, 2, best = true, chain = chain, strikeEarned = true, hooked = false) {} } }
        dialogShot("moment_milestone_14")
        assertTrue(has("14 days unbroken")); assertTrue(has("+1 strike day"))
    }

    @Test fun rank3() {
        rule.setContent { MarxTheme(Lang.EN, null) { PromotionScreen(2, 28, milestone = 30, hooked = false) {} } }
        dialogShot("moment_rank_3")
        assertTrue(has("LEVEL 3")); assertTrue(!has("GENEHMIGT") && !has("APPROVED")); assertTrue(has("Expanded reproduction")); assertTrue(has("30 days in a row"))
        assertTrue("rank-3 perk line (built @1120 #206)", has("Marx Basics"))
    }

    @Test fun heroTiers() {
        val ns = listOf(3, 7, 14, 30, 50, 100, 200, 365)
        rule.setContent { MarxTheme(Lang.EN, null) {
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1F4E9A), Color(0xFF3A8BD0)))).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ns.chunked(4).forEachIndexed { r, row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEachIndexed { i, n -> Column { StreakStar(n, r * 4 + i); Text("tier ${r * 4 + i}", color = Color.White); StrikeSlots(i % 3) } }
                } }
                MilestoneBar(10, 14)
            }
        } }
        rule.mainClock.advanceTimeBy(500); rule.waitForIdle()
        assertTrue(has("tier 7"))
        assumeTrue(shots)
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        dir.mkdirs(); File(dir, "hero_tiers.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
