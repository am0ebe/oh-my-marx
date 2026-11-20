package de.strix.marxversion.ui.ds

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import de.strix.marxversion.R

/**
 * 🔤 21 sizes ➡ 9 (design_system.md § 3) · weights 400 · 500 · 700 only · serif = Marx, sans = app + ppl
 * 🔑 a full sentence is nvr < 14sp · label-s 11 = floor (refs · timestamps · legal)
 * D-033 b ✔️ @1120 (D-058): Marx voice = bundled **Literata** (OFL, assets/licenses) · variable wght + opsz ➡ 1 file per style
 *   non-Latin glyphs (zh · ar · hi · bn · ur) fall back 2 th system font per glyph · book body = justified + hyphenated
 */
private val base = Typography()

/** 📖 Literata: roman cut at opsz 12 (17sp text) · italic at opsz 18 (quotes) · was 2 variable files + FontVariation til #218 */
// @1120 #218: 5 static instances, Latin + Greek + Cyrillic only (bin/font_subset.sh · same weights + opsz as th variable setup) · −748 KB
val BookSerif = FontFamily(
    Font(R.font.literata_regular, FontWeight.Normal, FontStyle.Normal), Font(R.font.literata_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.literata_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.literata_italic, FontWeight.Normal, FontStyle.Italic), Font(R.font.literata_bold_italic, FontWeight.Bold, FontStyle.Italic),
)
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
    headlineSmall = base.headlineSmall.set(24, 30, FontWeight.Bold).copy(fontFamily = BookSerif),  // book-title
)

/** 📖 book styles M3 has no slot 4 · `bookBody` × readScale at th call site */
object OmmType {
    val bookTitle = MarxTypography.headlineSmall
    /** section title i th book = red rubric (colour set by caller: Ds.red) */
    val bookHead = TextStyle(fontFamily = BookSerif, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp)
    /** 🖨️ D-058 (🦠 @1120 "like a printed book"): block-justified + auto hyphens (Paragraph line breaker = fewer rivers)
     *  ⚠️ hyphenation follows th txt's locale ➡ callers set `localeList` 2 th shown lang (VerseView · NoteBubble) */
    val bookBody = TextStyle(fontFamily = BookSerif, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp,
        textAlign = TextAlign.Justify, hyphens = Hyphens.Auto, lineBreak = LineBreak.Paragraph)
    val quote = TextStyle(fontFamily = BookSerif, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 30.sp)
    /** running head · folio — th only all-caps (caller uppercases) */
    val bookMeta = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.2.sp)
    /** Primary-L label (16 bold, D-030 gallery @1113) */
    val primaryL = MarxTypography.labelLarge.set(16, 20, FontWeight.Bold)
    val primaryS = MarxTypography.labelLarge.copy(fontWeight = FontWeight.Bold)
    /** streak number on th star etc. */
    val display = MarxTypography.displaySmall
}
