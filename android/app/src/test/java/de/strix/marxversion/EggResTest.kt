package de.strix.marxversion

import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.notify.widgetHead
import de.strix.marxversion.ui.ds.EggRes
import de.strix.marxversion.ui.ds.eggBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** 🥚 D-066 (amended #241: in-app only, launcher icon stays plain) · all 30 eggs inflate */
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

    /** 📱 widget: evy egg renders as a badge bitmap (RemoteViews can't take th layered vectors) · ☭ only wo feast */
    @Test fun widgetShowsTheDaysEgg() {
        Feast.entries.forEach { f ->
            val b = eggBitmap(ctx, f, 66)
            assertEquals(66, b.width)
            assertEquals(f.name, 0, android.graphics.Color.alpha(b.getPixel(0, 0)))   // corner = outside th circle
        }
        assertEquals("☭ Marx verse  ·  🔥 3", widgetHead(null, "Marx verse", 3))
        assertEquals("Marx verse", widgetHead(Feast.GEBURTSTAG, "Marx verse", 0))
    }
}
