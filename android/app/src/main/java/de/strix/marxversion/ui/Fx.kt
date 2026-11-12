package de.strix.marxversion.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.sqrt
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.random.Random
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── 🎉 small effects: confetti (full Stempelkarte) · 📳 shake detector ("Krise!") ──

private val CONFETTI = listOf(0xFFB3261E, 0xFFE8B931, 0xFFFFFFFF, 0xFFF29E4C, 0xFF2B1B4F).map { Color(it) }

private class Bit(val x: Float, val speed: Float, val drift: Float, val spin: Float, val color: Color, val w: Float)

/**
 * 🎉 ~1.8 s o red/gold paper falling over evything (own transparent dialog ➡ also above th Losung popup) ➡ onDone.
 * Hooked: celebrate th milestone right when it happens (reward o th self).
 */
@Composable
fun Confetti(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    val t = remember { Animatable(0f) }
    val bits = remember { List(110) { Bit(Random.nextFloat(), 0.6f + Random.nextFloat() * 0.9f, Random.nextFloat() - 0.5f,
        Random.nextFloat() * 720f - 360f, CONFETTI.random(), 8f + Random.nextFloat() * 10f) } }
    // 🐛 @1107: th overlay is a full-screen Dialog ➡ if it outlives th animation it eats evy touch (= "app frozen",
    // happened w `Confetti {}` i th Orden popup) ➡ it removes itself, whatever th caller does
    var alive by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(1800, easing = LinearEasing)); alive = false; done() }
    if (alive) Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)  // no grey scrim
        Canvas(Modifier.fillMaxSize()) {
            bits.forEach { b ->
                val y = -40f + t.value * size.height * 1.3f * b.speed
                val x = b.x * size.width + b.drift * 200f * t.value
                rotate(b.spin * t.value, Offset(x, y)) {
                    drawRect(b.color, Offset(x - b.w / 2, y - b.w), Size(b.w, b.w * 2))
                }
            }
        }
    }
}

/** transparent full-screen overlay (above evything incl. other dialogs), no grey scrim */
@Composable
private fun Overlay(content: @Composable () -> Unit) =
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        (LocalView.current.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
        content()
    }

/**
 * 📉 KRISE! ~1.4 s: screen flashes red 3×, "KRISE!" slams in + shakes, phone buzzes like a market crash ➡ onDone
 * (th crisis card itself is then th 1st card i th carousel)
 */
@Composable
fun KriseFlash(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    val ctx = LocalContext.current
    val word = kriseWord(LocalLang.current)
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        buzz(ctx, longArrayOf(0, 120, 60, 120, 60, 300))
        t.animateTo(1f, tween(1400, easing = LinearEasing)); done()
    }
    Overlay {
        val v = t.value
        val flash = (sin(v * 3 * PI).toFloat()).coerceAtLeast(0f) * 0.85f * (1f - v * 0.4f)
        Box(Modifier.fillMaxSize().background(Color(0xFFD50000).copy(alpha = flash)), contentAlignment = Alignment.Center) {
            Text(word, fontSize = 56.sp, fontWeight = FontWeight.Black, color = Color.White,
                modifier = Modifier.graphicsLayer {
                    val s = if (v < 0.15f) v / 0.15f * 1.4f else 1.4f - (v - 0.15f) * 0.5f
                    scaleX = s; scaleY = s
                    translationX = (sin(v * 70f) * 28f * (1f - v))
                    rotationZ = sin(v * 40f) * 6f * (1f - v)
                    alpha = if (v > 0.8f) (1f - v) / 0.2f else 1f
                })
        }
    }
}

/**
 * ☭ Amen fx (strix @1106): hammer (fr th left) + sickle (fr th right) float in, breathing (bigger/smaller) ➡ rotate
 * open = wind-up ➡ SMASH into each other = crossed ☭ ➡ impact (white flash + sparks + burst lines + ring + buzz, no txt) ➡ fade.
 * Played on Amen = reading day counted. ~2.3 s.
 */
private const val HIT = 0.62f   // impact moment (0..1)
private class Spark(val angle: Float, val speed: Float, val gold: Boolean)

