package de.strix.marxversion.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.FEED_BATCH
import de.strix.marxversion.logic.FEED_BREAK
import de.strix.marxversion.logic.chapterPercent
import de.strix.marxversion.logic.feedStart

/**
 * 📜 Weiterlesen (big round #4 · D-024): aft today's snippet is done, Home turns into an endless feed o th book —
 * insta-style, no "weiter" tap: near th bottom ➡ th nxt [FEED_BATCH] passages load (am0 Q3 ✔️ auto-scroll)
 * 🪝 progress (chapter % bar) · movement (passages keep coming) · curiosity ("gleich: »…«" teaser o th nxt one)
 * honest: evy [FEED_BREAK] a ☕ card says th Soll is long done · scrolled-past passages count as read (Read % · badges),
 * not as plan days (D-011) · lives at th very end o Home (Marxoskop stays reachable above it)
 */
@Composable
fun ReadOnFeed(vm: AppVm, after: List<String>, scroll: ScrollState, speaker: Speaker) {
    val verses = vm.content.verses
    val start = remember(after) { feedStart(verses, after) }
    if (start >= verses.size) return
    var n by rememberSaveable(start) { mutableIntStateOf(FEED_BATCH) }
    val left = verses.size - start
    LaunchedEffect(start) {
        // near th bottom (≤ ~1.5 screens o px left) ➡ load more · maxValue grows w th new items ➡ waits 4 th nxt approach
        snapshotFlow { scroll.maxValue - scroll.value }.collect { rest -> if (rest < 1800 && n < left) n = minOf(n + FEED_BATCH, left) }
    }
    val items = verses.subList(start, start + n)
    // everything but th newest batch was scrolled past ➡ read
    LaunchedEffect(n) { if (n > FEED_BATCH) vm.read(items.take(n - FEED_BATCH).map { it.id }) }
    val read by vm.readIds.collectAsStateWithLifecycle()
    val cur = items.last()
    val chapter = vm.content.chapters.firstOrNull { it.kap == cur.kap }
    val pct = remember(cur.kap, read) { chapterPercent(vm.content.chapter(cur.kap), read) }

    Column(Modifier.fillMaxWidth()) {
        Text(tr("📜 Weiterlesen", "📜 Keep reading"), fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Serif)
        Text(tr("Für die Motivierten: das Buch geht einfach weiter. Scrollen genügt.", "For the motivated: the book simply goes on. Just scroll."),
            fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        Text((chapter?.let { chapterTitle(it) } ?: cur.kap) + "  ·  " + tr("%d %% gelesen", "%d %% read").f(pct),
            fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red, modifier = Modifier.padding(top = 6.dp))
        LinearProgressIndicator(progress = { pct / 100f }, color = Red, trackColor = Red.copy(alpha = 0.15f),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp).height(4.dp).clip(RoundedCornerShape(50)))
        items.forEachIndexed { k, v ->
            val prev = if (k == 0) verses.getOrNull(start - 1) else items[k - 1]
            if (v.kap != prev?.kap) vm.content.chapters.firstOrNull { it.kap == v.kap }?.let {
                Text(chapterTitle(it), fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
            }
            if (v.secTitle.isNotBlank() && v.sec != prev?.sec) SectionTitle(secTitle(v))
            VerseView(vm, v, speaker, preview = true)
            Gap(6)
            if ((k + 1) % FEED_BREAK == 0) BreakCard(k + 1)
        }
        verses.getOrNull(start + n)?.let { next ->
            // 🪝 curiosity: th nxt passage's opening, faded ➡ keep scrolling 2 get it
            Text(tr("⬇ gleich: %s", "⬇ coming up: %s").f(quoted(opening(verseText(next)))), fontStyle = FontStyle.Italic,
                fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 10.dp))
        } ?: Text(tr("🏁 Das war Band 1. Ganz. Respekt.", "🏁 That was volume 1. All of it. Respect."),
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        Gap(40)
    }
}

/** ☕ honest Hooked: u could stop here, nothing is lost (D-014 tone: kind, no guilt) */
@Composable
private fun BreakCard(n: Int) {
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(RoundedCornerShape(12.dp)).background(Gold.copy(alpha = 0.18f)).padding(12.dp)) {
        Text(tr("☕ %d Absätze Mehrlesen", "☕ %d paragraphs of surplus reading").f(n), fontWeight = FontWeight.Bold)
        Text(tr("Dein Tagessoll ist längst erfüllt. Pause ist auch Klassenkampf — der Feed läuft nicht weg.",
            "Your daily quota is long met. A break is class struggle too — the feed isn't going anywhere."), fontSize = 13.sp)
    }
}
