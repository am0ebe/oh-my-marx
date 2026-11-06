package de.strix.marxversion.data

import kotlinx.serialization.Serializable
import java.util.Locale

// ── languages ──
// DE + EN = hand-written i th code (tr("de", "en")) · th rest = lookup tables fr bin/translate.py
// (assets/content/i18n_xx.json, key = th EN string) · missing entry ➡ falls back 2 EN

enum class Lang(
    val code: String,
    val native: String,
    val locale: Locale,
    val open: String,       // quote marks: „…“ · “…” · «…» · 「…」
    val close: String,
    val rtl: Boolean = false,
) {
    DE("DE", "Deutsch", Locale.GERMAN, "„", "“"),
    EN("EN", "English", Locale.UK, "“", "”"),
    IT("IT", "Italiano", Locale.ITALIAN, "«", "»"),
    ES("ES", "Español", Locale.forLanguageTag("es-ES"), "«", "»"),
    PT("PT", "Português", Locale.forLanguageTag("pt-BR"), "“", "”"),
    ZH("中文", "中文", Locale.SIMPLIFIED_CHINESE, "“", "”"),
    AR("عربي", "العربية", Locale.forLanguageTag("ar"), "«", "»", rtl = true),
    // @1106: th big ones ➡ ~top 12 by speakers together w th above
    FR("FR", "Français", Locale.FRENCH, "« ", " »"),
    RU("RU", "Русский", Locale.forLanguageTag("ru"), "«", "»"),
    HI("हिंदी", "हिन्दी", Locale.forLanguageTag("hi"), "“", "”"),
    BN("বাংলা", "বাংলা", Locale.forLanguageTag("bn"), "“", "”"),
    UR("اردو", "اردو", Locale.forLanguageTag("ur"), "“", "”", rtl = true),
    ID("ID", "Bahasa Indonesia", Locale.forLanguageTag("id"), "“", "”");

    val file get() = "i18n_${name.lowercase()}.json"

    companion object {
        /** menu order (@1106): EN 1st, DE (Marx's original) 2nd, then by speakers · enum order stays (ordinal > 1 = LLM-translated) */
        val menu = listOf(EN, DE, ZH, HI, ES, FR, AR, BN, PT, RU, UR, ID, IT)

        /** 1st start: th fon's language if we hv it, else EN */
        fun system(): Lang {
            val l = Locale.getDefault().language
            return entries.firstOrNull { it.locale.language == l } ?: EN
        }
    }
    // LLM quotes sometimes carry their own marks ➡ strip them, else „"…“
    fun quote(s: String) = open + s.trim().trim('"', '\'', '„', '“', '”', '«', '»', '‚', '‘', '’') + close
}

/** 1 translation table (per lang) · fields mirror bin/translate.py export */
@Serializable
data class Tr(
    val ui: Map<String, String> = emptyMap(),           // EN string ➡ translation
    val q: Map<String, String> = emptyMap(),            // verse id ➡ quote
    val a: Map<String, String> = emptyMap(),            // "plan:dayIdx" ➡ Andacht
    val plan: Map<String, List<String>> = emptyMap(),   // plan id ➡ [title, desc]
    val chap: Map<String, String> = emptyMap(),         // kap ➡ title
)

/** translatable pair, 4 strings that live outside composables (enums, maps) · bin/translate.py greps `T("…", "…")` */
@Suppress("FunctionName")
fun T(de: String, en: String) = de to en

/** non-composable lookup (notification, share sheet) */
fun Tr?.t(lang: Lang, de: String, en: String): String = when (lang) {
    Lang.DE -> de
    Lang.EN -> en
    else -> this?.ui?.get(en) ?: en
}

/** String.format w/o locale digits (Arabic phones would otherwise print ٣ instead of 3 — breaks "8,12" refs) */
fun String.f(vararg args: Any?): String = String.format(Locale.ROOT, this, *args)
