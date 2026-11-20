package de.strix.marxversion.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 🗣️ 5 voices (design_system.md § 7 · qa-D6 "mine ⬄ others ⬄ Marx at a glance"):
 * Marx = serif, bare on paper · me = blue ruler + blueTint · others = paperDeep bubble · app = Callout (Surfaces.kt) · Hooked = post-it
 * 🔑 my msgs = blue (was gold) ➡ same "me" as my margin notes · gold stays = earned
 */

/** Marx (Kapital txt): serif, no box, ink · × readScale by caller */
@Composable
fun MarxVoice(text: String, modifier: Modifier = Modifier, scale: Float = 1f) =
    Text(text, color = Ds.ink, modifier = modifier,
        style = OmmType.bookBody.let { it.copy(fontSize = it.fontSize * scale, lineHeight = it.lineHeight * scale,
            localeList = androidx.compose.ui.text.intl.LocaleList(de.strix.marxversion.ui.LocalLang.current.locale.toLanguageTag())) })

/** me (notes · my msgs): 3dp blue start ruler + blueTint, r12 end corners · lead = "✍" o null */
@Composable
fun MeBubble(modifier: Modifier = Modifier, lead: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Row(modifier.height(IntrinsicSize.Min)
        .clip(RoundedCornerShape(topEnd = Radius.m, bottomEnd = Radius.m)).background(Ds.blueTint)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(Ds.blue))
        lead?.let { Text(it, color = Ds.blue, modifier = Modifier.padding(start = Space.s, top = Space.s)) }
        Column(Modifier.padding(horizontal = Space.m, vertical = Space.s)) {
            CompositionLocalProvider(LocalContentColor provides Ds.blue, LocalTextStyle provides MaterialTheme.typography.bodyMedium) { content() }
        }
    }
}

/** others (comments · circle chat): bubble r12 paperDeep · name + time label-s on top */
@Composable
fun OtherBubble(name: String, time: String?, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(Radius.m)).background(Ds.paperDeep).padding(horizontal = Space.m, vertical = Space.s)) {
        Row {
            Text(name, color = Ds.ink, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            time?.let { Text("  $it", color = Ds.inkMuted, style = MaterialTheme.typography.labelSmall) }
        }
        CompositionLocalProvider(LocalContentColor provides Ds.ink, LocalTextStyle provides MaterialTheme.typography.bodyMedium) { content() }
    }
}

/**
 * ✍️ NoteCard (C5 · D-054 b · design § 14.4): margin note AND comment = 1 component
 * mine = paperHi card + 1dp blue 30 % hairline + 3dp blue ruler (start) · others = paperDeep, no ruler, no hairline
 * head: initial disc 24 (blue mine · inkMuted others) + "name · kind · time" label-s · ✎ = edit (mine, optional)
 * `tail` = 8dp notch at th top-start pointing up at th verse (margin notes only)
 */
@Composable
fun NoteCard(
    name: String, meta: String?, modifier: Modifier = Modifier, mine: Boolean = true, tail: Boolean = false,
    posted: String? = null, onEdit: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit,
) {
    val bg = if (mine) Ds.paperHi else Ds.paperDeep
    val edge = Ds.blue.copy(alpha = .3f)
    Column(modifier) {
        if (tail) androidx.compose.foundation.Canvas(Modifier.padding(start = Space.xl).width(16.dp).height(8.dp)) {
            val p = androidx.compose.ui.graphics.Path().apply { moveTo(0f, size.height); lineTo(size.width / 2, 0f); lineTo(size.width, size.height); close() }
            drawPath(p, bg)
            if (mine) drawPath(p, edge, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            drawLine(bg, androidx.compose.ui.geometry.Offset(1f, size.height), androidx.compose.ui.geometry.Offset(size.width - 1f, size.height), 2.dp.toPx())
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).clip(RoundedCornerShape(Radius.m)).background(bg)
            .then(if (mine) Modifier.border(1.dp, edge, RoundedCornerShape(Radius.m)) else Modifier)) {
            if (mine) Box(Modifier.width(3.dp).fillMaxHeight().background(Ds.blue))
            Row(Modifier.padding(horizontal = Space.m, vertical = Space.s)) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(if (mine) Ds.blue else Ds.inkMuted), contentAlignment = Alignment.Center) {
                    Text(name.trim().take(1).uppercase().ifEmpty { "?" }, color = Ds.white, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
                Column(Modifier.weight(1f).padding(start = Space.s)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(name + (meta?.let { " · $it" } ?: ""), color = Ds.inkMuted, style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.weight(1f))
                        onEdit?.let { Text("✎", color = Ds.blue, style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.clickable(onClick = it).padding(start = Space.s)) }
                    }
                    CompositionLocalProvider(LocalContentColor provides Ds.ink, LocalTextStyle provides MaterialTheme.typography.bodyMedium) { content() }
                    posted?.let { Text("↳ $it", color = Ds.blue, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = Space.xxs)) }
                }
            }
        }
    }
}

/** 🪝 Hooked = th app showing its own tricks · yellow post-it r12 body-m */
@Composable
fun HookPostIt(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(Radius.m)).background(Ds.hook).padding(Space.m)) {
        CompositionLocalProvider(LocalContentColor provides Ds.onHook, LocalTextStyle provides MaterialTheme.typography.bodyMedium) { content() }
    }
}
