package de.strix.marxversion.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🧩 surfaces (design_system.md § 7) · depth = flat: paperHi + 1dp line, 0 shadow · quote card = th only lifted thing
 * nesting: Panel > Callout ✔ · Callout > Callout ❌ · Panel > Panel ❌ · lists i a Panel = rows 56 + line dividers
 */

/** M3 shape slots ➡ th 5 radii (xs 6 · m 12 · l 16 · xl 24 · full) · Theme.kt installs it */
val MarxShapes = Shapes(
    extraSmall = RoundedCornerShape(Radius.xs), small = RoundedCornerShape(Radius.m), medium = RoundedCornerShape(Radius.m),
    large = RoundedCornerShape(Radius.l), extraLarge = RoundedCornerShape(Radius.xl),
)

/** plan card · settings group · circle card */
@Composable
fun Panel(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(Radius.l)
    Column(modifier.fillMaxWidth().clip(shape).background(Ds.paperHi).border(1.dp, Ds.line, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .padding(Space.panelPad), content = content)
}

/**
 * plans: Panel + 4dp start stripe + 44 emoji disc (accent 14 %) · title = ink (accent nvr carries txt: 3.0-4.3 on white)
 * active = 2dp gold ring · `head` = title + Tag row next 2 th disc
 */
@Composable
fun AccentPanel(
    accent: Color, emoji: String, modifier: Modifier = Modifier, active: Boolean = false, onClick: (() -> Unit)? = null,
    head: @Composable ColumnScope.() -> Unit, content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(Radius.l)
    Row(modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(shape).background(Ds.paperHi)
        .border(if (active) 2.dp else 1.dp, if (active) Ds.gold else Ds.line, shape)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Box(Modifier.width(4.dp).fillMaxHeight().background(accent))
        Column(Modifier.weight(1f).padding(Space.panelPad)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(accent.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 20.sp)
                }
                Column(Modifier.padding(start = Space.m).weight(1f), content = head)
            }
            content?.let { Column(Modifier.padding(top = Space.m), content = it) }
        }
    }
}

/** callout tones: gold = reward / news · blue = app talks / new txt · accent = Andacht · hook = Hooked post-it */
class Tone(val bg: Color, val fg: Color) {
    companion object {
        val Gold = Tone(Ds.goldTint, Ds.ink)
        val Blue = Tone(Ds.blueTint, Ds.blue)
        val Hook = Tone(Ds.hook, Ds.onHook)
        fun accent(c: Color) = Tone(c.copy(alpha = .14f), Ds.ink)
    }
}

/** tint fill · r12 · pad 12 · lead emoji + title-s + body-m · no border (app voice) */
@Composable
fun Callout(
    tone: Tone, modifier: Modifier = Modifier, lead: String? = null, title: String? = null, onClick: (() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth().clip(RoundedCornerShape(Radius.m)).background(tone.bg)
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(Space.m)) {
        lead?.let { Text(it, fontSize = 20.sp, modifier = Modifier.padding(end = Space.s)) }
        Column(Modifier.weight(1f)) {
            title?.let { Text(it, color = tone.fg, style = MaterialTheme.typography.titleSmall) }
            content?.let { androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides tone.fg,
                androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.bodyMedium) { it() } }
        }
    }
}

/** "Day 3" · "active" · "new" · pill h 20 · label-s · accent 14 % + ink · gold = earned (gold fill + ink) */
@Composable
fun Tag(label: String, modifier: Modifier = Modifier, accent: Color = Ds.blue, gold: Boolean = false) =
    Box(modifier.height(20.dp).clip(CircleShape).background(if (gold) Ds.gold else accent.copy(alpha = .14f))
        .padding(horizontal = Space.s), contentAlignment = Alignment.Center) {
        Text(label, color = Ds.ink, style = MaterialTheme.typography.labelSmall, maxLines = 1)
    }
