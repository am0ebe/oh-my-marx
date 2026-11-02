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
    AR("عربي", "العربية", Locale.forLanguageTag("ar"), "«", "»", rtl = true);

    val file get() = "i18n_${name.lowercase()}.json"
    fun quote(s: String) = open + s + close
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
