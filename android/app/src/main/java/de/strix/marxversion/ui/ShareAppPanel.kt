package de.strix.marxversion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.MarxApp
import kotlinx.coroutines.launch

/** QR of [APP_LINK] (qrencode -l M, 29×29) · "1" = dark module · ⚠️ regenerate if APP_LINK changes */
private val QR_ROWS = listOf(
    "11111110100111110111101111111",
    "10000010100101001110001000001",
    "10111010011000001011101011101",
    "10111010100010110101001011101",
    "10111010010100101011101011101",
    "10000010001111011001001000001",
    "11111110101010101010101111111",
    "00000000100100011110100000000",
    "10110111010011010100001001011",
    "10000101001001110111111110001",
    "10101111100111001110010010110",
    "00110000111100001010111100001",
    "10011011101010110101000001100",
    "01100101000000101001001000111",
    "01100111111101011011110100111",
    "10001000010000101001111010010",
    "11011110110100111011110111010",
    "00100000001010101000010101110",
    "10100111001001111010100100100",
    "00010001101001100110000100100",
    "01101111000101101111111111100",
    "00000000111010011000100011111",
    "11111110110010110001101011010",
    "10000010110001110000100011000",
    "10111010001000000100111110101",
    "10111010100000000010100111010",
    "10111010111011000000000100101",
    "10000010000111101010101101010",
    "11111110101001010001101101010",
)

private class Target(val label: String, val url: (msg: String, link: String) -> String)

/** same set as th web page (bin/lib.sh share panel) */
private val TARGETS = listOf(
    Target("Fediverse") { m, l -> "https://s2f.kytta.dev/?text=" + Uri.encode("$m $l") },
    Target("Bluesky") { m, l -> "https://bsky.app/intent/compose?text=" + Uri.encode("$m $l") },
    Target("Telegram") { m, l -> "https://t.me/share/url?url=" + Uri.encode(l) + "&text=" + Uri.encode(m) },
    Target("Reddit") { m, l -> "https://www.reddit.com/submit?url=" + Uri.encode(l) + "&title=" + Uri.encode(m) },
    Target("WhatsApp") { m, l -> "https://wa.me/?text=" + Uri.encode("$m $l") },
    Target("Mail") { m, l -> "mailto:?subject=" + Uri.encode(APP_NAME) + "&body=" + Uri.encode("$m $l") },
)

private fun openTarget(ctx: Context, url: String) {
    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onSuccess { (ctx.applicationContext as? MarxApp)?.let { a -> a.scope.launch { a.prefs.shared() } } }
}

/**
 * 📤 app share panel (QA @1112 #16) = web-style: social targets + copy link + system sheet + QR.
 * Use from any "share th app" btn: `if (show) ShareAppPanel(msg) { show = false }`
 * ⚠️ call sites (Cards.kt · MineScreen.kt) still use shareApp() ➡ switch i fan-in (other lanes' files)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShareAppPanel(msg: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val sheetTitle = tr("App teilen", "Share app")
    MarxDialog(onDismiss = onDismiss, title = tr("App teilen", "Share app")) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            TARGETS.forEach { t ->
                OutlinedButton(onClick = { openTarget(ctx, t.url(msg, APP_LINK)) }, modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(t.label, fontSize = 13.sp)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            OutlinedButton(onClick = { copyText(ctx, APP_LINK) }) { Text(tr("📋 Link kopieren", "📋 Copy link"), fontSize = 13.sp) }
            OutlinedButton(onClick = { shareApp(ctx, msg, sheetTitle) }) { Text(tr("Mehr Apps …", "More apps …"), fontSize = 13.sp) }
        }
        Canvas(Modifier.padding(top = 14.dp).size(168.dp).align(Alignment.CenterHorizontally)) {
            val n = QR_ROWS.size
            val m = size.width / n
            drawRect(Color.White)
            QR_ROWS.forEachIndexed { y, r ->
                r.forEachIndexed { x, c ->
                    if (c == '1') drawRect(Color(0xFF1D1230), Offset(x * m, y * m), Size(m + 0.5f, m + 0.5f))
                }
            }
        }
        Text(tr("Mit der Handykamera scannen", "Scan with a phone camera"), fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
    }
}
