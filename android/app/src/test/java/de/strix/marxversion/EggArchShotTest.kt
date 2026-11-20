package de.strix.marxversion

import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.data.ContentRepo
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.t
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.ui.archSplit
import de.strix.marxversion.ui.cardBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** 🌈 #246: today's arch title = th FULL greeting (feast) o "Quote o th day" ➡ evy egg × long langs ➡ egg_arch_<lang>.png */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
class EggArchShotTest {
    private val CROP = 600  // arch + egg + greeting tail, quote cut off

    /** headline cut: date dots ≠ sentence end · ! kept · dash/colon dropped · no cut = whole */
    @Test fun split() {
        assertEquals("14. März 1883" to "Karl Marx stirbt in London. Ein Vers als Grablicht.",
            archSplit("14. März 1883: Karl Marx stirbt in London. Ein Vers als Grablicht."))
        assertEquals("1. Mai" to "Tag der Arbeit.", archSplit("1. Mai — Tag der Arbeit."))
        assertEquals("Frohe Ostern!" to "Auferstehung.", archSplit("Frohe Ostern! Auferstehung."))
        assertEquals("新年快乐！" to "新年决心", archSplit("新年快乐！新年决心"))
        assertEquals("Quote of the day" to null, archSplit("Quote of the day"))
    }

    /** evy feast × evy lang: headline fits th widest arch at ≥ 24 px (no "…" fallback needed) */
    @Test fun everyHeadFits() {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = ContentRepo(ctx)
        val p = android.graphics.Paint().apply {
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            letterSpacing = 0.14f; textSize = 24f
        }
        val max = (Math.PI * 600 * 100 / 180).toFloat()
        val bad = Lang.entries.flatMap { l -> val tr = repo.tr(l)
            Feast.entries.map { archSplit(tr.t(l, it.greet.first, it.greet.second)).first.uppercase() }.filter { p.measureText(it) > max }
        }
        assertTrue("2 long 4 th arch: $bad", bad.isEmpty())
    }

    @Test fun archAllEggs() {
        assumeTrue(System.getProperty("shots") == "1")
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        val repo = ContentRepo(ctx)
        val dir = File(System.getProperty("shotsDir") ?: "build/shots").apply { mkdirs() }
        for (lang in listOf(Lang.EN, Lang.DE, Lang.RU, Lang.AR)) {
            val tr = repo.tr(lang)
            val titles = listOf<Pair<Feast?, String>>(null to tr.t(lang, "Zitat des Tages", "Quote of the day")) +
                Feast.entries.map { it to tr.t(lang, it.greet.first, it.greet.second) }
            val cols = 4; val s = 3  // ⅓ scale, top strip only (arch + egg)
            val cw = 1080 / s; val ch = CROP / s
            val out = Bitmap.createBitmap(cols * cw, ((titles.size + cols - 1) / cols) * ch, Bitmap.Config.ARGB_8888)
            val c = android.graphics.Canvas(out)
            titles.forEachIndexed { i, (f, t) ->
                val b = cardBitmap(ctx, "Quote", "1,1", lang, f, title = t)
                val top = Bitmap.createBitmap(b, 0, 0, 1080, CROP)
                c.drawBitmap(Bitmap.createScaledBitmap(top, cw, ch, true), ((i % cols) * cw).toFloat(), ((i / cols) * ch).toFloat(), null)
            }
            File(dir, "egg_arch_${lang.name.lowercase()}.png").outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }
}
