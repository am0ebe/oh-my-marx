package de.strix.marxversion.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import kotlin.math.abs

private val ITEM_H = 44.dp
private const val VISIBLE = 5  // odd ➡ 1 centre row

/**
 * Scroll wheel (iOS / old-Android spinner style).
 * LazyColumn + snap fling · cyclic = "endless" list (count × 1000 items, start i th middle).
 * Fade + shrink by distance fr th centre is done i graphicsLayer ➡ runs per frame, no recomposition.
 */
@Composable
fun Wheel(count: Int, value: Int, label: (Int) -> String, onChange: (Int) -> Unit, modifier: Modifier = Modifier, cyclic: Boolean = true) {
    val pad = VISIBLE / 2
    val total = if (cyclic) count * 1000 else count + 2 * pad
    val first = if (cyclic) total / 2 - (total / 2) % count + value - pad else value  // index o th top row
    val state = rememberLazyListState(initialFirstVisibleItemIndex = first)
    val fling = rememberSnapFlingBehavior(state, SnapPosition.Center)

    // when th wheel comes 2 rest ➡ th row closest 2 th centre = th value
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.collect { moving ->
            if (moving) return@collect
            val info = state.layoutInfo
            val mid = (info.viewportStartOffset + info.viewportEndOffset) / 2
            val c = info.visibleItemsInfo.minByOrNull { abs(it.offset + it.size / 2 - mid) } ?: return@collect
            val v = if (cyclic) c.index % count else c.index - pad
            if (v in 0 until count) onChange(v)
        }
    }

    Box(modifier.height(ITEM_H * VISIBLE), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(ITEM_H).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)))
        LazyColumn(state = state, flingBehavior = fling, modifier = Modifier.fillMaxWidth()) {
            items(total) { i ->
                val v = if (cyclic) i % count else i - pad
                Text(
                    if (v in 0 until count) label(v) else "",
                    fontSize = 24.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().height(ITEM_H).wrapContentHeight().graphicsLayer {
                        val info = state.layoutInfo
                        val it = info.visibleItemsInfo.firstOrNull { it.index == i } ?: return@graphicsLayer
                        val mid = (info.viewportStartOffset + info.viewportEndOffset) / 2f
                        val d = (abs(it.offset + it.size / 2f - mid) / (it.size * (pad + 0.5f))).coerceIn(0f, 1f)
                        alpha = 1f - d * 0.85f
                        scaleX = 1f - d * 0.3f; scaleY = scaleX
                        rotationX = (it.offset + it.size / 2f - mid) / it.size * -18f  // lil 3D drum tilt
                    },
                )
            }
        }
    }
}

/** dialog w hour + minute wheels · OK ➡ onPick(h24, m) */
@Composable
fun TimeWheelDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onPick: (Int, Int) -> Unit) {
    var h by remember { mutableIntStateOf(hour) }
    var m by remember { mutableIntStateOf(minute) }
    MarxDialog(onDismiss, actions = { androidx.compose.material3.Button(onClick = { onPick(h, m) }) { Text("OK") } }) {
        // clocks read left➡right evywhere, also i Arabic
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            // always 24 h: 2 wheels 00-23 : 00-59 (strix @1103: no AM/PM, even if th fon is set 2 12 h)
            Row(Modifier.fillMaxWidth().padding(top = 28.dp), verticalAlignment = Alignment.CenterVertically) {
                Wheel(24, h, { "%02d".format(it) }, { h = it }, Modifier.weight(1f))
                Text(":", fontSize = 24.sp, modifier = Modifier.width(12.dp), textAlign = TextAlign.Center)
                Wheel(60, m, { "%02d".format(it) }, { m = it }, Modifier.weight(1f))
            }
        }
    }
}
