package de.strix.marxversion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.Full
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Tr
import de.strix.marxversion.data.t
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.DsDark
import de.strix.marxversion.ui.ds.MarxShapes
import de.strix.marxversion.ui.ds.MarxTypography
import de.strix.marxversion.ui.ds.OmmType

// colours: Parteibuch-rot · Dietz-MEW-blau · gold (Heiligenschein) · altes Papier ➡ raw values live i ui/ds/Tokens.kt (D-029)
//   these 4 names stay 4 th ~70 call sites · new code: `Ds.red` …

val Red = Ds.red
val Blue = Ds.blue
val Gold = Ds.gold
val Paper = Ds.paper

/** highlighter pens 4 Mark.highlight (index) */
val HIGHLIGHT_COLORS = Ds.highlights

/**
 * 🎨 light "Papier" = evy M3 role filled (design_system.md § 2) ➡ no M3 dflt lavender left (nav bar · cards · sheets)
 * red = act · blue = 2nd · gold = earned · flat: surfaceTint = paperHi (no tonal tint)
 */
private val scheme = lightColorScheme(
    primary = Ds.red, onPrimary = Ds.white, primaryContainer = Ds.redTint, onPrimaryContainer = Ds.redDeep,
    secondary = Ds.blue, onSecondary = Ds.white, secondaryContainer = Ds.blueTint, onSecondaryContainer = Ds.blue,
    tertiary = Ds.gold, onTertiary = Ds.ink, tertiaryContainer = Ds.goldTint, onTertiaryContainer = Ds.ink,
    background = Ds.paper, onBackground = Ds.ink,
    surface = Ds.paperHi, onSurface = Ds.ink, surfaceVariant = Ds.paperDeep, onSurfaceVariant = Ds.inkMuted,
    surfaceTint = Ds.paperHi, surfaceBright = Ds.paperHi, surfaceDim = Ds.paperDeep,
    surfaceContainerLowest = Ds.paperHi, surfaceContainerLow = Ds.paperHi, surfaceContainer = Ds.paperDeep,
    surfaceContainerHigh = Ds.paperHi, surfaceContainerHighest = Ds.paperDeep,
    inverseSurface = Ds.ink, inverseOnSurface = Ds.paper, inversePrimary = DsDark.red,
    outline = Ds.outline, outlineVariant = Ds.line,
    error = Ds.redDeep, onError = Ds.white, errorContainer = Ds.redTint, onErrorContainer = Ds.redDeep,
    scrim = Color.Black,
)

/** 🌙 "Nachtschicht" — defined, NOT wired (D-032: follow system = L6) */
@Suppress("unused")
val MarxDarkScheme = darkColorScheme(
    primary = DsDark.primary, onPrimary = Ds.white, primaryContainer = DsDark.surfaceVariant, onPrimaryContainer = DsDark.red,
    secondary = DsDark.blue, onSecondary = DsDark.blueContainer, secondaryContainer = DsDark.blueContainer, onSecondaryContainer = DsDark.onBlueContainer,
    tertiary = DsDark.gold, onTertiary = DsDark.bg, tertiaryContainer = DsDark.surfaceVariant, onTertiaryContainer = DsDark.gold,
    background = DsDark.bg, onBackground = DsDark.ink,
    surface = DsDark.surface, onSurface = DsDark.ink, surfaceVariant = DsDark.surfaceVariant, onSurfaceVariant = DsDark.inkMuted,
    surfaceTint = DsDark.surface, surfaceBright = DsDark.surfaceVariant, surfaceDim = DsDark.bg,
    surfaceContainerLowest = DsDark.bg, surfaceContainerLow = DsDark.surface, surfaceContainer = DsDark.surface,
    surfaceContainerHigh = DsDark.surfaceVariant, surfaceContainerHighest = DsDark.surfaceVariant,
    inverseSurface = DsDark.ink, inverseOnSurface = DsDark.bg, inversePrimary = Ds.red,
    outline = DsDark.outline, outlineVariant = DsDark.line,
    error = DsDark.red, onError = DsDark.bg, errorContainer = DsDark.surfaceVariant, onErrorContainer = DsDark.red,
    scrim = Color.Black,
)

/**
 * CompositionLocal = a value evy composable below can read wo passing it down by hand.
 * Here: th current language + its translation table ➡ tr("Heute", "Today") anywhere i th UI.
 */
val LocalLang = staticCompositionLocalOf { Lang.EN }

/** 🧪 DEV app (bin/deploy.sh) ➡ blue Marx disc + "DEV" i th top bar, same blue as its launcher icon (src/debug/res) */
val IS_DEV = BuildConfig.APPLICATION_ID.endsWith(".dev")
val DevBlue = Ds.dev
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
        MaterialTheme(colorScheme = scheme, typography = MarxTypography, shapes = MarxShapes, content = content)
    }
}

/** th quote style — serif + italic like a Kalenderspruch (OmmType.quote, size kept 4 th call sites) */
@Composable
fun QuoteText(text: String, size: Int = 22) = Text(
    text, style = OmmType.quote.copy(fontSize = size.sp, lineHeight = (size * 1.35).sp), color = Blue,
)
