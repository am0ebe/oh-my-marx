package de.strix.marxversion.ui.ds

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
        Box(width.offset(y = sink).height(h).defaultMinSize(minWidth = 64.dp).background(bg, Pill).padding(horizontal = 24.dp),
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
        Text(label, color = labelColor, style = MaterialTheme.typography.labelMedium, maxLines = 1,
            modifier = Modifier.padding(top = if (style == ActStyle.Circle) 0.dp else Space.xxs))
    }
}

// ── singletons (1 look, fixed spot) ────────────────────────────────────────────────────────────────────────

/** ★ = close, evywhere (top-end o dialogs + sheets) · 36 visual, 48 target */
@Composable
fun CloseStar(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Ds.red, size: Dp = 36.dp, desc: String = "Close") {
    Surface(onClick = onClick, shape = CircleShape, color = tint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.30f)), modifier = modifier.size(size).semantics { contentDescription = desc }) {
        Box(contentAlignment = Alignment.Center) { Text("★", fontSize = (size.value * 0.48f).sp, color = tint, fontWeight = FontWeight.Bold) }
    }
}

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
fun CountChip(n: Int, onClick: () -> Unit, desc: String, modifier: Modifier = Modifier) =
    Box(modifier.minimumInteractiveComponentSize().height(28.dp).clip(Pill).background(Ds.paperDeep)
        .clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc }
        .padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
        Text("💬 $n", color = Ds.ink, style = MaterialTheme.typography.labelMedium)
    }
