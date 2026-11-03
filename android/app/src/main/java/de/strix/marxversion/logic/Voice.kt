package de.strix.marxversion.logic

import java.nio.ByteBuffer
import java.nio.ByteOrder

// ── 🔊 heilige Stimme = Android TTS ➡ wav ➡ layered + Hall, all on th fon ──
// pure Kotlin (no android.*) ➡ JVM-testable · Speaker (ui/Share.kt) does th TTS + playback
//
// "voice o God" trick (film/radio classic): same line spoken at 2-3 pitches at once
// (octave below = authority, above = choir) + a long church reverb

/** mono PCM, samples i -1..1 */
class Pcm(val rate: Int, val s: FloatArray)

/** 1 TTS pass: pitch fed 2 th engine · gain i th mix · delay = slight smear (choir, not unison) */
data class Layer(val pitch: Float, val gain: Float = 1f, val delayMs: Int = 0)

/** fb = comb feedback (room size) · damp = hi-freq loss · wet = hall vs dry (RMS ratio) · tail = extra seconds 2 ring out */
enum class Room(val fb: Float, val damp: Float, val wet: Float, val preMs: Int, val tailS: Float) {
    LESESAAL(0.74f, 0.45f, 0.28f, 12, 1.2f),     // British Museum reading room
    KIRCHE(0.84f, 0.30f, 0.45f, 35, 2.2f),
    KATHEDRALE(0.90f, 0.20f, 0.65f, 70, 3.5f),
}

data class Holy(val layers: List<Layer>, val room: Room)

object Wav {
    /** RIFF/WAVE 16-bit PCM (what TextToSpeech.synthesizeToFile writes) · stereo ➡ mono */
    fun read(b: ByteArray): Pcm {
        require(b.size >= 12 && String(b, 0, 4, Charsets.US_ASCII) == "RIFF" && String(b, 8, 4, Charsets.US_ASCII) == "WAVE") { "not a wav" }
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        var p = 12; var rate = 0; var ch = 1; var bits = 16
        while (p + 8 <= b.size) {
            val id = String(b, p, 4, Charsets.US_ASCII)
            var len = bb.getInt(p + 4)
            val body = p + 8
            if (id == "fmt ") { ch = bb.getShort(body + 2).toInt(); rate = bb.getInt(body + 4); bits = bb.getShort(body + 14).toInt() }
            if (id == "data") {
                require(bits == 16 && rate > 0) { "need 16-bit pcm" }
                // streaming writers leave size 0 / -1 ➡ take th rest o th file
                if (len <= 0 || body + len > b.size) len = b.size - body
                val n = len / 2 / ch
                val out = FloatArray(n)
                for (i in 0 until n) {
                    var acc = 0f
                    for (c in 0 until ch) acc += bb.getShort(body + (i * ch + c) * 2) / 32768f
                    out[i] = acc / ch
                }
                return Pcm(rate, out)
            }
            p = body + len + (len and 1)  // chunks r word-aligned
        }
        error("wav: no data chunk")
    }

    fun write(p: Pcm): ByteArray {
        val bb = ByteBuffer.allocate(44 + p.s.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        bb.put("RIFF".toByteArray()).putInt(36 + p.s.size * 2).put("WAVE".toByteArray())
        bb.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1).putInt(p.rate).putInt(p.rate * 2).putShort(2).putShort(16)
        bb.put("data".toByteArray()).putInt(p.s.size * 2)
        p.s.forEach { bb.putShort((it.coerceIn(-1f, 1f) * 32767).toInt().toShort()) }
        return bb.array()
    }
}

/** sum th layers (each w its gain + delay) · all same rate (same engine) */
fun mix(parts: List<Pair<Pcm, Layer>>): Pcm {
    val rate = parts.first().first.rate
    val n = parts.maxOf { (p, l) -> p.s.size + l.delayMs * rate / 1000 }
    val out = FloatArray(n)
    parts.forEach { (p, l) ->
        val off = l.delayMs * rate / 1000
        p.s.forEachIndexed { i, v -> out[i + off] += v * l.gain }
    }
    return Pcm(rate, out)
}

/**
 * Freeverb-ish mono hall: 8 parallel damped combs ➡ 4 series allpasses (Schroeder/Moorer)
 * tunings r Jezar's (@44.1k) scaled 2 th TTS rate · wet level set by RMS so evy voice gets th same hall
 */
fun reverb(p: Pcm, r: Room): Pcm {
    val sc = p.rate / 44100f
    val pre = r.preMs * p.rate / 1000
    val n = p.s.size + pre + (r.tailS * p.rate).toInt()
    val combs = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617).map { FloatArray((it * sc).toInt().coerceAtLeast(1)) }
    val ci = IntArray(combs.size); val store = FloatArray(combs.size)
    val aps = intArrayOf(556, 441, 341, 225).map { FloatArray((it * sc).toInt().coerceAtLeast(1)) }
    val ai = IntArray(aps.size)
    val wet = FloatArray(n)
    for (i in 0 until n) {
        val x = if (i - pre in p.s.indices) p.s[i - pre] else 0f
        var acc = 0f
        combs.forEachIndexed { k, buf ->
            val y = buf[ci[k]]
            store[k] = y * (1 - r.damp) + store[k] * r.damp
            buf[ci[k]] = x + store[k] * r.fb
            ci[k] = (ci[k] + 1) % buf.size
            acc += y
        }
        aps.forEachIndexed { k, buf ->
            val b = buf[ai[k]]
            buf[ai[k]] = acc + b * 0.5f
            ai[k] = (ai[k] + 1) % buf.size
            acc = b - acc
        }
        wet[i] = acc
    }
    val g = r.wet * rms(p.s) / rms(wet).coerceAtLeast(1e-9f)
    return Pcm(p.rate, FloatArray(n) { i -> (if (i < p.s.size) p.s[i] else 0f) + wet[i] * g })
}

fun rms(x: FloatArray): Float = if (x.isEmpty()) 0f else kotlin.math.sqrt(x.fold(0.0) { a, v -> a + v * v } / x.size).toFloat()

/** loudest sample ➡ peak (layering + hall add up way past 1) */
fun normalize(p: Pcm, peak: Float = 0.9f): Pcm {
    val m = p.s.maxOfOrNull { kotlin.math.abs(it) } ?: 0f
    return if (m < 1e-6f) p else Pcm(p.rate, FloatArray(p.s.size) { p.s[it] * peak / m })
}

/** th whole chain: TTS passes (1 per layer, same order) ➡ 1 holy wav */
fun holy(passes: List<Pcm>, h: Holy): Pcm = normalize(reverb(mix(passes.zip(h.layers)), h.room))