/** pose o 1 tool at time u: x offset (fraction o width, side = -1 hammer / +1 sickle) · y bob · rotation · scale */
private fun pose(u0: Float, side: Float): FloatArray {
    val u = u0.coerceIn(0f, 1f)
    val bob = (sin(u * 2 * PI * 3) * 0.02).toFloat()
    fun out3(p: Float) = 1 - (1 - p) * (1 - p) * (1 - p)
    return when {
        u < 0.32f -> { val p = out3(u / 0.32f)                                        // float in + breathe
            floatArrayOf(side * lerpF(0.62f, 0.22f, p), bob, side * lerpF(80f, 20f, p), 0.85f + 0.25f * sin(p * PI.toFloat() * 2).coerceAtLeast(0f)) }
        u < 0.52f -> { val p = out3((u - 0.32f) / 0.2f)                               // open up = wind-up
            floatArrayOf(side * lerpF(0.22f, 0.30f, p), bob, side * lerpF(20f, 75f, p), lerpF(1f, 1.12f, p)) }
        u < HIT -> { val p = (u - 0.52f) / (HIT - 0.52f); val e = p * p              // SMASH (accelerating)
            floatArrayOf(side * lerpF(0.30f, 0.0f, e), 0f, side * lerpF(75f, -40f, e), 1.12f) }
        else -> { val q = (u - HIT) / (1 - HIT)                                       // crossed ☭, punch ➡ settle
            floatArrayOf(0f, 0f, side * -40f, 1f + 0.25f * (1 - q) * (1 - q)) }
    }
}

@Composable
fun SickleSwoosh(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    val ctx = LocalContext.current
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(HIT, tween((2300 * HIT).toInt(), easing = LinearEasing))
        buzz(ctx, longArrayOf(0, 35, 30, 80))                                         // the BAM, i th hand
        t.animateTo(1f, tween((2300 * (1 - HIT)).toInt(), easing = LinearEasing))
        done()
    }
    val gold = Color(0xFFE8B931); val red = Color(0xFFE53935)
    val sparks = remember { List(36) { Spark((Random.nextFloat() * 2 * PI).toFloat(), 0.4f + Random.nextFloat() * 0.8f, Random.nextBoolean()) } }
    Overlay {
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                val u = t.value
                val c = Offset(size.width / 2, size.height * 0.42f)
                val s = size.minDimension * 0.34f                                     // tool length
                val fade = if (u > 0.86f) 1 - (u - 0.86f) / 0.14f else 1f
                // 💥 impact burst behind th tools (no "BAM!" txt @1107: just th explosion)
                if (u >= HIT) {
                    val q = (u - HIT) / (1 - HIT)
                    if (q < 0.12f) drawCircle(Color.White.copy(alpha = 0.55f * (1 - q / 0.12f)), radius = s * 2.2f, center = c)   // flash
                    sparks.forEach { sp ->                                                                                  // sparks fly out
                        val r = s * (0.3f + sp.speed * 2.2f * q)
                        val p = c + Offset(cos(sp.angle) * r, sin(sp.angle) * r + q * q * s * 0.6f)   // + a bit o gravity
                        drawCircle(if (sp.gold) gold else red, radius = s * 0.035f * (1 - q) + 1f, center = p, alpha = 1 - q)
                    }
                    drawCircle(red.copy(alpha = 0.5f * (1 - q)), radius = s * (0.4f + 1.4f * q), center = c, style = Stroke(s * 0.06f * (1 - q) + 1f))
                    for (k in 0 until 14) {
                        val a = (k * 2 * PI / 14 + 0.2).toFloat()
                        val r0 = s * (0.55f + 0.9f * q); val r1 = r0 + s * 0.45f * (1 - q)
                        drawLine(if (k % 2 == 0) gold else Color.White, c + Offset(cos(a) * r0, sin(a) * r0), c + Offset(cos(a) * r1, sin(a) * r1),
                            strokeWidth = s * 0.05f, cap = StrokeCap.Round, alpha = (1 - q))
                    }
                }
                // ghost trail while flying (k > 0) = th swoosh
                for (k in (if (u < HIT) 4 else 0) downTo 0) {
                    val a = if (k == 0) 1f else (1f - k / 5f) * 0.35f
                    val h = pose(u - k * 0.018f, -1f); val sk = pose(u - k * 0.018f, 1f)
                    withTransform({ translate(c.x + h[0] * size.width, c.y + h[1] * size.height); rotate(h[2], Offset.Zero); scale(h[3], h[3], Offset.Zero) }) {
                        hammer(s, gold, a * fade)
                    }
                    withTransform({ translate(c.x + sk[0] * size.width, c.y + sk[1] * size.height); rotate(sk[2], Offset.Zero); scale(sk[3], sk[3], Offset.Zero) }) {
                        sickle(s, gold, a * fade)
                    }
                }
            }
        }
    }
}

