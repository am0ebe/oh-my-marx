package de.strix.marxversion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.chapterDone

private val ABSCHNITTE = listOf(
    T("Vorworte", "Prefaces"), T("Ware und Geld", "Commodities and Money"),
    T("Die Verwandlung von Geld in Kapital", "The Transformation of Money into Capital"),
    T("Die Produktion des absoluten Mehrwerts", "The Production of Absolute Surplus-Value"),
    T("Die Produktion des relativen Mehrwerts", "The Production of Relative Surplus-Value"),
    T("Die Produktion des absoluten und relativen Mehrwerts", "The Production of Absolute and Relative Surplus-Value"),
    T("Der Arbeitslohn", "Wages"), T("Der Akkumulationsprozeß des Kapitals", "The Process of Accumulation of Capital"),
)

@Composable
fun ReadScreen(vm: AppVm, openChapter: (String) -> Unit) {
    val readIds by vm.readIds.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val done = remember(readIds) { chapterDone(vm.content.verses, readIds) }
    val n = done.values.count { it }
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(tr("Das Kapital, Band 1", "Capital, Volume 1"), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Text(tr("%d von %d erledigt", "%d of %d done").f(n, done.size), fontSize = 13.sp)
            LinearProgressIndicator(progress = { n / done.size.toFloat() }, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp))
            HookNote(Hook.ENDOWED, s.testMode)
        }
        var lastAb = -1
        vm.content.chapters.forEach { c ->
            if (c.abschnitt != lastAb) {
                lastAb = c.abschnitt
                item { SectionTitle(ABSCHNITTE.getOrNull(c.abschnitt)?.let { tr(it) } ?: "") }
            }
            item {
                val key = if (c.kap.startsWith("V")) "V" else c.kap
                val label = when {
                    key == "V" -> tr(" · geschenkt 🎁", " · on the house 🎁")
                    key == "8" -> tr(" · schon gelebt (du arbeitest ja)", " · already lived (you do work, right?)")
                    else -> ""
                }
                Row(Modifier.fillMaxWidth().clickable { openChapter(c.kap) }.padding(vertical = 8.dp)) {
                    Text(if (done[key] == true) "✔️ " else "○ ", fontSize = 15.sp)
                    Column {
                        Text(chapterTitle(c), fontSize = 15.sp)
                        Text((c.page?.let { tr("S. %d", "p. %d").f(it) } ?: "") + label, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ChapterScreen(vm: AppVm, kap: String, focus: String?) {
    val verses = remember(kap) { vm.content.chapter(kap) }
    val chapter = vm.content.chapters.firstOrNull { it.kap == kap }
    val speaker = rememberSpeaker()
    val state = rememberLazyListState()
    // scroll 2 th verse we came fr ("Im Kontext") · +1 = th title item
    LaunchedEffect(focus) {
        val i = verses.indexOfFirst { it.id == focus }
        if (i >= 0) state.scrollToItem(i + 1)
    }
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 12.dp), state = state) {
        item { Text(chapter?.let { chapterTitle(it) } ?: kap, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp)) }
        items(verses, key = { it.id }) { v ->
            val prev = verses.getOrNull(verses.indexOf(v) - 1)
            if (v.secTitle.isNotBlank() && v.sec != prev?.sec) SectionTitle(v.secTitle)
            // composed = (about 2 be) on screen ➡ count as read
            LaunchedEffect(v.id) { vm.read(listOf(v.id)) }
            VerseView(vm, v, speaker)
            Gap(6)
        }
    }
}
