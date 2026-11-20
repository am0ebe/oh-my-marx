package de.strix.marxversion.ui.ds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.logic.STRIKES_MAX
import kotlin.math.cos
import kotlin.math.sin

/**
 * ★ streak emblem (C2 · design § 14.1.3 · D-046 · D-047): th 1 star asset (`StarImage`, nvr a Path() copy) + tier ornaments
 * drawn AROUND it · 0 plain · 1 glow 7+ · 2 ring 14+ · 3 laurel 30+ · 4 5 stars on th ring 50+ · 5 halo w rays 100+ ·
 * 6 ribbon "200" 200+ · 7 full Orden (laurel + halo + ribbon w year) 365+
 * `grow` 0..1 = th newest ornament growing in (Meilenstein screen) · 1 = still
 */
@Composable
fun StreakStar(n: Int, tier: Int, modifier: Modifier = Modifier, size: Dp = 92.dp, grow: Float = 1f, onGradient: Boolean = true) {
    val gold = Ds.gold
    val laurel = if (onGradient) Ds.gold else Ds.goldDeep
    Box(modifier.size(size).semantics { contentDescription = "streak $n · tier $tier" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center; val r = this.size.minDimension / 2
            fun a(t: Int) = if (tier == t) grow else if (tier > t) 1f else 0f
            // 1 · glow (gold 30 % radial) — stays under evy higher tier
            if (tier >= 1) drawCircle(Brush.radialGradient(listOf(gold.copy(alpha = .30f * a(1).coerceAtLeast(if (tier > 1) 1f else 0f)), Color.Transparent), c, r), r, c)
            // 5 · halo w rays (Saint Marx)
            if (tier >= 5) rays(c, r, gold, a(5).let { if (tier == 7) 1f else it })
            // 2 · thin ring
            if (tier >= 2) drawCircle(gold.copy(alpha = a(2).coerceAtLeast(if (tier > 2) 1f else 0f)), r * .93f, c, style = Stroke(2.dp.toPx()))
            // 4 · 5 tiny stars on th ring
            if (tier >= 4) repeat(5) { i ->
                val ang = Math.toRadians(-90.0 + i * 72.0)
                tinyStar(Offset(c.x + r * .93f * cos(ang).toFloat(), c.y + r * .93f * sin(ang).toFloat()), r * .09f * a(4).coerceAtLeast(if (tier > 4) 1f else 0f), gold)
            }
            // 3 · laurel: 2 branches round th bottom
            if (tier >= 3) laurel(c, r, laurel, a(3).coerceAtLeast(if (tier > 3) 1f else 0f))
        }
        StarImage(Modifier.fillMaxSize().padding(size * .1f))
        val k = size.value / 92f
        Text("$n", color = Color.White, fontSize = ((if (n < 100) 26 else 20) * k).sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = (6 * k).dp))
        // 6 · ribbon under it (red + gold txt) · 7 = w th year
        if (tier >= 6) Box(Modifier.align(Alignment.BottomCenter).clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
            .background(Ds.red).border(1.dp, gold, androidx.compose.foundation.shape.RoundedCornerShape(3.dp))
            .padding(horizontal = (6 * k).dp)) {
            Text(if (tier >= 7) "${n / 365} ✦ ${java.time.Year.now()}" else "200", color = gold, fontSize = (10 * k).sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun DrawScope.rays(c: Offset, r: Float, col: Color, a: Float) {
    drawCircle(col.copy(alpha = .55f * a), r * .78f, c, style = Stroke(3.dp.toPx()))
    repeat(16) { i ->
        rotate(i * 22.5f, c) {
            drawLine(col.copy(alpha = .6f * a), Offset(c.x, c.y - r * .80f), Offset(c.x, c.y - r * (.80f + .18f * a)), 2.dp.toPx())
        }
    }
}

private fun DrawScope.tinyStar(at: Offset, s: Float, col: Color) {
    if (s <= 0f) return
    val p = Path()
    repeat(10) { i ->
        val rr = if (i % 2 == 0) s else s * .45f
        val ang = Math.toRadians(-90.0 + i * 36.0)
        val x = at.x + rr * cos(ang).toFloat(); val y = at.y + rr * sin(ang).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close(); drawPath(p, col)
}

/** 2 branches: leaves along an arc fr bottom-centre up each side · `a` = how far they've grown */
private fun DrawScope.laurel(c: Offset, r: Float, col: Color, a: Float) {
    if (a <= 0f) return
    val leaves = (7 * a).toInt().coerceAtLeast(1)
    for (side in listOf(-1f, 1f)) for (i in 0 until leaves) {
        val t = 100f + i * 14f   // degrees fr th bottom, going up
        val ang = Math.toRadians((90.0 + side * (t - 90.0)))
        val p = Offset(c.x + r * .86f * cos(ang).toFloat(), c.y + r * .86f * sin(ang).toFloat())
        rotate((Math.toDegrees(ang) + 90).toFloat() + side * 30f, p) {
            drawOval(col, Offset(p.x - r * .05f, p.y - r * .11f), Size(r * .10f, r * .22f))
        }
    }
}

/** ✊ Streiktag slots next 2 th star: held = red ring + white fist · free = hollow ring (max 2, D-046) */
@Composable
fun StrikeSlots(held: Int, modifier: Modifier = Modifier, ring: Color = Color.White) {
    Row(modifier.semantics { contentDescription = "Streiktage $held / $STRIKES_MAX" }, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(STRIKES_MAX) { i ->
            Box(Modifier.size(16.dp).clip(CircleShape).then(if (i < held) Modifier.background(Ds.red) else Modifier)
                .border(1.5.dp, if (i < held) Ds.white else ring.copy(alpha = .6f), CircleShape), contentAlignment = Alignment.Center) {
                if (i < held) Text("✊", fontSize = 8.sp)
            }
        }
    }
}
