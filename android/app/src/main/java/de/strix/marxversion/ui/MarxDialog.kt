package de.strix.marxversion.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.strix.marxversion.ui.ds.CloseStar
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Radius
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.TextBtn

/** a dialog btn: label + what it does */
class DialogAction(val label: String, val onClick: () -> Unit)

/**
 * 🪟 th 1 dialog look o th app (design_system.md § 7 · D-007): paperHi · r24 · gold hairline (55 %) · pad 24 ·
 * title-m centred · ★ top-end (always th same spot) · 6dp + dimmed backdrop
 * actions = **stacked** (L0 @1114): `primary` = Primary-L full-width · `defer` = Text btn centred under it
 *   (only if deferring means sth, else ★ is enough) · old `actions` row = until each lane moves its call site
 */
@Composable
fun MarxDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    icon: (@Composable () -> Unit)? = null,
    primary: DialogAction? = null,
    defer: DialogAction? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) { MarxDialogPanel(onDismiss, title, icon, primary, defer, actions, content) }
}

/** th dialog's panel wo its window (MarxDialog · ds_gallery shot: own windows r not i a root capture) */
@Composable
fun MarxDialogPanel(
    onDismiss: () -> Unit,
    title: String? = null,
    icon: (@Composable () -> Unit)? = null,
    primary: DialogAction? = null,
    defer: DialogAction? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    run {
        Surface(
            shape = RoundedCornerShape(Radius.xl), color = Ds.paperHi, shadowElevation = 6.dp,
            border = BorderStroke(1.dp, Ds.gold.copy(alpha = 0.55f)), modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                Column(Modifier.padding(Space.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    icon?.let { Box(Modifier.padding(bottom = Space.s)) { it() } }
                    title?.let {
                        Text(it, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = Space.xxl, vertical = Space.xs))   // room 4 th ★
                    }
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(top = Space.s)) { content() }
                    if (primary != null || defer != null) {
                        Column(Modifier.fillMaxWidth().padding(top = Space.l), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                            primary?.let { Primary(it.label, it.onClick) }
                            defer?.let { TextBtn(it.label, it.onClick) }
                        }
                    }
                    actions?.let {
                        Row(Modifier.fillMaxWidth().padding(top = Space.l), horizontalArrangement = Arrangement.spacedBy(Space.s, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically, content = it)
                    }
                }
                CloseButton(onDismiss, Modifier.align(Alignment.TopEnd).padding(Space.m))
            }
        }
    }
}

/** ★ = close, evywhere ➡ thin wrapper over ds `CloseStar` (call sites untouched) */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Red, size: Dp = 36.dp) =
    CloseStar(onClick, modifier, tint, size, tr("Schließen", "Close"))
