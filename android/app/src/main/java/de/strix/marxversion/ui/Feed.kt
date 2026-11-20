package de.strix.marxversion.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.OmmType
import de.strix.marxversion.ui.ds.Space
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

// (ReadOnFeed = batch feed rm'd @1120 #200 ➡ ScrollBook below · git: b7659ec)

/**
 * 📜 th reader's scroll mode (qa_1120 🐛 #200: batch feed got stuck at new sections + couldn't scroll back up)
 * = 1 LazyColumn over th WHOLE book, 1 item per passage ➡ scrolls both ways, nothing 2 load · opens at `start`
 * [onTop] = id o th top visible passage (➡ 📖 page index + saved reading position) · scrolled past = read
 */
@Composable
fun ScrollBook(vm: AppVm, start: Int, speaker: Speaker, modifier: Modifier = Modifier, onTop: (String) -> Unit) {
    val verses = vm.content.verses
    val state = androidx.compose.foundation.lazy.rememberLazyListState(initialFirstVisibleItemIndex = start.coerceIn(0, (verses.size - 1).coerceAtLeast(0)))
    LaunchedEffect(state) {
        var prev = state.firstVisibleItemIndex
        snapshotFlow { state.firstVisibleItemIndex }.collect { top ->
            verses.getOrNull(top)?.let { onTop(it.id) }
            if (top > prev) vm.read(verses.subList(prev, top).map { it.id })   // scrolled past = read
            prev = top
        }
    }
    androidx.compose.foundation.lazy.LazyColumn(modifier, state = state,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = Space.readerStart, end = Space.readerEnd, top = Space.m, bottom = 40.dp)) {
        items(verses.size, key = { verses[it].id }) { i ->
            val v = verses[i]
            val prev = verses.getOrNull(i - 1)
            Column {
                if (v.kap != prev?.kap) vm.content.chapters.firstOrNull { it.kap == v.kap }?.let {
                    Text(chapterTitle(it), style = OmmType.bookTitle, color = Ds.ink,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                }
                if (v.kap == prev?.kap && v.page != null && v.page != prev.page) PageBreak(v.page)
                if (v.secTitle.isNotBlank() && v.sec != prev?.sec) SectionTitle(secTitle(v))
                VerseView(vm, v, speaker, showRef = false, dropCap = v.kap != prev?.kap,
                    sectionCap = v.secTitle.isNotBlank() && v.sec != prev?.sec)   // ¶ marks rm'd (🦠 #241)
                Gap(6)
                if (i == verses.lastIndex) Text(tr("🏁 Das war Band 1. Ganz. Respekt.", "🏁 That was volume 1. All of it. Respect."),
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
            }
        }
    }
}

/** 📄 page turn i th 📜 scroll: hairline + serif folio, muted (decor only, like th 📖 page's "— 49 —") */
@Composable
private fun PageBreak(page: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = Ds.line)
        Text(tr("— S. %d —", "— p. %d —").f(page), style = OmmType.bookMeta.copy(fontFamily = FontFamily.Serif), color = Ds.inkMuted,
            modifier = Modifier.padding(horizontal = 10.dp))
        HorizontalDivider(Modifier.weight(1f), color = Ds.line)
    }
}

