package de.strix.marxversion.ui.ds

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🔘 5 btn kinds — nothing else (design_system.md § 5 · D-030 ✔️ A+ @1114):
 * Primary (L 52 full-width w press edge · S 40 hug) · Tonal · Text · ActionItem (Circle · Bare) · Chip (Chips.kt)
 * round = tappable · 1 Primary per screen / dialog / panel · labels: sentence case, verb 1st, no emoji
 * states: pressed · disabled (ink 12 % fill + ink 38 % txt) · loading (spinner, width stays) · focus ring 2dp blue
 */
private val Pill = CircleShape
private val Disabled = Ds.ink.copy(alpha = .12f)
private val OnDisabled = Ds.ink.copy(alpha = .38f)
private val EDGE = 4.dp     // Primary-L press edge (B's letterpress, only on th 1 big btn)

/** shared press/focus/click plumbing · sink-free kinds get th 8 % ink overlay on press */
@Composable
private fun Modifier.press(src: MutableInteractionSource, enabled: Boolean, onClick: () -> Unit): Modifier {
    val focused by src.collectIsFocusedAsState()
    return this
        .then(if (focused) Modifier.border(2.dp, Ds.blue, Pill) else Modifier)
        .clip(Pill)
        .clickable(interactionSource = src, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

@Composable
private fun Label(text: String, color: Color, style: TextStyle, loading: Boolean) = Box(contentAlignment = Alignment.Center) {
    Text(text, color = color, style = style, textAlign = TextAlign.Center, modifier = Modifier.alpha(if (loading) 0f else 1f))
    if (loading) CircularProgressIndicator(Modifier.size(18.dp), color = color, strokeWidth = 2.dp)
}

/**
 * 🔴 THE action (Amen · day done · start plan · install · send) · max 1 per view
 * large = h 52 full-width + 4dp redDeep edge th pill sinks into on press (spring) · small = h 40, hugs its label
 */
@Composable
fun Primary(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier, large: Boolean = true,
    enabled: Boolean = true, loading: Boolean = false, src: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by src.collectIsPressedAsState()
    val h = if (large) 52.dp else 40.dp
    val edge = if (large && enabled) EDGE else 0.dp
    val sink by animateDpAsState(if (pressed && edge > 0.dp) edge - 1.dp else 0.dp,
        spring(dampingRatio = Motion.pressDamping), label = "sink")
    val bg = when {
        !enabled -> Disabled
        pressed && !large -> lerp(Ds.red, Ds.ink, .22f)
        else -> Ds.red
    }
    val width = if (large) Modifier.fillMaxWidth() else Modifier
    Box(modifier.then(width).minimumInteractiveComponentSize().height(h + edge)
        .semantics(mergeDescendants = true) {}.press(src, enabled && !loading, onClick)) {
        if (edge > 0.dp) Box(Modifier.matchParentSize().padding(top = edge).background(Ds.redDeep, Pill))
        Box(width.offset { IntOffset(0, sink.roundToPx()) }.height(h).defaultMinSize(minWidth = 64.dp).background(bg, Pill).padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center) {
            Label(label, if (enabled) Ds.white else OnDisabled, if (large) OmmType.primaryL else OmmType.primaryS, loading)
        }
    }
}

/** 📘 2nd actions (found / join · copy link · refresh) · blueTint pill h 40 · max 2 side by side */
@Composable
fun Tonal(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, loading: Boolean = false,
    src: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by src.collectIsPressedAsState()
    Box(modifier.minimumInteractiveComponentSize().height(40.dp).defaultMinSize(minWidth = 64.dp)
        .press(src, enabled && !loading, onClick)
        .background(if (enabled) Ds.blueTint else Disabled)
        .then(if (pressed) Modifier.background(Ds.ink.copy(alpha = .08f)) else Modifier)
        .padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
        Label(label, if (enabled) Ds.blue else OnDisabled, MaterialTheme.typography.labelLarge, loading)
    }
}

/** defer / leave / more ("Later" · "Show me") · danger = redDeep (leave circle · delete note ➡ confirm dialog 1st) */
@Composable
fun TextBtn(
    label: String, onClick: () -> Unit, modifier: Modifier = Modifier, danger: Boolean = false, enabled: Boolean = true,
    src: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by src.collectIsPressedAsState()
    val fg = when { !enabled -> OnDisabled; danger -> Ds.redDeep; else -> Ds.blue }
    Box(modifier.minimumInteractiveComponentSize().height(40.dp).press(src, enabled, onClick)
        .then(if (pressed) Modifier.background(Ds.ink.copy(alpha = .08f)) else Modifier)
        .padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

enum class ActStyle { Circle, Bare }

/**
 * icon row item: mono icon + label-m under it · 48 target · max 5 per row
 * Circle = tint 10 % disc 44 (dflt blue ➡ blueTint) — quote card row, About row · Bare = icon only — verse menu i th book
 * glyph = 1-char fallback wh there's no vector (nvr an emoji i chrome)
 */
@Composable
fun ActionItem(
    icon: Int?, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: ActStyle = ActStyle.Circle,
    tint: Color = Ds.blue, labelColor: Color = tint, glyph: String? = null,
    src: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val pressed by src.collectIsPressedAsState()
    val w = if (style == ActStyle.Circle) Modifier.width(64.dp) else Modifier
    Column(modifier.then(w).clip(RoundedCornerShape(Radius.m))
        .clickable(interactionSource = src, indication = null, role = Role.Button, onClick = onClick)
        .then(if (pressed && style == ActStyle.Bare) Modifier.background(Ds.ink.copy(alpha = .08f)) else Modifier)
        .padding(vertical = if (style == ActStyle.Bare) 4.dp else 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        val glyphView: @Composable () -> Unit = {
            if (icon != null) Icon(painterResource(icon), label, Modifier.size(if (style == ActStyle.Bare) 22.dp else 20.dp), tint = tint)
            else Text(glyph.orEmpty(), color = tint, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (style == ActStyle.Circle) {
            Box(Modifier.size(Space.tap), contentAlignment = Alignment.Center) {
                Box(Modifier.size(44.dp).clip(CircleShape)
                    .background(tint.copy(alpha = if (pressed) .18f else .10f)), contentAlignment = Alignment.Center) { glyphView() }
            }
        } else glyphView()
        // qa_1117 r3: "Karte teilen" cut at 64dp ➡ long labels wrap 2 2 lines (centred) instead o clipping
        Text(label, color = labelColor, style = MaterialTheme.typography.labelMedium, maxLines = 2, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = if (style == ActStyle.Circle) 0.dp else Space.xxs))
    }
}

// ── singletons (1 look, fixed spot) ────────────────────────────────────────────────────────────────────────

/** ★ = close, evywhere (top-end o dialogs + sheets) · 36 visual, 48 target */
@Composable
fun CloseStar(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Ds.red, size: Dp = 36.dp, desc: String = "Close",
              variant: CloseVariant = CloseVariant.V2) {   // ✔️ D-055 V2 (🦠 @1120 via M8) · was V1
    // qa_1116 L3 #6 + #7: th 1 star asset (res/drawable/ic_star.xml, bbox-centred i its box = no nudge) · bigger (pad 8 %) ·
    //   gold rim = th hero star's, scaled · circle = tint 10 % + 30 % hairline
    // qa_1117 r1 "star too low + too big i th circle": pad 8 ➡ 18 % · bbox-centred ≠ optically centred (th star's mass =
    //   its circumcentre, 1.09/24 below th bbox centre) ➡ lift by that share o th star's drawn size · hero star untouched
    // 🎨 D-055 (design § 14.5 · lane C6): V1-V8 = th variants sheet (test/CloseSheetTest ➡ close_sheet.png) · 🦠 picks ➡ dflt
    //   changes here, 1 line · evy variant draws th SAME ic_star (tint · cut-outs via blend), nvr a Path() copy o it
    val v = variant
    // V5 · V7 = solid red objects ➡ a light tint (white on th hero gradient) would vanish ➡ they stay brand red there
    val solid = if (tint.luminance() > 0.5f) Ds.red else tint
    val star = size * (1 - 2 * (if (v == CloseVariant.V2) 0.11f else 0.18f))
    val lift = Modifier.offset(y = -star * (1.09f / 24f))
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val spin by androidx.compose.animation.core.animateFloatAsState(if (v == CloseVariant.V8 && pressed) 72f else 0f,
        spring(dampingRatio = 0.45f, stiffness = 500f), label = "spin")
    val (bg, rim) = when (v) {
        CloseVariant.V2, CloseVariant.V7 -> Color.Transparent to null
        CloseVariant.V5 -> solid to null
        CloseVariant.V6 -> Color.Transparent to BorderStroke(1.dp, Ds.gold.copy(alpha = 0.55f))
        else -> tint.copy(alpha = 0.10f) to BorderStroke(1.dp, tint.copy(alpha = 0.30f))
    }
    Surface(onClick = onClick, shape = CircleShape, color = bg, border = rim, interactionSource = src,
        modifier = modifier.size(size).semantics { contentDescription = desc }) {
        Box(contentAlignment = Alignment.Center) {
            when (v) {
                CloseVariant.V1, CloseVariant.V2 -> StarImage(Modifier.size(star).then(lift))
                CloseVariant.V8 -> StarImage(Modifier.size(star).then(lift).graphicsLayer { rotationZ = spin; transformOrigin = STAR_PIVOT })
                CloseVariant.V3 -> CloseCross(Modifier.size(size * 0.42f), tint)
                CloseVariant.V4 -> StarDrawn(Modifier.size(star).then(lift), cross = true)
                CloseVariant.V5 -> StarDrawn(Modifier.size(star).then(lift), fill = Ds.white)
                CloseVariant.V6 -> StarDrawn(Modifier.size(star).then(lift), fill = Ds.gold, hole = 0.62f)
                CloseVariant.V7 -> WaxSeal(Modifier.size(size), solid)
            }
        }
    }
}

/** 🎨 D-055 close btn variants (design § 14.5) · V1 = now · 🦠 picks fr close_sheet.png */
enum class CloseVariant(val look: String) {
    V1("now: ★ i tinted circle"), V2("bare ★, no circle"), V3("mono ✕ i circle"), V4("★ w ✕ cut-out"),
    V5("red disc, white ★"), V6("outline ★, gold rim"), V7("wax seal, embossed ★"), V8("V1 + spins 72° on press"),
}

/** th star's optical centre i its 24 box (circumcentre y 13.09) ➡ pivot 4 spin + hole scaling */
private val STAR_PIVOT = androidx.compose.ui.graphics.TransformOrigin(0.5f, 13.09f / 24f)

/** ic_star drawn i a Canvas ➡ tint (fill) · hole = same star scaled around its centre, punched out (outline look) ·
 *  cross = ✕ punched out o th middle · offscreen layer ➡ punches show whatever is behind (circle · gradient · dark) */
@Composable
private fun StarDrawn(modifier: Modifier, fill: Color? = null, hole: Float = 0f, cross: Boolean = false) {
    val p = painterResource(de.strix.marxversion.R.drawable.ic_star)
    Canvas(modifier.graphicsLayer { compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen }) {
        val cf = fill?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) }
        with(p) { draw(size, colorFilter = cf) }
        val c = Offset(size.width / 2, size.height * 13.09f / 24f)
        if (hole > 0f) scale(hole, hole, c) {
            drawIntoCanvas { cv ->
                cv.saveLayer(androidx.compose.ui.geometry.Rect(Offset.Zero, size),
                    androidx.compose.ui.graphics.Paint().apply { blendMode = androidx.compose.ui.graphics.BlendMode.DstOut })
                with(p) { draw(size) }
                cv.restore()
            }
        }
        if (cross) {
            val a = size.width * 0.13f; val w = size.width * 0.085f
            listOf(Offset(-a, -a) to Offset(a, a), Offset(-a, a) to Offset(a, -a)).forEach { (s, e) ->
                drawLine(Color.Black, c + s, c + e, w, androidx.compose.ui.graphics.StrokeCap.Round,
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear)
            }
        }
    }
}

/** V3: plain ✕, 2 round strokes */
@Composable
private fun CloseCross(modifier: Modifier, tint: Color) = Canvas(modifier) {
    val w = size.width * 0.16f
    drawLine(tint, Offset.Zero, Offset(size.width, size.height), w, androidx.compose.ui.graphics.StrokeCap.Round)
    drawLine(tint, Offset(0f, size.height), Offset(size.width, 0f), w, androidx.compose.ui.graphics.StrokeCap.Round)
}

/** V7: red wax blob (12 soft bumps) + embossed ★ (dark drop 1 px below, light star on top) */
@Composable
private fun WaxSeal(modifier: Modifier, tint: Color) {
    val p = painterResource(de.strix.marxversion.R.drawable.ic_star)
    Canvas(modifier) {
        val c = center; val r = size.minDimension / 2 * 0.94f
        val blob = Path().apply {
            for (i in 0..96) {
                val t = i / 96f * 2 * Math.PI
                val rr = r * (0.93f + 0.07f * kotlin.math.cos(12 * t).toFloat())
                val pt = Offset(c.x + rr * kotlin.math.cos(t).toFloat(), c.y + rr * kotlin.math.sin(t).toFloat())
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
            close()
        }
        drawPath(blob, tint)
        drawCircle(lerp(tint, Color.Black, 0.25f), r * 0.66f, c, style = Stroke(r * 0.05f))
        val s = size.width * 0.5f
        val tl = Offset(c.x - s / 2, c.y - s / 2 - s * (1.09f / 24f))
        translate(tl.x, tl.y + 1.2f) { with(p) { draw(androidx.compose.ui.geometry.Size(s, s), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(lerp(tint, Color.Black, 0.35f))) } }
        translate(tl.x, tl.y) { with(p) { draw(androidx.compose.ui.geometry.Size(s, s), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(lerp(tint, Color.White, 0.30f))) } }
    }
}

/** ★ th 1 star (streak hero · close) = `R.drawable.ic_star` · nvr redraw it w Path() (drift, 🦠 @1116) */
@Composable
fun StarImage(modifier: Modifier = Modifier) =
    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(de.strix.marxversion.R.drawable.ic_star), null, modifier)

/** red track on · paperDeep off */
@Composable
fun DsSwitch(checked: Boolean, onChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Switch(checked, onChange, modifier, enabled = enabled, colors = SwitchDefaults.colors(
        checkedTrackColor = Ds.red, checkedThumbColor = Ds.white, checkedBorderColor = Ds.red,
        uncheckedTrackColor = Ds.paperDeep, uncheckedThumbColor = Ds.outline, uncheckedBorderColor = Ds.outline,
    ))

/** "read more ▾" i th book · redTint pill */
@Composable
fun MoreChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, open: Boolean = false) =
    Box(modifier.minimumInteractiveComponentSize().height(28.dp).clip(Pill).background(Ds.redTint)
        .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
        Text(label + if (open) " ▴" else " ▾", color = Ds.red, style = MaterialTheme.typography.labelMedium)
    }

/** "💬 N" oth ppl's notes on a page ➡ comments sheet (desc 4 TalkBack) */
@Composable
fun CountChip(n: Int, onClick: () -> Unit, desc: String, modifier: Modifier = Modifier) = HeadChip("💬 $n", onClick, desc, modifier)

/** small paper pill i th reader's slim head (💬 N · 📖⬄📜 mode toggle) · desc 4 TalkBack */
@Composable
fun HeadChip(text: String, onClick: () -> Unit, desc: String, modifier: Modifier = Modifier) =
    Box(modifier.minimumInteractiveComponentSize().height(28.dp).clip(Pill).background(Ds.paperDeep)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc }
        .padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Ds.ink, style = MaterialTheme.typography.labelMedium)
    }
