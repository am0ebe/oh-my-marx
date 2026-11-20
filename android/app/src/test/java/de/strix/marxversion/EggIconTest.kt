package de.strix.marxversion

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.data.EggIcon
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.ui.ds.EggRes
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

/** 🥚 D-066: launcher alias per feast · exactly 1 launcher entry at a time · ⚙️ off = plain Marx · all 30 eggs inflate */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class EggIconTest {
    private val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
    private val pm = ctx.packageManager
    private val plain = LocalDate.of(2027, 11, 20)
    private val womensDay = LocalDate.of(2027, 3, 8)

    @After fun reset() { EggIcon.preview = null }

    private fun launchers(): Set<String> = (listOf(EggIcon.DEFAULT) + Feast.entries.map { EggRes.alias(it) }).filter {
        val s = pm.getComponentEnabledSetting(ComponentName(ctx.packageName, it))
        s == PackageManager.COMPONENT_ENABLED_STATE_ENABLED || (it == EggIcon.DEFAULT && s == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT)
    }.toSet()

    @Test fun evyAliasIsInTheManifest_andPointsHome() {
        Feast.entries.map { EggRes.alias(it) }.plus(EggIcon.DEFAULT).forEach { c ->
            val info = pm.getActivityInfo(ComponentName(ctx.packageName, c), PackageManager.MATCH_DISABLED_COMPONENTS)
            assertEquals(c, HomeActivity::class.java.name, info.targetActivity)
        }
    }

    @Test fun feastDay_eggOnly_thenBackToPlain() {
        assertEquals(setOf(EggIcon.DEFAULT), launchers())
        assertEquals(EggRes.alias(Feast.FRAUENTAG), EggIcon.sync(ctx, on = true, day = womensDay))
        assertEquals(setOf(EggRes.alias(Feast.FRAUENTAG)), launchers())
        EggIcon.sync(ctx, on = true, day = plain)
        assertEquals(setOf(EggIcon.DEFAULT), launchers())
    }

    @Test fun switchedOff_plainEvenOnAFeast() {
        EggIcon.sync(ctx, on = true, day = womensDay)
        EggIcon.sync(ctx, on = false, day = womensDay)
        assertEquals(setOf(EggIcon.DEFAULT), launchers())
    }

    @Test fun testChipPreview_wins_andFeastToFeast() {
        EggIcon.sync(ctx, on = true, day = womensDay)
        EggIcon.preview = Feast.HALLOWEEN
        EggIcon.sync(ctx, on = true, day = womensDay)
        assertEquals(setOf(EggRes.alias(Feast.HALLOWEEN)), launchers())
    }

    @Test fun all30EggsInflate() {
        Feast.entries.forEach { f ->
            assertNotNull(f.name, ContextCompat.getDrawable(ctx, EggRes.fg(f)))
            assertNotNull(f.name, ContextCompat.getDrawable(ctx, EggRes.bg(f)))
        }
        assertTrue(Feast.entries.map { EggRes.alias(it) }.toSet().size == 30)
    }
}