/** 🔨 hammer, origin = grip point · head at th top (−y) */
private fun DrawScope.hammer(s: Float, col: Color, a: Float) {
    drawRoundRect(col, Offset(-0.06f * s, -0.36f * s), Size(0.12f * s, 0.92f * s), CornerRadius(0.05f * s), alpha = a)
    drawRoundRect(col, Offset(-0.32f * s, -0.52f * s), Size(0.58f * s, 0.22f * s), CornerRadius(0.04f * s), alpha = a)
}

/** 🌙 sickle: blade = thick arc, handle angled down-left */
private fun DrawScope.sickle(s: Float, col: Color, a: Float) {
    drawArc(col, startAngle = 200f, sweepAngle = 250f, useCenter = false, topLeft = Offset(-0.42f * s, -0.58f * s),
        size = Size(0.84f * s, 0.84f * s), style = Stroke(0.11f * s, cap = StrokeCap.Round), alpha = a)
    drawLine(col, Offset(0f, 0.25f * s), Offset(-0.2f * s, 0.6f * s), strokeWidth = 0.13f * s, cap = StrokeCap.Round, alpha = a)
}

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

/** 📳 a vibration pattern (ms: wait, buzz, wait, buzz …) */
fun buzz(ctx: Context, pattern: LongArray) {
    val vib = if (Build.VERSION.SDK_INT >= 31) ctx.getSystemService(VibratorManager::class.java)?.defaultVibrator
        else @Suppress("DEPRECATION") ctx.getSystemService(Vibrator::class.java)
    runCatching { vib?.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
}

/** 📳 calls onShake on a real shake (≥ 2.7 g, max 1×/1.5 s) while this composable is on screen */
@Composable
fun ShakeDetector(onShake: () -> Unit) {
    val ctx = LocalContext.current
    val cb by rememberUpdatedState(onShake)
    DisposableEffect(Unit) {
        val sm = ctx.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var last = 0L
        val l = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) {
                val (x, y, z) = e.values
                val g = sqrt(x * x + y * y + z * z) / SensorManager.GRAVITY_EARTH
                val now = System.currentTimeMillis()
                if (g > 2.7f && now - last > 1500) { last = now; cb() }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
        if (acc != null) sm.registerListener(l, acc, SensorManager.SENSOR_DELAY_UI)
        onDispose { sm.unregisterListener(l) }
    }
}

/** ✔️ "Tag geschafft" fx (QA @1112: ≠ ☭): check pops in w overshoot + green ring pulse + 📳 ➡ fade. ~1.2 s */
@Composable
fun CheckPop(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    val ctx = LocalContext.current
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        buzz(ctx, longArrayOf(0, 35, 30, 80))
        t.animateTo(1f, tween(1200, easing = LinearEasing)); done()
    }
    Overlay {
        val v = t.value
        val pop = if (v < 0.25f) v / 0.25f * 1.25f else 1.25f - (v - 0.25f).coerceAtMost(0.15f) / 0.15f * 0.25f
        val fade = if (v > 0.75f) (1f - v) / 0.25f else 1f
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val ring = (v / 0.6f).coerceIn(0f, 1f)
                drawCircle(Color(0xFF43A047).copy(alpha = (1f - ring) * 0.5f * fade), radius = 110f * (0.5f + ring * 0.5f) * 2f, style = Stroke(6f))
            }
            Text("✔️", fontSize = 96.sp, modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop; alpha = fade })
        }
    }
}
