package de.strix.marxversion.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.strix.marxversion.R
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Space
import kotlin.math.sin

/** dark "velvet cushion" behind gold (gold on paper = 1.6, washed out) · fr tokens, no raw hex */
private val Velvet = Brush.verticalGradient(listOf(Ds.blue, lerp(Ds.blue, Color.Black, 0.35f)))
private val Keyhole = lerp(Ds.blue, Color.Black, 0.35f)

/**
 * 🔓 "Hooked-Modus freigeschaltet!" (big round #5 · D-025) — progressive disclosure: aft 7 reading days o th 1st finished
 * plan th app shows its own tricks. ~2 s: gold padlock wiggles ➡ shackle springs open ➡ 🪝 drops in w a bounce + 📳 + confetti.
 * Hooked ch4 (reward o th self) + Duolingo-style unlock moment · th dialog itself says it's a trick (honest, like all 🪝 notes)
 * look (§ 7 reward dialog, L5 @1114): 96 medal-size art · 1 line y · Primary-L "Switch on" + Text "Later" stacked
 * queued: MainActivity shows it via th celebration queue (ui/Fx.kt), 1 reward at a ti
 */
@Composable
fun HookedUnlockDialog(turnOn: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    val haptics = rememberHaptics()
    val still = remember { reducedMotion(ctx) }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (still) { t.snapTo(1f); buzz(ctx, longArrayOf(0, 30, 50, 120)); return@LaunchedEffect }
        t.animateTo(0.42f, tween(900, easing = LinearEasing))
        buzz(ctx, longArrayOf(0, 30, 50, 120))          // 📳 click o th lock
        t.animateTo(1f, tween(1100, easing = LinearEasing))
    }
    Confetti {}
    MarxDialog(onDone, title = tr("🔓 Freigeschaltet!", "🔓 Unlocked!"),
        primary = DialogAction(tr("Einschalten", "Switch on")) { haptics.confirm(); turnOn(); onDone() },
        defer = DialogAction(tr("Später", "Later"), onDone),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            val shape = MaterialTheme.shapes.large
            Box(Modifier.size(120.dp).clip(shape).background(Velvet).border(2.dp, Ds.gold, shape), contentAlignment = Alignment.Center) {
                Padlock(t.value)
            }
            Text(tr("Hooked-Modus", "Hooked mode"), style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                modifier = Modifier.padding(top = Space.m).graphicsLayer { alpha = ((t.value - 0.55f) / 0.2f).coerceIn(0f, 1f) })
            Text(tr("Du liest regelmäßig — Zeit für einen Blick hinter die Kulissen: gelbe 🪝-Zettel zeigen an jedem Trick, wie die App dich anfixt. Auch dieses Fenster ist einer (Freischalt-Moment, Hooked Kap. 4).",
                "You read regularly — time for a look behind the scenes: yellow 🪝 notes show on every trick how the app hooks you. This window is one too (unlock moment, Hooked ch. 4)."),
                style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = Space.s))
            Text(tr("🏅 neues Abzeichen: Hinter den Kulissen", "🏅 new badge: Behind the scenes"),
                style = MaterialTheme.typography.labelMedium, color = Ds.goldDeep, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = Space.s))
        }
    }
}

/** gold padlock · t 0..0.42 wiggle · 0.42..0.6 shackle lifts + swings open · 0.55..1 🪝 (vector) drops w bounce */
@Composable
private fun Padlock(t: Float) {
    val wiggle = if (t < 0.42f) sin(t * 60f) * 8f * (t / 0.42f) else 0f
    val open = ((t - 0.42f) / 0.18f).coerceIn(0f, 1f)
    val drop = ((t - 0.55f) / 0.3f).coerceIn(0f, 1f)
    val bounce = if (drop < 1f) 1f - (1f - drop) * (1f - drop) else 1f
    Box(contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(80.dp).graphicsLayer { rotationZ = wiggle; alpha = 1f - ((t - 0.8f) / 0.2f).coerceIn(0f, 0.65f) }) {
            val w = size.width; val h = size.height
            val body = Size(w * 0.7f, h * 0.48f)
            val bodyTop = h * 0.46f
            // shackle: lifts by ~12 % then swings open around its right foot
            rotate(-35f * open, Offset(w * 0.68f, bodyTop - h * 0.12f * open)) {
                drawArc(Ds.gold, 180f, 180f, false, Offset(w * 0.28f, h * 0.12f - h * 0.12f * open), Size(w * 0.44f, h * 0.5f),
                    style = Stroke(width = w * 0.09f, cap = StrokeCap.Round))
            }
            drawRoundRect(Ds.gold, Offset((w - body.width) / 2, bodyTop), body, CornerRadius(w * 0.08f))
            drawCircle(Keyhole, radius = w * 0.06f, center = Offset(w / 2, bodyTop + body.height * 0.45f))
        }
        Icon(painterResource(R.drawable.ic_hook), null, tint = Ds.gold, modifier = Modifier.size(56.dp).graphicsLayer {
            translationY = (bounce - 1f) * 220f; alpha = drop; scaleX = 0.6f + 0.4f * bounce; scaleY = scaleX
        })
    }
}
