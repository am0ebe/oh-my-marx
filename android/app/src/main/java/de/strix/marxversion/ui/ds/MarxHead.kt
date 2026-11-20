package de.strix.marxversion.ui.ds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.strix.marxversion.R
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.Head

/**
 * 🧔 th ONLY place th picks a Marx head (C3 · D-051 a): base head (`ic_marx_fg`) + 1 overlay prop per feast
 * drawn props (same flat 2-3 colours, no raster): party hat · Santa hat · carnation · Phrygian cap · hard hat · candle ·
 * glasses (♥ / sun) · rainbow beard · dove · pumpkin glow · quiet († days) = greyscale
 * not-yet-drawn props (fist · dynamite · book …) = small token disc w th feast emoji on th shoulder (🔵 draw them, design § 14.2.2)
 * coords = fractions o th 108-dp adaptive-icon box (face ≈ centre, crown ≈ y .28, eyes ≈ y .44)
 */
@Composable
fun MarxHead(feast: Feast?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val grey = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    Box(modifier.size(size)) {
        Image(painterResource(R.mipmap.ic_marx_fg), null, Modifier.fillMaxSize(),
            colorFilter = if (feast?.quiet == true) grey else null)
        val h = feast?.head ?: Head.NONE
        if (h != Head.NONE) Canvas(Modifier.fillMaxSize()) { prop(h) }
        if (feast != null && h !in DRAWN && h != Head.NONE) Box(Modifier.align(Alignment.BottomEnd).size(size * .42f).clip(CircleShape)
            .background(Ds.paperHi), contentAlignment = Alignment.Center) {
            Text(feast.emoji, style = androidx.compose.material3.MaterialTheme.typography.labelSmall)
        }
    }
}

/** props drawn as vector overlay · rest = token disc */
val DRAWN = setOf(Head.PARTY_HAT, Head.SANTA_HAT, Head.CARNATION, Head.PHRYGIAN, Head.HARD_HAT, Head.CANDLE,
    Head.GLASSES, Head.HEART_GLASSES, Head.SUNGLASSES, Head.RAINBOW, Head.DOVE, Head.PUMPKIN)

private val GOLD = Color(0xFFE8B931); private val RED = Color(0xFFB3261E); private val INK = Color(0xFF2B2118)
private val WHITE = Color(0xFFFFFFFF); private val YELLOW = Color(0xFFFFC107); private val ORANGE = Color(0xFFE65100)

private fun DrawScope.p(x: Float, y: Float) = Offset(size.width * x, size.height * y)
private fun DrawScope.tri(a: Offset, b: Offset, c: Offset, col: Color) =
    drawPath(Path().apply { moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(c.x, c.y); close() }, col)

private fun DrawScope.prop(h: Head) {
    val w = size.width
    when (h) {
        Head.PARTY_HAT -> { tri(p(.38f, .27f), p(.62f, .27f), p(.54f, .02f), GOLD)
            drawLine(RED, p(.42f, .2f), p(.6f, .17f), w * .025f); drawCircle(RED, w * .04f, p(.54f, .03f)) }
        Head.SANTA_HAT -> { tri(p(.3f, .27f), p(.7f, .27f), p(.78f, .06f), RED)
            drawRoundRect(WHITE, p(.28f, .24f), Size(w * .44f, w * .07f), androidx.compose.ui.geometry.CornerRadius(w * .035f))
            drawCircle(WHITE, w * .055f, p(.8f, .07f)) }
        Head.PHRYGIAN -> { drawPath(Path().apply { moveTo(p(.3f, .28f).x, p(.3f, .28f).y)
            quadraticTo(p(.34f, .04f).x, p(.34f, .04f).y, p(.62f, .08f).x, p(.62f, .08f).y)
            quadraticTo(p(.74f, .12f).x, p(.74f, .12f).y, p(.66f, .2f).x, p(.66f, .2f).y)
            lineTo(p(.7f, .28f).x, p(.7f, .28f).y); close() }, RED)
            drawCircle(WHITE, w * .035f, p(.4f, .24f)); drawCircle(Color(0xFF1E3A8A), w * .02f, p(.4f, .24f)) }
        Head.HARD_HAT -> { drawArc(YELLOW, 180f, 180f, true, p(.3f, .1f), Size(w * .4f, w * .34f))
            drawRoundRect(YELLOW, p(.25f, .25f), Size(w * .5f, w * .05f)) }
        Head.CARNATION -> { listOf(.0f to .0f, .03f to -.03f, -.03f to -.02f, .02f to .03f, -.02f to .03f).forEach { (dx, dy) ->
            drawCircle(RED, w * .045f, p(.68f + dx, .78f + dy)) }; drawLine(Color(0xFF2E7D32), p(.68f, .8f), p(.62f, .92f), w * .02f) }
        Head.CANDLE -> { drawRect(Color(0xFFF5F5F5), p(.74f, .72f), Size(w * .08f, w * .2f))
            drawOval(GOLD, p(.755f, .62f), Size(w * .05f, w * .1f)) }
        Head.GLASSES, Head.SUNGLASSES -> { val f = if (h == Head.SUNGLASSES) INK else Color.Transparent
            listOf(.41f, .59f).forEach { x -> drawCircle(f, w * .065f, p(x, .44f)); drawCircle(INK, w * .065f, p(x, .44f), style = Stroke(w * .018f)) }
            drawLine(INK, p(.475f, .44f), p(.525f, .44f), w * .018f) }
        Head.HEART_GLASSES -> listOf(.41f, .59f).forEach { x ->
            drawCircle(RED, w * .035f, p(x - .03f, .425f)); drawCircle(RED, w * .035f, p(x + .03f, .425f))
            tri(p(x - .065f, .435f), p(x + .065f, .435f), p(x, .5f), RED) }
        Head.RAINBOW -> listOf(RED, ORANGE, YELLOW, Color(0xFF2E7D32), Color(0xFF1565C0), Color(0xFF6A1B9A)).forEachIndexed { i, c ->
            drawLine(c.copy(alpha = .75f), p(.36f, .62f + i * .035f), p(.64f, .62f + i * .035f), w * .03f) }
        Head.DOVE -> { drawOval(WHITE, p(.66f, .66f), Size(w * .16f, w * .08f)); tri(p(.7f, .68f), p(.78f, .68f), p(.72f, .6f), WHITE)
            drawCircle(WHITE, w * .03f, p(.83f, .66f)) }
        Head.PUMPKIN -> drawCircle(ORANGE.copy(alpha = .35f), w * .48f, center, style = Stroke(w * .05f))
        else -> {}
    }
}
