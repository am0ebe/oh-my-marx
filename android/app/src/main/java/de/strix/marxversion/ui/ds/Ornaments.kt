package de.strix.marxversion.ui.ds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 📜 holy-book devices (C4 · D-053 b · design § 14.3) · all drawn / type, no raster
 * headpiece (5) · tailpiece + seal (6) · ribbon bookmark (9) · gilt edge (10) · roman() 4 th incipit (4)
 */

/** I II III … (incipit "Kapitel IV") */
fun roman(n: Int): String {
    if (n <= 0) return n.toString()
    val v = listOf(1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC", 50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I")
    var r = n; val sb = StringBuilder()
    v.forEach { (k, s) -> while (r >= k) { sb.append(s); r -= k } }
    return sb.toString()
}

/** (5) printer's band over th chapter title */
@Composable
fun Headpiece(modifier: Modifier = Modifier) = Text("❦  ✥  ❦  ✥  ❦", style = OmmType.bookHead, color = Ds.red.copy(alpha = .7f),
    textAlign = TextAlign.Center, modifier = modifier.fillMaxWidth())

/** (6) ☙ vignette at chapter end · `sealed` = chapter fully read ➡ red wax ☭ seal pressed onto it (T2b) */
@Composable
fun Tailpiece(sealed: Boolean, modifier: Modifier = Modifier) = Box(modifier.fillMaxWidth().padding(vertical = Space.l), contentAlignment = Alignment.Center) {
    // qa_1120: 1 symbol only — read ➡ th ☭ seal · not yet ➡ ☙ ❧ (both stacked looked weird)
    if (!sealed) Text("☙  ❧", style = OmmType.bookHead, color = Ds.ink.copy(alpha = .35f), textAlign = TextAlign.Center)
    else Box(Modifier.size(34.dp).rotate(-8f).clip(CircleShape)
        .background(Brush.radialGradient(listOf(Ds.red, Ds.redDeep))).semantics { contentDescription = "sealed" },
        contentAlignment = Alignment.Center) {
        Text("☭", color = Ds.gold, style = OmmType.bookHead.copy(fontWeight = FontWeight.Bold))
    }
}

/** (9) red silk ribbon hanging fr th top edge, swallowtail end · put at TopEnd o a bookmarked page */
@Composable
fun Ribbon(modifier: Modifier = Modifier) = Canvas(modifier.width(14.dp).height(72.dp).semantics { contentDescription = "bookmark ribbon" }) {
    val w = size.width; val h = size.height
    drawPath(Path().apply { moveTo(0f, 0f); lineTo(w, 0f); lineTo(w, h); lineTo(w / 2, h - w * .8f); lineTo(0f, h); close() },
        Brush.horizontalGradient(listOf(Ds.redDeep, Ds.red, Ds.redDeep)))
}

/** (10) 2dp gold gradient on th page's outer edge */
@Composable
fun GiltEdge(modifier: Modifier = Modifier) = Box(modifier.width(2.dp).fillMaxHeight()
    .background(Brush.verticalGradient(listOf(Ds.goldDeep, Ds.gold, Color(0xFFF6E7B8), Ds.gold, Ds.goldDeep))))
