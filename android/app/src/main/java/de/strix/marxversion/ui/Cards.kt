package de.strix.marxversion.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import de.strix.marxversion.R
import de.strix.marxversion.data.Verse
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.Feast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import kotlin.math.abs

// ── 🃏 Home = th cards. Quote cards r 1st-class: open th app ➡ they're right there, swipe thru them ──

/** 1 card i th Home carousel · TODAY = th Losung · DAY = an earlier day's Losung · ORAKEL / KRISE = pulled / shaken in */
data class CardItem(val v: Verse, val kind: Kind, val back: Int = 0, val feast: Feast? = null, val n: Long = System.nanoTime()) {
    enum class Kind { TODAY, DAY, ORAKEL, KRISE }
    // n: th same verse can be pulled twice ➡ pager keys must still differ (dup key = crash)
    val key get() = if (kind == Kind.ORAKEL || kind == Kind.KRISE) "$kind:${v.id}:$n" else "$kind:${v.id}:$back"
}

private const val HISTORY = 60  // swipe back 2 months o Losungen

/**
 * Pager: [extras (oracle / crisis, newest 1st)] + today + yesterday … 60 days ago.
 * Under th card: 🔊 · 📋 · 📖 · big Teilen · Amen (today only, until counted).
 * jump changes ➡ back 2 th 1st card (home btn, notif tap, new extra card).
 */
@Composable
fun CardCarousel(
    vm: AppVm, extras: List<CardItem>, today: CardItem?, doneToday: Boolean, speaker: Speaker, persona: Persona,
    jump: Int, openChapter: (String, String) -> Unit, onAmen: (Verse) -> Unit,
) {
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val count = extras.size + (if (today != null) 1 else 0) + HISTORY
    val pager = rememberPagerState { count }
    fun item(i: Int): CardItem? = when {
        i < extras.size -> extras[i]
        today != null && i == extras.size -> today
        else -> {
            val back = i - extras.size + (if (today != null) 0 else 1)
            vm.losungOn(LocalDate.now().minusDays(back.toLong()))?.let { CardItem(it, CardItem.Kind.DAY, back, Feast.on(LocalDate.now().minusDays(back.toLong()))) }
        }
    }
    LaunchedEffect(jump, extras.firstOrNull()?.key) { pager.animateScrollToPage(0) }

    HorizontalPager(
        pager, contentPadding = PaddingValues(horizontal = 28.dp), pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth(), key = { item(it)?.key ?: "x$it" },
    ) { i ->
        val it = item(i) ?: return@HorizontalPager
        val q = verseQuote(it.v)
        val r = ref(it.v)
        // card bitmap off th main thread (StaticLayout + 1080×1350 px) ➡ swiping stays smooth
        val bmp by produceState<ImageBitmap?>(null, it.key, lang, q) {
            value = withContext(Dispatchers.Default) { cardBitmap(ctx, q, r, lang, it.feast, it.kind == CardItem.Kind.KRISE).asImageBitmap() }
        }
        // neighbours shrink + fade a bit = "deck o cards" feel
        val off = abs((pager.currentPage - i) + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
        Column(Modifier.graphicsLayer { scaleX = lerp(1f, 0.9f, off); scaleY = scaleX; alpha = lerp(1f, 0.6f, off) },
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label(it), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                color = if (it.kind == CardItem.Kind.KRISE) Red else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 6.dp))
            Box(Modifier.fillMaxWidth().aspectRatio(1080f / 1350f).shadow(10.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2B1B4F), Color(0xFFB3261E), Color(0xFFF29E4C))))) {
                bmp?.let { b -> Image(b, contentDescription = q, modifier = Modifier.fillMaxWidth().aspectRatio(1080f / 1350f)) }
            }
        }
    }

    // actions 4 th card i front
    val cur = item(pager.currentPage) ?: return
    val q = verseQuote(cur.v)
    val r = ref(cur.v)
    LaunchedEffect(cur.key, lang, persona) { speaker.prerender(q, lang, persona) }  // 🔊 tap = instant
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = { speaker.say(q, lang, persona) }) {
            Icon(painterResource(R.drawable.ic_volume), tr("Vorlesen", "Listen"), Modifier.size(22.dp), tint = if (speaker.speaking) Red else Blue)
        }
        CopyIcon("${lang.quote(q)}\n— Karl Marx, $r", Blue)
        TextButton(onClick = { openChapter(cur.v.kap, cur.v.id) }) { Text("📖") }
        val shareTitle = tr("Spruchkarte teilen", "Share verse card")
        Button(onClick = {
            val bmp = cardBitmap(ctx, q, r, lang, cur.feast, cur.kind == CardItem.Kind.KRISE)
            shareCard(ctx, bmp, "${lang.quote(q)} — Karl Marx, $r\n$APP_NAME", shareTitle)
        }) {
            Icon(painterResource(R.drawable.ic_share), null, Modifier.size(18.dp)); Text(" " + tr("Teilen", "Share"))
        }
        if (cur.kind == CardItem.Kind.TODAY && !doneToday)
            OutlinedButton(onClick = { onAmen(cur.v) }, colors = ButtonDefaults.outlinedButtonColors(contentColor = Red)) { Text(tr("Amen ☭", "Amen ☭")) }
    }
    Text(
        tr("⬅ wischen: frühere Losungen · ⬇ ziehen: Orakel · 📳 schütteln: Krise", "⬅ swipe: earlier verses · ⬇ pull: oracle · 📳 shake: crisis"),
        fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun label(c: CardItem): String = when (c.kind) {
    CardItem.Kind.TODAY -> c.feast?.let { it.emoji + " " + tr(it.greet) } ?: tr("✨ Deine Marx-Losung für heute", "✨ Your Marx verse for today")
    CardItem.Kind.DAY -> "📜 " + if (c.back == 1) tr("gestern", "yesterday") else tr("vor %d Tagen", "%d days ago").f(c.back)
    CardItem.Kind.ORAKEL -> tr("🔮 Marxoskop sagt:", "🔮 Marxoscope says:")
    CardItem.Kind.KRISE -> tr("📉 Krise! Marx teilt aus:", "📉 Crisis! Marx lets rip:")
}
