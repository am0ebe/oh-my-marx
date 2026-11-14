package de.strix.marxversion.ui.ds

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 🎨 design tokens = th ONLY place w raw colours (design_system.md § 2 · § 4 · § 8 · D-029)
 * 3 materials (paper · ink · cloth-blue) + 2 signals (red = act · gold = earned) · contrast ✔ i th spec table
 * outside `ui/ds/` ➡ use these names, nvr `Color(0x…)` (DesignGuardTest counts it)
 */
object Ds {
    // 📄 paper = surfaces
    val paper = Color(0xFFF6EEDC)      // app bg
    val paperHi = Color(0xFFFFFBF2)    // panels · dialogs · sheets · fields
    val paperDeep = Color(0xFFEDE3CC)  // nav bar · oth ppl's bubbles · tracks · chips off
    val page = Color(0xFFF3E7CF)       // reader page
    // ✒️ ink = text
    val ink = Color(0xFF3B2A1A)        // 11.9 on paper
    val inkMuted = Color(0xFF6B5A48)   // 5.7 on paper · meta, hints
    val line = Color(0xFFDCD3C1)       // ink 14 % · hairlines (decor only)
    val outline = Color(0xFF8F8271)    // ink 55 % · field borders (3.3)
    // 🔴 red = act
    val red = Color(0xFFB3261E)
    val redDeep = Color(0xFF8C1D18)    // press edge · error txt
    val redTint = Color(0xFFEFDAC9)    // selected chip · nav indicator · "read more"
    // 📘 cloth = app voice, 2nd actions
    val blue = Color(0xFF1F2F4F)
    val blueTint = Color(0xFFE0DBCE)   // Tonal btn · own note / msg
    // 🟡 gold = kept / earned — fills only on light (1.6 on paper ➡ nvr txt)
    val gold = Color(0xFFE8B931)
    val goldDeep = Color(0xFF7A5C00)   // txt th means "earned / active"
    val goldTint = Color(0xFFF3E4BD)   // reward callout · break card
    // 🪝 Hooked post-its only
    val hook = Color(0xFFFFF1B8)
    val onHook = Color(0xFF3A2E00)
    val ok = Color(0xFF2E7D32)         // ✔ icons ≥ 18dp only (4.4)
    val dev = Color(0xFF1565C0)        // DEV build marks only
    val white = Color.White
    /** 4 highlighter pens (Mark.highlight index) @ 40 % = user content, not chrome */
    val highlights = listOf(Color(0x66E8B931), Color(0x66E57373), Color(0x6681C784), Color(0x6664B5F6))
}

/** 🌙 "Nachtschicht" — fixed now, wired later (D-032) · surface needs th hairline (1.1 vs bg) */
object DsDark {
    val bg = Color(0xFF15120E)
    val surface = Color(0xFF201B15)
    val surfaceVariant = Color(0xFF2E271F)
    val line = Color(0xFF332F29)
    val ink = Color(0xFFEDE3CC)
    val inkMuted = Color(0xFFB3A792)
    val primary = Color(0xFFC8372D)    // fill · white on it 5.2
    val red = Color(0xFFFF8A7A)        // txt
    val blue = Color(0xFFA9BEE8)       // txt
    val blueContainer = Color(0xFF1F2F4F)
    val onBlueContainer = Color(0xFFDCE6FF)
    val gold = Color(0xFFE8B931)       // may be txt i dark (10.2)
    val page = Color(0xFF1B1712)
    val pageInk = Color(0xFFD9CDB8)
    val hook = Color(0xFF3A2E00)
    val onHook = Color(0xFFFFF1B8)
    val outline = Color(0xFF767064)
}

/** 📏 grid 4 · kill 6 · 10 · 14 · 18 · 22 */
object Space {
    val xxs = 2.dp; val xs = 4.dp; val s = 8.dp; val m = 12.dp; val l = 16.dp; val xl = 24.dp; val xxl = 32.dp; val xxxl = 48.dp
    val gutter = l; val panelPad = l; val betwPanels = m; val betwSections = xl; val inGroup = s; val iconLabel = xs
    val readerStart = xl; val readerEnd = 20.dp     // spine side wider
    val tap = 48.dp                                   // min tap target, even if th visual is 32-36
}

/** radius: xs verse frame · m callouts/bubbles/fields · l panels/hero/quote card · xl dialogs/sheet top · btns + chips = full */
object Radius {
    val xs = 6.dp; val m = 12.dp; val l = 16.dp; val xl = 24.dp
}

/** 🎬 ms · easing: move FastOutSlowIn · enter decelerate · exit accelerate · physical = spring 0.6-0.8 */
object Motion {
    const val instant = 100; const val fast = 180; const val base = 260; const val slow = 400
    const val fxMin = 1200; const val fxMax = 2300
    const val pressDamping = 0.7f
}
