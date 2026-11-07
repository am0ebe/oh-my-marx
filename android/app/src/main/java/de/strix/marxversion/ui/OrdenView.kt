package de.strix.marxversion.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.Orden
import de.strix.marxversion.logic.OrdenGroup

/** 🎖️ 1 medal: red/gold ribbon on top + gold disc w th emoji · locked = grey (secret + locked = "?") */
@Composable
fun Medal(o: Orden, have: Boolean, size: Dp = 56.dp) {
    val grey = Color(0xFFB9B2A6)
    Box(Modifier.size(size, size * 1.25f), contentAlignment = Alignment.BottomCenter) {
        // ribbon: 2 tails (red · gold) meeting under th disc
        Canvas(Modifier.size(size * 0.7f, size * 0.55f).align(Alignment.TopCenter)) {
            val w = this.size.width; val h = this.size.height
            val l = Path().apply { moveTo(0f, 0f); lineTo(w * 0.5f, 0f); lineTo(w * 0.62f, h); lineTo(w * 0.18f, h); close() }
            val r = Path().apply { moveTo(w * 0.5f, 0f); lineTo(w, 0f); lineTo(w * 0.82f, h); lineTo(w * 0.38f, h); close() }
            drawPath(l, if (have) Red else grey)
            drawPath(r, if (have) Gold else grey.copy(alpha = 0.7f))
            drawLine(Color.White.copy(alpha = 0.5f), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h * 0.9f), strokeWidth = 2f)
        }
        Box(
            Modifier.size(size).clip(CircleShape).background(if (have) Gold else Color(0xFFE6E0D4))
                .border(3.dp, if (have) Color(0xFFB8860B) else grey, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (have || !o.hidden) Text(o.emoji, fontSize = (size.value * 0.45f).sp, modifier = Modifier.alpha(if (have) 1f else 0.3f))
            else Text("?", fontSize = (size.value * 0.45f).sp, fontWeight = FontWeight.Bold, color = grey)
        }
    }
}

/** Mein Kapital ➡ 🎖️ Orden: per group, 3 per row · tap = what it's 4 */
@Composable
fun OrdenSection(earned: Set<Orden>, hooked: Boolean) {
    var open by remember { mutableStateOf<Orden?>(null) }
    Text(tr("🎖️ Orden  %d / %d", "🎖️ Medals  %d / %d").f(earned.size, Orden.entries.size), fontWeight = FontWeight.Bold, fontSize = 17.sp,
        modifier = Modifier.padding(top = 16.dp))
    OrdenGroup.entries.forEach { g ->
        Text(tr(g.label), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
        Orden.entries.filter { it.group == g }.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { o ->
                    val have = o in earned
                    Column(Modifier.width(100.dp).clickable { open = o }.padding(vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Medal(o, have)
                        Text(if (have || !o.hidden) tr(o.title) else "???", fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 13.sp,
                            fontWeight = if (have) FontWeight.Bold else FontWeight.Normal,
                            color = if (have) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 2.dp))
                    }
                }
                repeat(3 - row.size) { Box(Modifier.width(100.dp)) }
            }
        }
    }
    HookNote(Hook.ORDEN, hooked)
    open?.let { o ->
        val have = o in earned
        AlertDialog(
            onDismissRequest = { open = null },
            confirmButton = { TextButton(onClick = { open = null }) { Text("☭") } },
            icon = {
                Box(Modifier.clip(RoundedCornerShape(16.dp)).background(Brush.verticalGradient(listOf(Color(0xFF2B1B4F), Color(0xFF7A1A14))))
                    .border(2.dp, if (have) Gold else Color(0xFFB9B2A6), RoundedCornerShape(16.dp)).padding(12.dp)) { Medal(o, have, 72.dp) }
            },
            title = { Text(if (have || !o.hidden) tr(o.title) else "???", textAlign = TextAlign.Center) },
            text = {
                Text(when {
                    have -> tr(o.how)
                    o.hidden -> tr("Geheimer Orden. Lies weiter, dann findest du ihn.", "Secret medal. Keep reading and you'll find it.")
                    else -> tr("Noch nicht verdient: ", "Not earned yet: ") + tr(o.how)
                }, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            },
        )
    }
}

/** 🎉 "Neuer Orden!" — pops once per medal (MainActivity) · several at once (1st start aft update) ➡ all i 1 dialog */
@Composable
fun NewOrdenDialog(new: List<Orden>, onDone: () -> Unit) {
    Confetti {}   // 1st = behind th dialog (removes itself aft 1.8 s)
    AlertDialog(
        onDismissRequest = onDone,
        confirmButton = { TextButton(onClick = onDone) { Text(tr("Für die Sache! ☭", "For the cause! ☭")) } },
        title = { Text(if (new.size == 1) tr("🎖️ Neuer Orden!", "🎖️ New medal!") else tr("🎖️ %d neue Orden!", "🎖️ %d new medals!").f(new.size)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // medals on a dark "velvet cushion" (gold on th white dialog looked washed out @1107)
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(Color(0xFF2B1B4F), Color(0xFF7A1A14))))
                        .border(2.dp, Gold, RoundedCornerShape(16.dp)).padding(horizontal = 18.dp, vertical = 12.dp)) {
                    new.take(6).chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { row.forEach { Medal(it, true, 52.dp) } }
                    }
                }
                val names = new.take(6).map { tr(it.title) }   // tr = @Composable ➡ not inside joinToString's lambda
                Text(names.joinToString(" · ") + if (new.size > 6) " …" else "",
                    textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                if (new.size == 1) Text(tr(new[0].how), textAlign = TextAlign.Center, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Text(tr("Alle Orden: Mein Kapital", "All medals: My Capital"), fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
            }
        },
    )
}
