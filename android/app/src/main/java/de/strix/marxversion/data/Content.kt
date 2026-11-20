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
    @SerialName("ste") val secTitleEn: String = "",   // EN heading (marxists.org via build.py en_sec_titles) · "" ➡ DE shown
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
    @SerialName("xd") val bridgeDe: String = "",   // 💡 "Und heute?" = NEW txt linking th passage 2 today (data/plans_extra.json, eg Marx ⬄ KI)
    @SerialName("xe") val bridgeEn: String = "",
    @SerialName("qd") val teaseDe: String = "",    // ❓ C7 · D-048 c: 1 teasing Q 4 TOMORROW's Stempeluhr ("Morgen: …?") · opens a loop, nvr a cut quote
    @SerialName("qe") val teaseEn: String = "",
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
    // 🔢 plan versioning (qa_1117 r4 · D-042): days r addressed by index (DayLog.planDay) ➡ a re-cut plan would shift ppl
    //   mid-plan · ver = current cut · old = earlier cuts (bin/build.py ⬄ data/plan_versions.json, auto-bumped on any change)
    val ver: Int = 1,
    val old: Map<String, List<PlanDay>> = emptyMap(),
    // 🔒 progressive unlock (qa_1117 Q12): null = open · see logic/PlanUnlock.kt
    val lock: PlanLock? = null,
) {
    /** th cut u started on (Prefs.planVer) · null = started b4 versioning existed = v1 · unknown ➡ current */
    fun daysFor(v: Int?): List<PlanDay> = (v ?: 1).let { if (it == ver) days else old[it.toString()] ?: days }
}

/** 🔒 wh opens a plan (any 1 o them) · feast = gift on that feast (stays open aftr) · lucky = 🎁 random draw on an Amen day */
@Serializable
data class PlanLock(
    val plans: Int = 0,          // ≥ N plans finished
    val orden: Int = 0,          // ≥ N badges
    val streak: Int = 0,         // streak ≥ N
    val feast: String? = null,   // logic/Feast name, eg WEIHNACHTEN
    val lucky: Int = 0,          // aftr ≥ N reading days: 1-in-5 chance per Amen day
)

/** th whole book i 1 o th LLM langs (bin/translate_full.py --export ➡ full_xx.json, ~2-3 MB each)
 *  v = verse id ➡ paragraph · fn = "kap:n" ➡ footnote · missing key ➡ caller falls back 2 EN */
@Serializable
data class Full(val v: Map<String, String> = emptyMap(), val fn: Map<String, String> = emptyMap())

/** loads evythg once (~5 MB JSON) · lookups by id afterwards r O(1) */
class ContentRepo(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    lateinit var verses: List<Verse>; private set
    lateinit var chapters: List<Chapter>; private set
    lateinit var plans: List<Plan>; private set
    private lateinit var byId: Map<String, Verse>
    private val tables = java.util.concurrent.ConcurrentHashMap<Lang, java.util.Optional<Tr>>()

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
        byId = verses.associateBy { it.id }
        // ⚡ qa_1116 L6 (startup 2-3 s): was 11 i18n tables (5.3 MB) + footnotes (1.1 MB) parsed here = 62 % o th 10.4 MB
        //   ➡ now lazy: tr(lang) parses only th lang u use (DE/EN: none) · footnotes on 1st use / warm() aftr th 1st frame
        loaded = true
    }

    /** footnotes (1.1 MB) · lazy = synchronized ➡ worker + UI safe */
    private val notesMap by lazy { read<List<Note>>("notes.json").associateBy { it.kap to it.n } }

    /** parse wh th 1st screen doesn't need, off th main thread, aftr it's up (AppVm) ➡ 1st footnote tap = no hitch */
    fun warm() { notesMap.size }

    fun verse(id: String): Verse? = byId[id]
    fun note(kap: String, n: String): Note? = notesMap[kap to n]
    fun chapter(kap: String): List<Verse> = verses.filter { it.kap == kap }
    fun plan(id: String): Plan? = plans.firstOrNull { it.id == id }
    /** th lang's UI + quotes table, parsed on 1st ask + kept · missing file (DE/EN, not yet translated) = null ➡ EN fallback
     *  AppVm asks on IO 1st (start + lang change) ➡ th composition's call = a cache hit, nvr a parse on th main thread */
    fun tr(lang: Lang): Tr? = tables.getOrPut(lang) { java.util.Optional.ofNullable(runCatching { read<Tr>(lang.file) }.getOrNull()) }.orElse(null)

    // full text: only th cur lang i memory (5 × ~2.5 MB parsed = too much 4 small fons) · DE/EN live i verses.json
    private var fullLang: Lang? = null
    private var fullText: Full? = null

    /** blocking (parses ~2-3 MB) ➡ call off th main thread · null = DE/EN o file missing */
    @Synchronized
    fun full(lang: Lang): Full? {
        if (lang == Lang.DE || lang == Lang.EN) return null
        if (lang != fullLang) {
            fullText = runCatching { read<Full>("full_${lang.name.lowercase()}.json") }.getOrNull()
            fullLang = lang
        }
        return fullText
    }
}
