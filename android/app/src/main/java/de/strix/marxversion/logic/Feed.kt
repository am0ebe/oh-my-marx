package de.strix.marxversion.logic

import de.strix.marxversion.data.Verse

/**
 * 📜 "Weiterlesen" feed (big round #4, am0 Q3: auto-scroll like insta) — pure part, UI ➡ ui/Feed.kt
 * after today's plan snippet is done: th book goes on in BOOK order right aft th last passage u just read
 * (not th plan's nxt day ➡ plans stay continuous, D-011) · endless = til th last Absatz o Kapital
 */
const val FEED_BATCH = 3        // passages per load (insta: a few, then more as u near th bottom)
const val FEED_BREAK = 10       // evy N passages a ☕ break card (honest Hooked: notwendige Arbeitszeit is long done)

/** index (i verses) o th 1st feed passage = right aft th furthest-on o `after` · none known ➡ verses.size (no feed) */
fun feedStart(verses: List<Verse>, after: List<String>): Int {
    val at = after.mapNotNull { id -> verses.indexOfFirst { it.id == id }.takeIf { it >= 0 } }.maxOrNull() ?: return verses.size
    return at + 1
}

/** share o a chapter read, 0..100 (feed header progress) */
fun chapterPercent(chapter: List<Verse>, read: Set<String>): Int =
    if (chapter.isEmpty()) 0 else chapter.count { it.id in read } * 100 / chapter.size
