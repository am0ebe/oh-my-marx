package de.strix.marxversion.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        // card bitmap off th main thread (StaticLayout + CARD_W×CARD_H px) ➡ swiping stays smooth
        val arch = archTitle(it)
        val bmp by produceState<ImageBitmap?>(null, it.key, lang, q, arch) {
            value = withContext(Dispatchers.Default) { cardBitmap(ctx, q, r, lang, it.feast, it.kind == CardItem.Kind.KRISE, arch).asImageBitmap() }
        }
        // neighbours shrink + fade a bit = "deck o cards" feel
        val off = abs((pager.currentPage - i) + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
        Column(Modifier.graphicsLayer { scaleX = lerp(1f, 0.9f, off); scaleY = scaleX; alpha = lerp(1f, 0.6f, off) },
            horizontalAlignment = Alignment.CenterHorizontally) {
            // today: th title lives i th card's arch (QA r3) ➡ no line above · oth cards keep their label
            if (arch == null) Text(label(it), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                color = if (it.kind == CardItem.Kind.KRISE) Red else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 6.dp))
            // 🙏 today's card itself = th Amen btn (@1106) · th pulsing "☭ Tap = Amen" pill on it rm'd @1113 (QA r4 shots: it sat
            //   on th ref line o th shorter card · r1: "amen 2 times") ➡ th 1 visible Amen = th big red btn under th actions
            val amenHere = it.kind == CardItem.Kind.TODAY && !doneToday
            Box(Modifier.fillMaxWidth().aspectRatio(CARD_RATIO).shadow(10.dp, RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2B1B4F), Color(0xFFB3261E), Color(0xFFF29E4C))))
                .then(if (amenHere) Modifier.clickable { onAmen(it.v) } else Modifier)) {
                bmp?.let { b -> Image(b, contentDescription = q, modifier = Modifier.fillMaxWidth().aspectRatio(CARD_RATIO)) }
            }
        }
    }

    // actions 4 th card i front
    val cur = item(pager.currentPage) ?: return
    val q = verseQuote(cur.v)
    val r = ref(cur.v)
    LaunchedEffect(cur.key, lang, persona) { speaker.prerender(q, lang, persona) }  // 🔊 tap = instant
    // 1 row o equal pills (icon + tiny label) · Teilen = 1 o them, not th loud 1 (feedback @1104) · Amen = same shape, red
    val haptic = LocalHapticFeedback.current
    val copied = tr("📋 Kopiert", "📋 Copied")
    val shareTitle = tr("Spruchkarte teilen", "Share verse card")
    val curArch = archTitle(cur)
    var sharePanel by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Top) {
        CardAction(R.drawable.ic_volume, tr("Vorlesen", "Listen"), if (speaker.speaking) Red else Blue) { speaker.say(q, lang, persona) }
        CardAction(R.drawable.ic_copy, tr("Kopieren", "Copy"), Blue) {
            copyText(ctx, "${lang.quote(q)}\n— Karl Marx, $r")
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT < 33) Toast.makeText(ctx, copied, Toast.LENGTH_SHORT).show()
        }
        CardAction(R.drawable.ic_book, tr("Im Buch", "In book"), Blue) { openChapter(cur.v.kap, cur.v.id) }
        // label says WHAT (QA @1112: "share wh?") · render + PNG off th main thread (was th lag)
        // share = exactly th card u see (same arch title, no sun)
        CardAction(R.drawable.ic_share, tr("Karte teilen", "Share card"), Blue) {
            shareCard(ctx, { cardBitmap(ctx, q, r, lang, cur.feast, cur.kind == CardItem.Kind.KRISE, curArch) },
                "${lang.quote(q)} — Karl Marx, $r\n$APP_NAME", shareTitle)
        }
        // 📤 share th app = same pill, right next 2 it (QA r3: streamline, was a loose txt btn aft Amen)
        CardAction(R.drawable.ic_send, tr("App teilen", "Share app"), Blue) { sharePanel = true }
    }
    if (sharePanel) ShareAppPanel(appShareMsg()) { sharePanel = false }
    // big + red + full width = can't miss it (was a 5th small pill)
    if (cur.kind == CardItem.Kind.TODAY && !doneToday)
        Button(onClick = { onAmen(cur.v) }, colors = ButtonDefaults.buttonColors(containerColor = Red),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 8.dp)) {
            Text(tr("☭ Amen — heute gelesen", "☭ Amen — read today"), fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    Text(
        tr("⬅ wischen: frühere Losungen · ⬇ ziehen: Orakel · 📳 schütteln: Krise", "⬅ swipe: earlier verses · ⬇ pull: oracle · 📳 shake: crisis"),
        fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
    )
}

/** 📤 1 "share th app" txt 4 evy entry (Today pill · Read page · About) · ShareAppPanel adds th link */
@Composable
fun appShareMsg() = tr("Ich lese Das Kapital jeden Tag ein Stück — mit Oh My Marx ☭", "I read Das Kapital a bit every day — with Oh My Marx ☭")

/** 🔘 1 card action: soft-tinted circle (tint @8 % + hairline ring) + label underneath · icon xor glyph · also used on th Read page */
@Composable
fun CardAction(icon: Int?, label: String, tint: Color, glyph: String? = null, onClick: () -> Unit) {
    // sweep r4: label tappable too (was: only th 46 dp circle ➡ taps on "In book" did nothing)
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick)) {
        Surface(onClick = onClick, shape = CircleShape, color = tint.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, tint.copy(alpha = 0.22f)), modifier = Modifier.size(46.dp)) {
            Box(contentAlignment = Alignment.Center) {
                if (icon != null) Icon(painterResource(icon), label, Modifier.size(20.dp), tint = tint)
                else Text(glyph.orEmpty(), color = tint, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(label, fontSize = 11.sp, color = tint.copy(alpha = 0.85f), maxLines = 1, modifier = Modifier.padding(top = 3.dp))
    }
}

/** 🌈 title set i th card's arch (above th saint): today only (feast ➡ its greeting) · null = no arch · QA r4: no "Marx" (he's on it) */
@Composable
fun archTitle(c: CardItem): String? =
    if (c.kind != CardItem.Kind.TODAY) null else c.feast?.let { tr(it.greet) } ?: tr("Zitat des Tages", "Quote of the day")

@Composable
private fun label(c: CardItem): String = when (c.kind) {
    CardItem.Kind.TODAY -> c.feast?.let { it.emoji + " " + tr(it.greet) } ?: tr("✨ Deine Marx-Losung für heute", "✨ Your Marx verse for today")
    CardItem.Kind.DAY -> "📜 " + if (c.back == 1) tr("gestern", "yesterday") else tr("vor %d Tagen", "%d days ago").f(c.back)
    CardItem.Kind.ORAKEL -> tr("🔮 Marxoskop sagt:", "🔮 Marxoscope says:")
    CardItem.Kind.KRISE -> tr("📉 Krise! Marx teilt aus:", "📉 Crisis! Marx lets rip:")
}
