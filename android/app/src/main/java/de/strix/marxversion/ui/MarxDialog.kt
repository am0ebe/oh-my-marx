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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * 🪟 th 1 dialog look o th app (@1107, strix: "windows look weird — frame colour, no frames, ☭ at odd spots"):
 * paper surface · 24 dp corners · hairline gold frame + soft shadow · dimmed backdrop · round ☭ close btn top-right
 * (always th same spot) · centred icon/title · optional actions row at th bottom (1 big primary btn = th main thing 2 do).
 * Like Material 3 / Duolingo / Insight Timer: 1 container style evywhere, close top-right, primary action bottom.
 */
@Composable
fun MarxDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    icon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp), color = Color(0xFFFFFBF2), shadowElevation = 16.dp,
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.55f)), modifier = Modifier.fillMaxWidth(),
        ) {
            Box {
                Column(Modifier.padding(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    icon?.let { Box(Modifier.padding(top = 6.dp, bottom = 8.dp)) { it() } }
                    title?.let {
                        Text(it, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp, vertical = 4.dp))   // room 4 th close btn
                    }
                    Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(top = 6.dp)) { content() }
                    actions?.let {
                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically, content = it)
                    }
                }
                CloseButton(onDismiss, Modifier.align(Alignment.TopEnd).padding(10.dp))
            }
        }
    }
}

/** ☭ = close, evywhere · a real btn: round, tinted, hairline ring, ripple on tap */
@Composable
fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = Red, size: Dp = 36.dp) {
    Surface(onClick = onClick, shape = CircleShape, color = tint.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.30f)), modifier = modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) { Text("☭", fontSize = (size.value * 0.48f).sp, color = tint, fontWeight = FontWeight.Bold) }
    }
}
