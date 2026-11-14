package de.strix.marxversion.ui.ds

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * 🏷️ chips = pill h 32 · label-m · 48 target (design_system.md § 5 kind 5)
 * Choice = toggle (persona · "also post 2 reading room") · Suggest = tap = go (Marxoscope topics · Hooked bench)
 * state nvr by colour alone ➡ selected Choice shows a ✓
 */
@Composable
fun ChoiceChip(label: String, selected: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Row(modifier.minimumInteractiveComponentSize().height(32.dp).clip(CircleShape)
        .background(if (selected) Ds.redTint else Ds.paperDeep)
        .toggleable(selected, enabled = enabled, role = Role.Checkbox, onValueChange = onChange)
        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (selected) Check(Ds.red)
        Text(label, color = if (selected) Ds.red else Ds.ink, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }

@Composable
fun SuggestChip(label: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Row(modifier.minimumInteractiveComponentSize().height(32.dp).clip(CircleShape).background(Ds.paperHi)
        .border(1.dp, Ds.outline, CircleShape)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Ds.ink, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }

/** ✓ drawn, not a font glyph (emoji fonts vary) */
@Composable
internal fun Check(c: Color) = Canvas(Modifier.padding(end = 6.dp).size(12.dp)) {
    val s = 2.dp.toPx()
    drawLine(c, Offset(size.width * .08f, size.height * .55f), Offset(size.width * .4f, size.height * .85f), s, StrokeCap.Round)
    drawLine(c, Offset(size.width * .4f, size.height * .85f), Offset(size.width * .95f, size.height * .18f), s, StrokeCap.Round)
}
