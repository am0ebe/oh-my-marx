package de.strix.marxversion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.Full
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Tr
import de.strix.marxversion.data.t

// colours: Parteibuch-rot · Dietz-MEW-blau · gold (Heiligenschein) · altes Papier

val Red = Color(0xFFB3261E)
val Blue = Color(0xFF1F2F4F)
val Gold = Color(0xFFE8B931)
val Paper = Color(0xFFF6EEDC)

/** highlighter pens 4 Mark.highlight (index) */
val HIGHLIGHT_COLORS = listOf(Color(0x66E8B931), Color(0x66E57373), Color(0x6681C784), Color(0x6664B5F6))

private val scheme = lightColorScheme(
    primary = Red, onPrimary = Color.White,
    secondary = Blue, onSecondary = Color.White,
    tertiary = Gold,
    background = Paper, surface = Color(0xFFFFFBF2), surfaceVariant = Color(0xFFEDE3CC),
)

/**
 * CompositionLocal = a value evy composable below can read wo passing it down by hand.
 * Here: th current language + its translation table ➡ tr("Heute", "Today") anywhere i th UI.
 */
val LocalLang = staticCompositionLocalOf { Lang.EN }

/** 🧪 DEV app (bin/deploy.sh) ➡ blue Marx disc + "DEV" i th top bar, same blue as its launcher icon (src/debug/res) */
val IS_DEV = BuildConfig.APPLICATION_ID.endsWith(".dev")
val DevBlue = Color(0xFF1565C0)
val LocalTr = staticCompositionLocalOf<Tr?> { null }
/** whole book i th cur lang (IT/ES/PT/ZH/AR) · null = DE/EN o loading */
val LocalFull = staticCompositionLocalOf<Full?> { null }

/** DE/EN written inline · oth langs looked up by th EN string (bin/translate.py) */
@Composable
fun tr(de: String, en: String) = LocalTr.current.t(LocalLang.current, de, en)

@Composable
fun tr(p: Pair<String, String>) = tr(p.first, p.second)

/** „Zitat“ · “quote” · «citazione» — per-language quote marks */
@Composable
fun quoted(s: String) = LocalLang.current.quote(s)

@Composable
fun MarxTheme(lang: Lang, table: Tr?, full: Full? = null, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalLang provides lang,
        LocalTr provides table,
        LocalFull provides full,
        // Arabic: whole layout mirrors (Rows start right) — Compose does it, we just flip th switch
        LocalLayoutDirection provides if (lang.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** th quote style — serif + italic like a Kalenderspruch */
@Composable
fun QuoteText(text: String, size: Int = 22) = Text(
    text, fontFamily = FontFamily.Serif, fontStyle = FontStyle.Italic,
    fontSize = size.sp, lineHeight = (size * 1.35).sp, color = Blue,
)
