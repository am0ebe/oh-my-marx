package de.strix.marxversion

import de.strix.marxversion.ui.Celebration
import de.strix.marxversion.ui.CelebrationQueue
import de.strix.marxversion.ui.Fx
import de.strix.marxversion.ui.amenFx
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 🎉 1 celebration at a ti (D-034 · design_system.md § 8): ui/Fx.kt CelebrationQueue
 * pure logic, no clock: th nxt item starts IN th `done()` call ➡ no fixed waits (was: Orden dialog aft a blind 2600 ms)
 */
class FxQueueTest {
    @Test fun twoRewardsSameTap_shownOneAfterTheOther_noDelay() {
        val q = CelebrationQueue()
        val amen = q.play(amenFx(rare = false))          // Amen tap ➡ daily CheckPop
        assertTrue(q.reward("orden"))                     // same tap earns a badge
        assertSame(amen, q.current)                       // fx 1st, badge waits
        assertEquals(1, q.queued.size)
        q.done(amen)                                      // fx ends ➡ badge dialog right now
        val badge = q.current
        assertTrue(badge is Celebration.Reward && badge.key == "orden")
        q.done(badge!!)
        assertNull(q.current)
        assertFalse(q.busy)
    }

    @Test fun rareAmen_isBig_dailyIsCheck() {
        assertEquals(Fx.Big, amenFx(rare = true))
        assertEquals(Fx.Check, amenFx(rare = false))
    }

    @Test fun sameRewardKey_queuedOnce() {
        val q = CelebrationQueue()
        assertTrue(q.reward("orden"))
        assertFalse(q.reward("orden"))                    // flow re-emits while showing ➡ no 2nd dialog
        q.play(Fx.Check)
        assertFalse(q.reward("orden"))
        assertEquals(1, q.queued.size)
    }

    @Test fun twoEffects_areTwoItems_fifo() {
        val q = CelebrationQueue()
        val a = q.play(Fx.Check); val b = q.play(Fx.Check)
        assertSame(a, q.current)
        q.done(b)                                         // cancel th waiting one
        q.done(a)
        assertNull(q.current)
    }

    @Test fun directFxHold_blocksQueue_untilReleased() {
        val q = CelebrationQueue()
        val release = q.hold()                            // TodayScreen still draws SickleSwoosh itself
        q.reward("orden")
        assertNull(q.current)
        assertTrue(q.busy)
        release(); release()                              // idempotent
        assertTrue((q.current as? Celebration.Reward)?.key == "orden")
    }

    @Test fun drop_slotLeftScreen_unblocks() {
        val q = CelebrationQueue()
        q.reward("dayDone"); q.play(Fx.Swoosh)
        q.drop("dayDone")                                 // its RewardSlot left composition
        assertEquals(Fx.Swoosh, (q.current as Celebration.Effect).fx)
    }

    @Test fun chrome_perRoute() {
        assertEquals(Chrome.Root, chromeFor(null))
        assertEquals(Chrome.Root, chromeFor("today"))
        assertEquals(Chrome.Root, chromeFor("mine"))
        assertEquals(Chrome.Reader, chromeFor("chapter/{kap}?v={v}"))
        assertEquals(Chrome.Sub, chromeFor("plan/{id}"))
        assertEquals(Chrome.Sub, chromeFor("kreis/{i}"))
        assertEquals(Chrome.Sub, chromeFor("settings"))
        assertEquals(Chrome.Sub, chromeFor("hooked"))
    }
}
