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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
    LaunchedEffect(Unit) { t.animateTo(1f, tween(1800, easing = LinearEasing)); done() }
    Dialog(onDismissRequest = {}, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
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
 * ☭ swoosh (strix @1103): floats up + grows ➡ orbits on a tilted circle (out + back in, near side big / far side small = 3D)
 * w a ghost trail ➡ shrinks into th middle + gone. Played on Amen = reading day counted.
 */
@Composable
fun SickleSwoosh(onDone: () -> Unit) {
    val done by rememberUpdatedState(onDone)
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(1900, easing = LinearEasing)); done() }
    Overlay {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val w = constraints.maxWidth.toFloat(); val h = constraints.maxHeight.toFloat()
            // position + scale + spin + alpha at time u
            fun at(u0: Float): FloatArray {
                val u = u0.coerceIn(0f, 1f)
                val cx = w / 2; val cy = h * 0.42f
                return when {
                    u < 0.2f -> { val p = u / 0.2f; val e = 1 - (1 - p) * (1 - p)
                        floatArrayOf(cx, lerpF(h * 0.85f, cy, e), lerpF(0.4f, 2f, e), 0f, 1f) }
                    u < 0.85f -> { val p = (u - 0.2f) / 0.65f; val th = (2.5f * 2 * PI * p).toFloat()
                        val rr = w * 0.38f * sin(PI * p).toFloat()             // out ➡ back in
                        val depth = cos(th)                                    // +1 = near
                        floatArrayOf(cx + rr * sin(th), cy + rr * cos(th) * 0.45f, 2f * (0.75f + 0.35f * depth), th * 57.3f, 1f) }
                    else -> { val p = (u - 0.85f) / 0.15f
                        floatArrayOf(cx, cy, 2f * (1 - p), 360f * p, 1 - p) }
                }
            }
            for (k in 6 downTo 0) {  // k > 0 = ghost trail behind
                val a = at(t.value - k * 0.022f)
                Text("☭", fontSize = 56.sp, color = if (k == 0) Color(0xFFE53935) else Color(0xFFE8B931),
                    modifier = Modifier.graphicsLayer {
                        translationX = a[0] - 56.sp.toPx() * 0.35f; translationY = a[1] - 56.sp.toPx() * 0.6f
                        scaleX = a[2]; scaleY = a[2]; rotationZ = a[3]
                        alpha = a[4] * (if (k == 0) 1f else (1f - k / 7f) * 0.45f)
                    })
            }
        }
    }
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
