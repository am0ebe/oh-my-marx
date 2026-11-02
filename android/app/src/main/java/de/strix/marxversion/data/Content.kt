package de.strix.marxversion.data

import android.content.Context
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── th book itself: read-only, shipped i assets/content/*.json (built by pr/bible/bin/build.py) ──

/** 1 Absatz o Kapital = 1 "Vers". id is bible-style "8,9" = Kap 8, Absatz 9 · prefaces = "V1,3" */
@Serializable
data class Verse(
    val id: String,
    val kap: String,
    val sec: String = "",
    @SerialName("st") val secTitle: String = "",
    @SerialName("p") val page: Int? = null,      // MEW 23 page (= strix's paper copy)
    val de: String,
    val en: String = "",
    @SerialName("qd") val quoteDe: String = "",  // best short line (LLM-picked)
    @SerialName("qe") val quoteEn: String = "",
    @SerialName("j") val juice: Int = 0,         // 1-5 how gripping
    @SerialName("d") val difficulty: Int = 3,
    @SerialName("s") val standalone: Int = 3,
    @SerialName("t") val tags: List<String> = emptyList(),
    @SerialName("pol") val polemic: Boolean = false,  // Marx roasts a named person/group
    @SerialName("bib") val bible: Boolean = false,
    val star: Boolean = false,                       // ⭐ top ~1/6 o its section ➡ Vers-des-Tages pool
)

@Serializable
data class Note(val kap: String, val n: String, val de: String, val en: String = "")

@Serializable
data class Chapter(
    val kap: String,
    val de: String,
    val en: String,
    @SerialName("ab") val abschnitt: Int = 0,
    @SerialName("p") val page: Int? = null,
)

@Serializable
data class PlanDay(
    @SerialName("v") val verseIds: List<String>,
    @SerialName("ad") val andachtDe: String = "",
    @SerialName("ae") val andachtEn: String = "",
)

@Serializable
data class Plan(
    val id: String,
    val emoji: String,
    @SerialName("tde") val titleDe: String,
    @SerialName("ten") val titleEn: String,
    @SerialName("dde") val descDe: String,
    @SerialName("den") val descEn: String,
    val days: List<PlanDay>,
)

/** loads evythg once (~5 MB JSON) · lookups by id afterwards r O(1) */
class ContentRepo(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    lateinit var verses: List<Verse>; private set
    lateinit var chapters: List<Chapter>; private set
    lateinit var plans: List<Plan>; private set
    private lateinit var byId: Map<String, Verse>
    private lateinit var notes: Map<Pair<String, String>, Note>
    private var tables: Map<Lang, Tr> = emptyMap()

    var loaded = false; private set

    private inline fun <reified T> read(name: String): T =
        context.assets.open("content/$name").bufferedReader().use { json.decodeFromString<T>(it.readText()) }

    /** call off th main thread (see AppVm) · synchronized: app, worker + widget may load at th same time */
    @Synchronized
    fun load() {
        if (loaded) return
        verses = read("verses.json")
        chapters = read("chapters.json")
        plans = read("plans.json")
        notes = read<List<Note>>("notes.json").associateBy { it.kap to it.n }
        byId = verses.associateBy { it.id }
        // translation tables: missing file (not yet translated) = fine ➡ EN fallback
        tables = Lang.entries.mapNotNull { l -> runCatching { l to read<Tr>(l.file) }.getOrNull() }.toMap()
        loaded = true
    }

    fun verse(id: String): Verse? = byId[id]
    fun note(kap: String, n: String): Note? = notes[kap to n]
    fun chapter(kap: String): List<Verse> = verses.filter { it.kap == kap }
    fun plan(id: String): Plan? = plans.firstOrNull { it.id == id }
    fun tr(lang: Lang): Tr? = tables[lang]
}
