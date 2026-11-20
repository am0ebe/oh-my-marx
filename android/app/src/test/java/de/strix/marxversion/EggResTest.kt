package de.strix.marxversion

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.notify.launcherReset
import de.strix.marxversion.ui.ds.EggRes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 🥚 D-066 (amended #241: in-app only, launcher icon stays plain) · all 30 eggs inflate · DEV 1911 leftover reset */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = android.app.Application::class)
class EggResTest {
    private val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()

    @Test fun all30EggsInflate() {
        Feast.entries.forEach { f ->
            assertNotNull(f.name, ContextCompat.getDrawable(ctx, EggRes.fg(f)))
            assertNotNull(f.name, ContextCompat.getDrawable(ctx, EggRes.bg(f)))
        }
    }

    /** DEV 1911 could leave th launcher activity disabled ➡ th update receiver puts it back */
    @Test fun updateReEnablesTheLauncher() {
        val c = ComponentName(ctx, MainActivity::class.java)
        ctx.packageManager.setComponentEnabledSetting(c, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        launcherReset(ctx)
        assertEquals(PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, ctx.packageManager.getComponentEnabledSetting(c))
    }
}
