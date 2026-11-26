package de.strix.marxversion.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * 🔊 AI voice packs (D-085 · audio crew's contract `pr/audio/voice/omm_contract.md`, voice D-028)
 *  scagent.de/oh-my-marx/voice/manifest.json lists 1 zip per persona + lang · th app loads its lang on th 1st 🔊 (asks 1st, shows MB)
 *  zip = index.json + <hex>.opus · item ids = th app's own: `q:<verse id>` Losung quote · `a:<plan>:<day>` Andacht
 *  stale guard: item sha = 1st 12 hex o SHA-1 over th RAW string th app would speak ➡ txt changed since th render ➡ system TTS
 *  nvr i th APK · no manifest yet (404 · offline) = no offer = old behaviour (system TTS)
 */
@Serializable data class VoiceManifest(val v: Int = 0, val base: String = "", val packs: List<PackInfo> = emptyList())
@Serializable data class PackInfo(val persona: String = "", val lang: String = "", val file: String = "", val bytes: Long = 0,
    val sha256: String = "", val items: Int = 0, val created: String = "", val label: String = "")
@Serializable data class PackIndex(val v: Int = 0, val persona: String = "", val lang: String = "", val label: String = "",
    val created: String = "", val items: Map<String, PackItem> = emptyMap())
@Serializable data class PackItem(val f: String = "", val sec: Double = 0.0, val sha: String = "")

class VoicePacks(
    private val root: File,
    private val fetchText: (String) -> String = ::httpText,
    private val fetchTo: (String, File, (Float) -> Unit) -> Unit = ::httpTo,
) {
    companion object {
        const val BASE = "https://scagent.de/oh-my-marx/voice/"
        const val FORMAT = 1
        const val RECHECK_MS = 7L * 24 * 3600 * 1000   // contract: manifest at most 1×/week
        const val RETRY_MS = 24L * 3600 * 1000         // no manifest yet (404 · offline) ➡ try again next day, not on evy open
        private val ZIP_NAME = Regex("[A-Za-z0-9._-]{1,80}\\.zip")
        private val CLIP = Regex("[0-9a-f]{8,40}\\.opus")
        fun sha12(raw: String): String = MessageDigest.getInstance("SHA-1").digest(raw.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }.take(12)
        fun sha256(f: File): String = MessageDigest.getInstance("SHA-256").let { md ->
            f.inputStream().use { i -> val b = ByteArray(1 shl 16); while (true) { val n = i.read(b); if (n < 0) break; md.update(b, 0, n) } }
            md.digest().joinToString("") { "%02x".format(it) }
        }
        private fun open(url: String) = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = 8000; readTimeout = 20000 }
        fun httpText(url: String): String = open(url).run {
            if (responseCode != 200) throw IOException("$responseCode $url"); inputStream.bufferedReader().use { it.readText() } }
        fun httpTo(url: String, out: File, progress: (Float) -> Unit) = open(url).run {
            if (responseCode != 200) throw IOException("$responseCode $url")
            val total = contentLengthLong
            inputStream.use { i -> out.outputStream().use { o ->
                val b = ByteArray(1 shl 16); var done = 0L
                while (true) { val n = i.read(b); if (n < 0) break; o.write(b, 0, n); done += n; if (total > 0) progress(done.toFloat() / total) }
            } }
        }
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val cache = HashMap<String, PackIndex?>()
    private fun dir(lang: String) = File(root, lang)
    private val manifestFile get() = File(root, "manifest.json")

    private val swap = Any()      // guards cache + th folder swap only (a 40 MB download must not block a 🔊 lookup)
    private val busy = Any()      // 1 install at a ti

    /** installed pack 4 lang (read once, then cached) · unknown format / broken = none */
    fun installed(lang: String): PackIndex? = synchronized(swap) { cache.getOrPut(lang) {
        runCatching { json.decodeFromString<PackIndex>(File(dir(lang), "index.json").readText()) }.getOrNull()?.takeIf { it.v == FORMAT }
    } }

    /** th opus 4 this item, only if its txt is still th one th pack was rendered from */
    fun clip(lang: String, id: String, raw: String): File? {
        val it = installed(lang)?.items?.get(id) ?: return null
        if (!CLIP.matches(it.f) || it.sha != sha12(raw)) return null
        return File(dir(lang), it.f).takeIf { f -> f.length() > 0 }
    }

    /** manifest: cached copy, refetched aftr a week (o force) · offline / 404 ➡ th cached one (o null)
     *  evy TRY is stamped (404 · offline too) ➡ at most 1 GET per day while there's no manifest, 1 per week aftr */
    fun manifest(now: Long, force: Boolean = false): VoiceManifest? {
        val f = manifestFile; val tried = File(root, "manifest.tried")
        val every = if (f.exists()) RECHECK_MS else RETRY_MS
        if (force || !tried.exists() || now - tried.lastModified() > every) {
            root.mkdirs(); tried.writeText(""); tried.setLastModified(now)
            runCatching {
                val t = fetchText(BASE + "manifest.json")
                json.decodeFromString<VoiceManifest>(t)   // garbage ➡ throws ➡ keep th old copy
                f.writeText(t)
            }
        }
        return runCatching { json.decodeFromString<VoiceManifest>(f.readText()) }.getOrNull()?.takeIf { it.v == FORMAT }
    }

    /** th "load th AI voice?" dialog: at most 1× a week per lang ("Later" = really later) */
    fun shouldAsk(lang: String, now: Long): Boolean = File(root, "asked_$lang").let { !it.exists() || now - it.lastModified() > RECHECK_MS }
    fun asked(lang: String, now: Long) { root.mkdirs(); File(root, "asked_$lang").apply { writeText(""); setLastModified(now) } }

    /** th pack th manifest offers 4 lang (1st listed = th chosen persona) */
    fun offer(m: VoiceManifest?, lang: String): PackInfo? =
        m?.packs?.firstOrNull { it.lang == lang && ZIP_NAME.matches(it.file) && it.sha256.length == 64 && it.bytes > 0 }

    /** offered pack is newer than th installed one (contract: newer `created` ➡ offer an update) */
    fun update(m: VoiceManifest?, lang: String): PackInfo? {
        val o = offer(m, lang) ?: return null
        val have = installed(lang) ?: return null
        return o.takeIf { it.created > have.created || it.persona != have.persona }
    }

    /** download ➡ sha256 ➡ unzip (only index.json + <hex>.opus, no paths) ➡ swap in · any failure = old pack stays, nothing half-written */
    fun install(p: PackInfo, base: String = BASE, progress: (Float) -> Unit = {}): Boolean = synchronized(busy) {
        root.mkdirs()
        val zip = File(root, "dl.zip"); val tmp = File(root, "${p.lang}.new")
        try {
            tmp.deleteRecursively(); tmp.mkdirs()
            fetchTo((base.ifBlank { BASE }).trimEnd('/') + "/" + p.file, zip, progress)
            if (sha256(zip) != p.sha256.lowercase()) return false
            ZipInputStream(zip.inputStream().buffered()).use { z ->
                while (true) {
                    val e = z.nextEntry ?: break
                    if (e.isDirectory || !(e.name == "index.json" || CLIP.matches(e.name))) continue   // no "../", no subdirs
                    File(tmp, e.name).outputStream().use { z.copyTo(it) }
                }
            }
            val idx = json.decodeFromString<PackIndex>(File(tmp, "index.json").readText())
            if (idx.v != FORMAT || idx.lang != p.lang) return false
            synchronized(swap) {   // 🔊 lookups wait only 4 these 2 renames
                val old = dir(p.lang); val bak = File(root, "${p.lang}.old")
                bak.deleteRecursively()
                if (old.exists() && !old.renameTo(bak)) return false
                if (!tmp.renameTo(old)) { bak.renameTo(old); return false }
                cache.remove(p.lang)
                bak.deleteRecursively()
            }
            return true
        } catch (e: Exception) {
            return false
        } finally { zip.delete(); tmp.deleteRecursively() }
    }

    fun delete(lang: String) = synchronized(swap) { dir(lang).deleteRecursively(); cache.remove(lang); Unit }
    fun bytes(lang: String): Long = dir(lang).listFiles()?.sumOf { it.length() } ?: 0L
}
