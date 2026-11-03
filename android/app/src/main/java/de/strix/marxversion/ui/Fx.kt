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
import kotlin.random.Random

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
