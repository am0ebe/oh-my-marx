package de.strix.marxversion.ui.ds

import androidx.compose.foundation.background
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
        style = OmmType.bookBody.let { it.copy(fontSize = it.fontSize * scale, lineHeight = it.lineHeight * scale) })

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

/** 🪝 Hooked = th app showing its own tricks · yellow post-it r12 body-m */
@Composable
fun HookPostIt(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(Radius.m)).background(Ds.hook).padding(Space.m)) {
        CompositionLocalProvider(LocalContentColor provides Ds.onHook, LocalTextStyle provides MaterialTheme.typography.bodyMedium) { content() }
    }
}
