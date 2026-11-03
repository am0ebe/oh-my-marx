package de.strix.marxversion

import de.strix.marxversion.logic.Holy
import de.strix.marxversion.logic.Layer
import de.strix.marxversion.logic.Pcm
import de.strix.marxversion.logic.Room
import de.strix.marxversion.logic.Wav
import de.strix.marxversion.logic.holy
import de.strix.marxversion.logic.mix
import de.strix.marxversion.logic.reverb
import de.strix.marxversion.logic.rms
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

/** 🔊 holy-voice DSP (logic/Voice.kt) — no TTS needed, sine "voices" */
class VoiceTest {
    private fun tone(hz: Double, sec: Double, rate: Int = 24000) =
        Pcm(rate, FloatArray((sec * rate).toInt()) { (0.5 * sin(2 * PI * hz * it / rate)).toFloat() })

    @Test fun wavRoundTrip() {
        val p = tone(220.0, 0.2)
        val back = Wav.read(Wav.write(p))
        assertEquals(24000, back.rate)
        assertEquals(p.s.size, back.s.size)
        p.s.indices.forEach { assertEquals(p.s[it], back.s[it], 1f / 16000) }
    }

    @Test fun wavStreamingHeader() {
        // some engines write data size 0 while streaming ➡ read th rest o th file
        val b = Wav.write(tone(220.0, 0.1))
        b[40] = 0; b[41] = 0; b[42] = 0; b[43] = 0
        assertEquals(2400, Wav.read(b).s.size)
    }

    @Test fun mixDelays() {
        val a = Pcm(1000, floatArrayOf(1f, 1f))
        val m = mix(listOf(a to Layer(1f), a to Layer(1f, 0.5f, 3)))  // 3 ms @1 kHz = 3 samples
        assertEquals(listOf(1f, 1f, 0f, 0.5f, 0.5f), m.s.toList())
    }

    @Test fun hallRingsOutAndDecays() {
        val p = tone(300.0, 0.5)
        val r = reverb(p, Room.KATHEDRALE)
        assertEquals(p.s.size + 70 * 24 + (3.5 * 24000).toInt(), r.s.size)
        val tail = r.s.copyOfRange(p.s.size, r.s.size)
        val early = rms(tail.copyOfRange(0, 12000))
        val late = rms(tail.copyOfRange(tail.size - 12000, tail.size))
        assertTrue("hall audible aft th voice stops", early > 0.01f)
        assertTrue("hall decays: $early ➡ $late", late < early / 10)
    }

    @Test fun holyIsNormalized() {
        val h = Holy(listOf(Layer(0.4f), Layer(0.8f, 0.55f, 25), Layer(1.3f, 0.25f, 45)), Room.KATHEDRALE)
        val out = holy(listOf(tone(110.0, 0.4), tone(220.0, 0.4), tone(330.0, 0.4)), h)
        assertEquals(0.9f, out.s.maxOf { abs(it) }, 1e-4f)
    }
}
