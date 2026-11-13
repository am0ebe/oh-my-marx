package de.strix.marxversion.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

/**
 * 🔓 "Hooked-Modus freigeschaltet!" (big round #5 · D-025) — progressive disclosure: aft 7 reading days o th 1st finished
 * plan th app shows its own tricks. ~2 s: gold padlock wiggles ➡ shackle springs open ➡ 🪝 drops in w a bounce + 📳 + confetti.
 * Hooked ch4 (reward o th self) + Duolingo-style unlock moment · th dialog itself says it's a trick (honest, like all 🪝 notes)
 */
@Composable
fun HookedUnlockDialog(turnOn: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(0.42f, tween(900, easing = LinearEasing))
        buzz(ctx, longArrayOf(0, 30, 50, 120))          // 📳 click o th lock
        t.animateTo(1f, tween(1100, easing = LinearEasing))
    }
    Confetti {}
    MarxDialog(onDone, title = tr("🔓 Freigeschaltet!", "🔓 Unlocked!"), actions = {
        TextButton(onClick = onDone) { Text(tr("Später", "Later")) }
        Button(onClick = { turnOn(); onDone() }) { Text(tr("🪝 Einschalten", "🪝 Turn it on")) }
    }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.size(140.dp).clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2B1B4F), Color(0xFF14284A))))
                .border(2.dp, Gold, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                Padlock(t.value)
            }
            Text(tr("Hooked-Modus", "Hooked mode"), fontSize = 22.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif,
                modifier = Modifier.padding(top = 12.dp).graphicsLayer { alpha = ((t.value - 0.55f) / 0.2f).coerceIn(0f, 1f) })
            Text(tr("Du liest regelmäßig — Zeit für einen Blick hinter die Kulissen: gelbe 🪝-Zettel zeigen an jedem Trick, wie die App dich anfixt. Auch dieses Fenster ist einer (Freischalt-Moment, Hooked Kap. 4).",
                "You read regularly — time for a look behind the scenes: yellow 🪝 notes show on every trick how the app hooks you. This window is one too (unlock moment, Hooked ch. 4)."),
                fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(tr("🏅 neues Abzeichen: Hinter den Kulissen", "🏅 new badge: Behind the scenes"), fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/** gold padlock · t 0..0.42 wiggle · 0.42..0.6 shackle lifts + swings open · 0.55..1 🪝 drops w bounce */
@Composable
private fun Padlock(t: Float) {
    val wiggle = if (t < 0.42f) sin(t * 60f) * 8f * (t / 0.42f) else 0f
    val open = ((t - 0.42f) / 0.18f).coerceIn(0f, 1f)
    val drop = ((t - 0.55f) / 0.3f).coerceIn(0f, 1f)
    val bounce = if (drop < 1f) 1f - (1f - drop) * (1f - drop) else 1f
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(90.dp).graphicsLayer { rotationZ = wiggle; alpha = 1f - ((t - 0.8f) / 0.2f).coerceIn(0f, 0.65f) }) {
            val w = size.width; val h = size.height
            val body = Size(w * 0.7f, h * 0.48f)
            val bodyTop = h * 0.46f
            // shackle: lifts by ~12 % then swings open around its right foot
            rotate(-35f * open, Offset(w * 0.68f, bodyTop - h * 0.12f * open)) {
                drawArc(Gold, 180f, 180f, false, Offset(w * 0.28f, h * 0.12f - h * 0.12f * open), Size(w * 0.44f, h * 0.5f),
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
            }
            drawRoundRect(Gold, Offset((w - body.width) / 2, bodyTop), body, CornerRadius(w * 0.08f))
            drawCircle(Color(0xFF14284A), radius = w * 0.06f, center = Offset(w / 2, bodyTop + body.height * 0.45f))
        }
        Text("🪝", fontSize = 54.sp, modifier = Modifier.graphicsLayer {
            translationY = (bounce - 1f) * 220f; alpha = drop.coerceAtMost(1f) * 1f; scaleX = 0.6f + 0.4f * bounce; scaleY = scaleX
        })
    }
}
