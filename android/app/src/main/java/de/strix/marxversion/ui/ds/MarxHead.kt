package de.strix.marxversion.ui.ds

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.strix.marxversion.R
import de.strix.marxversion.logic.Feast

/**
 * 🧔 th ONLY place th picks a Marx head · plain day = th photo (`ic_marx_fg`) · feast = its 🥚 egg (D-066, was D-051 a
 * small props on th photo ➡ 🦠 @1120 "th icon should change") · same art as th launcher icon (bin/eggs.py ➡ EggRes)
 */
@Composable
fun MarxHead(feast: Feast?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    if (feast != null) EggBadge(feast, modifier, size)
    else Box(modifier.size(size)) { Image(painterResource(R.mipmap.ic_marx_fg), null, Modifier.fillMaxSize()) }
}

/**
 * 🥚 Today hero on a feast (D-066 "WOW"): th egg pops in (spring overshoot), wiggles evy ~5 s, tap = wiggle again
 * reduced motion (Android setting) ➡ still, full size
 */
@Composable
fun EggPop(feast: Feast, modifier: Modifier = Modifier, size: Dp = 72.dp) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val still = androidx.compose.runtime.remember { de.strix.marxversion.ui.reducedMotion(ctx) }
    val pop = androidx.compose.runtime.remember(feast) { androidx.compose.animation.core.Animatable(if (still) 1f else 0f) }
    val tilt = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    var poke by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    suspend fun wiggle() { if (!still) for (d in listOf(-14f, 12f, -8f, 5f, 0f)) tilt.animateTo(d, androidx.compose.animation.core.tween(90)) }
    androidx.compose.runtime.LaunchedEffect(feast) {
        if (still) return@LaunchedEffect
        pop.animateTo(1f, androidx.compose.animation.core.spring(dampingRatio = .38f, stiffness = 260f))
        while (true) { wiggle(); kotlinx.coroutines.delay(5200) }
    }
    androidx.compose.runtime.LaunchedEffect(poke) { if (poke > 0) wiggle() }
    EggBadge(feast, modifier.graphicsLayer { scaleX = pop.value; scaleY = pop.value; rotationZ = tilt.value }
        .pointerInput(Unit) { detectTapGestures { poke++ } }, size)
}

/**
 * 🥚 egg as a round badge = what th launcher shows: bg + fg, cropped 2 th inner 72 o th 108 canvas (× 1.5)
 * `ring` = thin white edge so it reads on any gradient
 */
@Composable
fun EggBadge(feast: Feast, modifier: Modifier = Modifier, size: Dp = 44.dp, ring: Boolean = true) {
    Box(modifier.size(size).clip(CircleShape).then(if (ring) Modifier.border(1.5.dp, Color.White.copy(alpha = .85f), CircleShape) else Modifier)
        .semantics { contentDescription = feast.emoji }) {
        val crop = Modifier.fillMaxSize().graphicsLayer { scaleX = 1.5f; scaleY = 1.5f }
        Image(painterResource(EggRes.bg(feast)), null, crop)
        Image(painterResource(EggRes.fg(feast)), null, crop)
    }
}
