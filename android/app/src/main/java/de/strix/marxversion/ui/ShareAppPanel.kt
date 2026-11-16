package de.strix.marxversion.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.strix.marxversion.MarxApp
import de.strix.marxversion.R
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.Tonal
import kotlinx.coroutines.delay
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

/** icon = brand logo (simple-icons CC0, same files as th web page bin/icons/) tinted i its brand colour (qa_1116 L3 #5) */
private class Target(val label: String, val icon: Int, val color: Color, val url: (msg: String, link: String) -> String)

/** same set + order as th web page (bin/lib.sh share panel · ref ~/scaricati/share_app.png) */
private val TARGETS = listOf(
    Target("Fediverse", R.drawable.ic_mastodon, Ds.brandMastodon) { m, l -> "https://s2f.kytta.dev/?text=" + Uri.encode("$m $l") },
    Target("Bluesky", R.drawable.ic_bluesky, Ds.brandBluesky) { m, l -> "https://bsky.app/intent/compose?text=" + Uri.encode("$m $l") },
    Target("Telegram", R.drawable.ic_telegram, Ds.brandTelegram) { m, l -> "https://t.me/share/url?url=" + Uri.encode(l) + "&text=" + Uri.encode(m) },
    Target("Reddit", R.drawable.ic_reddit, Ds.brandReddit) { m, l -> "https://www.reddit.com/submit?url=" + Uri.encode(l) + "&title=" + Uri.encode(m) },
    Target("Mail", R.drawable.ic_mail, Ds.blue) { m, l -> "mailto:?subject=" + Uri.encode(APP_NAME) + "&body=" + Uri.encode("$m $l") },
    Target("WhatsApp", R.drawable.ic_whatsapp, Ds.brandWhatsApp) { m, l -> "https://wa.me/?text=" + Uri.encode("$m $l") },
)

private fun openTarget(ctx: Context, url: String) {
    runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onSuccess { (ctx.applicationContext as? MarxApp)?.let { a -> a.scope.launch { a.prefs.shared() } } }
}

/**
 * 📤 app share panel (QA @1112 #16) = web-style: social targets + copy link + system sheet + QR.
 * Use from any "share th app" btn: `if (show) ShareAppPanel(msg) { show = false }`
 * ⚠️ call sites (Cards.kt · MineScreen.kt) still use shareApp() ➡ switch i fan-in (other lanes' files)
 * look (qa_1116 L3 #5, 🦠 "bland ➡ more color, hover/click states", web ref): 3×2 tiles (brand logo i brand colour on a
 * 12 % tint · label) w ripple + press-shrink · Tonal "Copy link" (📳 + inline ✓ 1.2 s) + Tonal "More apps" (system sheet) · QR
 */
@Composable
fun ShareAppPanel(msg: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val haptics = rememberHaptics()
    val sheetTitle = tr("App teilen", "Share app")
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) { if (copied) { delay(1200); copied = false } }
    MarxDialog(onDismiss = onDismiss, title = sheetTitle) {
        TARGETS.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = Space.s), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
                row.forEach { t -> ShareTile(t, Modifier.weight(1f)) { haptics.confirm(); openTarget(ctx, t.url(msg, APP_LINK)) } }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = Space.xs), horizontalArrangement = Arrangement.spacedBy(Space.s)) {
            Tonal(if (copied) "✓ " + tr("Kopiert", "Copied") else tr("Link kopieren", "Copy link"),
                { copyText(ctx, APP_LINK); haptics.confirm(); copied = true }, Modifier.weight(1f))
            Tonal(tr("Mehr Apps …", "More apps …"), { shareApp(ctx, msg, sheetTitle) }, Modifier.weight(1f))
        }
        Canvas(Modifier.padding(top = Space.l).size(144.dp).align(Alignment.CenterHorizontally)) {
            val n = QR_ROWS.size
            val m = size.width / n
            drawRect(Color.White)
            QR_ROWS.forEachIndexed { y, r ->
                r.forEachIndexed { x, c ->
                    if (c == '1') drawRect(Ds.ink, Offset(x * m, y * m), Size(m + 0.5f, m + 0.5f))
                }
            }
        }
        Text(tr("Mit der Handykamera scannen", "Scan with a phone camera"), style = MaterialTheme.typography.labelMedium,
            color = Ds.inkMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = Space.xs))
    }
}

/** 1 share target tile: brand logo 26 on 12 % brand tint, label under · h 72 · ripple (brand) + shrinks 4 % while pressed */
@Composable
private fun ShareTile(t: Target, modifier: Modifier, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val k by animateFloatAsState(if (pressed) 0.96f else 1f, label = "tile")
    Column(modifier.height(72.dp).graphicsLayer { scaleX = k; scaleY = k }.clip(MaterialTheme.shapes.medium)
        .background(t.color.copy(alpha = if (pressed) 0.22f else 0.12f))
        .clickable(src, ripple(color = t.color), role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(painterResource(t.icon), null, Modifier.size(26.dp), tint = t.color)
        Text(t.label, style = MaterialTheme.typography.labelMedium, color = Ds.ink, maxLines = 1, modifier = Modifier.padding(top = Space.xs))
    }
}
