package de.strix.marxversion

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.ui.MarxTheme
import de.strix.marxversion.ui.ds.MarxHead
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDate

/** 🎄 C3 · D-051/052: 30 feasts · evy date i th table · longest gap · feast_sheet.png (all heads + gradients) */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class FeastSheetTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    private val fixed = mapOf(
        "01-01" to Feast.NEUJAHR, "01-15" to Feast.ROSA_KARL, "02-14" to Feast.VALENTIN, "02-21" to Feast.MANIFEST,
        "03-08" to Feast.FRAUENTAG, "03-14" to Feast.TODESTAG, "03-18" to Feast.KOMMUNE, "04-28" to Feast.WORKERS_MEMORIAL,
        "05-01" to Feast.ERSTER_MAI, "05-05" to Feast.GEBURTSTAG, "05-08" to Feast.BEFREIUNG, "06-01" to Feast.KINDERTAG,
        "06-04" to Feast.WEBER, "06-21" to Feast.LAENGSTER_TAG, "06-28" to Feast.CSD, "07-05" to Feast.MATCHGIRLS,
        "07-14" to Feast.BASTILLE, "08-05" to Feast.ENGELS_TOD, "08-16" to Feast.PETERLOO, "09-01" to Feast.ANTIKRIEG,
        "09-14" to Feast.KAPITAL, "09-28" to Feast.INTERNATIONALE, "10-17" to Feast.ARMUT, "10-31" to Feast.HALLOWEEN,
        "11-28" to Feast.ENGELS_GEB, "12-02" to Feast.JENNY, "12-24" to Feast.WEIHNACHTEN, "12-31" to Feast.SILVESTER,
    )

    @Test fun evyDateInTheTable() {
        // 2027: Easter 28.3 · Black Friday 26.11 ➡ no clash w a fixed day
        fixed.forEach { (md, f) -> assertEquals(md, f, Feast.on(LocalDate.parse("2027-$md"))) }
        assertEquals(Feast.OSTERN, Feast.on(Feast.easter(2027)))
        assertEquals(Feast.BLACK_FRIDAY, Feast.on(Feast.blackFriday(2027)))
        assertEquals(30, Feast.entries.size)
    }

    @Test fun longestGapAbout4Weeks() {
        val days = (0 until 365).map { LocalDate.of(2027, 1, 1).plusDays(it.toLong()) }.filter { Feast.on(it) != null }
        val gap = days.zipWithNext { a, b -> b.toEpochDay() - a.toEpochDay() }.max()
        assertTrue("longest gap $gap d (was ~130 b4 C3)", gap <= 31)
    }

    @Test fun quietDaysAreTheDeathDays() {
        assertEquals(setOf(Feast.TODESTAG, Feast.ROSA_KARL, Feast.WORKERS_MEMORIAL, Feast.BEFREIUNG, Feast.ENGELS_TOD,
            Feast.PETERLOO, Feast.JENNY), Feast.entries.filter { it.quiet }.toSet())
    }

    /** 🥚 D-066: quote-o-th-day card on a feast = th egg instead o th saint (♀ · 🎃 · 💀) ➡ egg_cards.png */
    @Test fun eggCards() {
        assumeTrue(System.getProperty("shots") == "1")
        val ctx = rule.activity
        val cards = listOf(Feast.FRAUENTAG, Feast.HALLOWEEN, Feast.TODESTAG).map { f ->
            de.strix.marxversion.ui.cardBitmap(ctx, "The wealth of those societies in which the capitalist mode of production prevails…",
                "Capital 1,1", Lang.EN, f, title = f.greet.second.substringBefore(" —").take(30))
        }
        val out = Bitmap.createBitmap(cards.sumOf { it.width } / 2, cards[0].height / 2, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(out)
        cards.forEachIndexed { i, b -> c.drawBitmap(Bitmap.createScaledBitmap(b, b.width / 2, b.height / 2, true), (i * b.width / 2).toFloat(), 0f, null) }
        val dir = File(System.getProperty("shotsDir") ?: "build/shots").apply { mkdirs() }
        File(dir, "egg_cards.png").outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun sheet() {
        assumeTrue(System.getProperty("shots") == "1")
        rule.setContent { MarxTheme(Lang.EN, null) {
            Column(Modifier.fillMaxSize().background(Color(0xFFF6EEDC)).padding(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Feast.entries.chunked(5).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { f -> Column(Modifier.width(76.dp).background(Brush.verticalGradient(f.colors.take(3).map { Color(it) }))
                        .padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        MarxHead(f, size = 64.dp)
                        Text(f.emoji + " " + f.name.lowercase().take(10), fontSize = 9.sp, color = Color.White)
                    } }
                } }
            }
        } }
        rule.waitForIdle()
        val v = rule.activity.window.decorView
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { v.draw(android.graphics.Canvas(b)) }
        val dir = File(System.getProperty("shotsDir") ?: "build/shots").apply { mkdirs() }
        File(dir, "feast_sheet.png").outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
