package de.strix.marxversion.ui.ds

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 🔤 21 sizes ➡ 9 (design_system.md § 3) · weights 400 · 500 · 700 only · serif = Marx, sans = app + ppl
 * 🔑 a full sentence is nvr < 14sp · label-s 11 = floor (refs · timestamps · legal)
 * D-033 a: system serif now (bundled font = 🧪 later)
 */
private val base = Typography()
private fun TextStyle.set(size: Int, line: Int, w: FontWeight) = copy(fontSize = size.sp, lineHeight = line.sp, fontWeight = w)

val MarxTypography = Typography(
    displaySmall = base.displaySmall.set(28, 34, FontWeight.Bold),     // streak number · fx
    titleLarge = base.titleLarge.set(22, 28, FontWeight.Bold),         // screen title
    titleMedium = base.titleMedium.set(18, 24, FontWeight.Bold),       // panel + dialog title
    titleSmall = base.titleSmall.set(16, 22, FontWeight.Bold),         // section heading
    bodyLarge = base.bodyLarge.set(16, 24, FontWeight.Normal),         // main UI sentences
    bodyMedium = base.bodyMedium.set(14, 20, FontWeight.Normal),       // 2nd sentences · bubbles · callouts
    labelLarge = base.labelLarge.set(14, 20, FontWeight.Medium),       // btn txt
    labelMedium = base.labelMedium.set(12, 16, FontWeight.Medium),     // chips · action labels · tags · counters
    labelSmall = base.labelSmall.set(11, 16, FontWeight.Medium),       // floor
    headlineSmall = base.headlineSmall.set(24, 30, FontWeight.Bold).copy(fontFamily = FontFamily.Serif),  // book-title
)

/** 📖 book styles M3 has no slot 4 · `bookBody` × readScale at th call site */
object OmmType {
    val bookTitle = MarxTypography.headlineSmall
    /** section title i th book = red rubric (colour set by caller: Ds.red) */
    val bookHead = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp)
    val bookBody = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp)
    val quote = TextStyle(fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 30.sp)
    /** running head · folio — th only all-caps (caller uppercases) */
    val bookMeta = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.2.sp)
    /** Primary-L label (16 bold, D-030 gallery @1113) */
    val primaryL = MarxTypography.labelLarge.set(16, 20, FontWeight.Bold)
    val primaryS = MarxTypography.labelLarge.copy(fontWeight = FontWeight.Bold)
    /** streak number on th star etc. */
    val display = MarxTypography.displaySmall
}
