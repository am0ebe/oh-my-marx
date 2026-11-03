package de.strix.marxversion.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.T
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.Holy
import de.strix.marxversion.logic.Layer
import de.strix.marxversion.logic.Room
import de.strix.marxversion.logic.Wav
import de.strix.marxversion.logic.holy
import android.media.MediaPlayer
import android.os.Bundle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.concurrent.ConcurrentHashMap

const val APP_NAME = "bibleApp ☭"

// ── Spruchkarte: kitsch sunset + Marx quote ➡ png ➡ Android share sheet ──
// drawn w plain android.graphics.Canvas (simpler than capturing a composable) · same bitmap = Losung popup preview

/** feast ≠ null ➡ 🎉 Sondermotiv: its colours + big emoji instead o th halo */
fun cardBitmap(quote: String, ref: String, lang: Lang, feast: Feast? = null): Bitmap {
    val w = 1080; val h = 1350
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    // Kalenderspruch-Sonnenuntergang (or th feast's palette)
    val bg = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            feast?.colors ?: intArrayOf(0xFF2B1B4F.toInt(), 0xFFB3261E.toInt(), 0xFFF29E4C.toInt(), 0xFFFFD98E.toInt()),
            floatArrayOf(0f, 0.45f, 0.8f, 1f), Shader.TileMode.CLAMP)
    }
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)
    // sun + halo
    c.drawCircle(w / 2f, h * 0.86f, 170f, Paint().apply { color = 0xCCFFE9A8.toInt() })
    if (feast == null) c.drawOval(w / 2f - 120, 70f, w / 2f + 120, 130f,
        Paint().apply { style = Paint.Style.STROKE; strokeWidth = 10f; color = 0xFFE8B931.toInt(); isAntiAlias = true })
    else Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 130f }.let { p ->  // colour emoji render fine i drawText
        c.drawText(feast.emoji, w / 2f - p.measureText(feast.emoji) / 2f, 175f, p)
    }

    val tp = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC)
        setShadowLayer(8f, 0f, 3f, 0x99000000.toInt())
    }
    val txt = lang.quote(quote)
    var size = 64f
    var layout: StaticLayout
    do {  // shrink til th quote fits
        tp.textSize = size
        layout = StaticLayout.Builder.obtain(txt, 0, txt.length, tp, w - 160)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.15f).build()
        size -= 4f
    } while (layout.height > h * 0.6 && size > 28f)
    c.save(); c.translate(80f, (h * 0.42f - layout.height / 2f).coerceAtLeast(if (feast == null) 170f else 230f)); layout.draw(c); c.restore()

    val small = TextPaint(tp).apply { textSize = 40f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    val by = "— Karl Marx, $ref"
    c.drawText(by, w / 2f - small.measureText(by) / 2f, h * 0.76f, small)
    small.textSize = 30f
    c.drawText(APP_NAME, w / 2f - small.measureText(APP_NAME) / 2f, h - 50f, small)
    return bmp
}

fun shareCard(ctx: Context, bmp: Bitmap, text: String, chooserTitle: String) {
    val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
    val f = File(dir, "marx.png")
    f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    ctx.startActivity(Intent.createChooser(send, chooserTitle))
}

fun copyText(ctx: Context, text: String) {
    ctx.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Marx", text))
}

// ── Vorlesen: Android's built-in TTS + "personas" ──
// plain = pitch/speed preset · holy ≠ null ➡ TTS ➡ wav per layer ➡ logic/Voice.kt (layer + Hall) ➡ MediaPlayer
// (real cloned voices = pre-rendered audio, later · notes.md § voice)

enum class Persona(val label: Pair<String, String>, val pitch: Float, val rate: Float, val holy: Holy? = null) {
    // ☭☁️ both: Marx's deep voice + an octave below + church = "Gottvater Marx"
    GOTTVATER(T("☭☁️ Gottvater Marx", "☭☁️ God the Father Marx"), 0.6f, 0.74f,
        Holy(listOf(Layer(0.6f), Layer(0.35f, 0.7f, 18)), Room.KIRCHE)),
    // ☁️ God: low + mid + high = choir, smeared a bit, huge hall
    GOTT(T("☁️ Gott (Kathedrale)", "☁️ God (cathedral)"), 0.45f, 0.7f,
        Holy(listOf(Layer(0.4f), Layer(0.8f, 0.55f, 25), Layer(1.3f, 0.25f, 45)), Room.KATHEDRALE)),
    // 📚 Marx alone, deep + slow, British Museum reading room (where Kapital was researched)
    MARX(T("📚 Marx im Lesesaal", "📚 Marx in the reading room"), 0.62f, 0.76f,
        Holy(listOf(Layer(0.62f)), Room.LESESAAL)),
    PFARRER(T("⛪ Pfarrer", "⛪ Pastor"), 0.78f, 0.82f, Holy(listOf(Layer(0.78f)), Room.KIRCHE)),
    NORMAL(T("🙂 Nüchtern", "🙂 Sober"), 1f, 1f),
    MASCHINE(T("🤖 Maschinerie (Kap. 13)", "🤖 Machinery (ch. 13)"), 1.5f, 1.2f);

    companion object { fun of(name: String) = entries.firstOrNull { it.name == name } ?: GOTTVATER }
}

class Speaker(ctx: Context) {
    private val app = ctx.applicationContext
    private var ready = false
    private var pending: (() -> Unit)? = null
    var speaking by mutableStateOf(false); private set

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private var player: MediaPlayer? = null
    /** synthesizeToFile passes in flight: utterance id ➡ done/failed */
    private val waits = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    // ⚠️ needs <queries> TTS_SERVICE i th manifest (Android 11+ package visibility) — else init fails silently
    private val tts = TextToSpeech(app) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) pending?.invoke()
        else Toast.makeText(app, "🔇 No text-to-speech engine — install “Speech Services by Google”", Toast.LENGTH_LONG).show()
        pending = null
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { if (id == PLAIN) speaking = true }
            override fun onDone(id: String?) { waits.remove(id)?.complete(true) ?: run { if (id == PLAIN) speaking = false } }
            @Deprecated("old API, still abstract") override fun onError(id: String?) { failed(id) }
            override fun onError(id: String?, code: Int) { failed(id) }
        })
    }

    private fun failed(id: String?) { waits.remove(id)?.complete(false) ?: run { if (id == PLAIN) speaking = false } }

    /** tap again while talking (or still rendering) = stop */
    fun say(text: String, lang: Lang, persona: Persona) {
        if (speaking) { stop(); return }
        val clean = text.replace(Regex("\\[\\d+[a-z]?]"), "").take(TextToSpeech.getMaxSpeechInputLength() - 1)
        val go: () -> Unit = {
            val r = tts.setLanguage(lang.locale)
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(app, "🔇 ${lang.native}: voice missing — Android settings › Text-to-speech › install voice data", Toast.LENGTH_LONG).show()
            if (persona.holy == null) plain(clean, persona)
            else {
                speaking = true  // = "⏹" right away, rendering takes ~1-3 s
                job = scope.launch {
                    val f = runCatching { render(clean, lang, persona, persona.holy) }.getOrNull()
                    withContext(Dispatchers.Main) {
                        if (!isActive) return@withContext
                        if (f == null) plain(clean, persona)  // engine can't write files ➡ at least speak
                        else play(f)
                    }
                }
            }
        }
        if (ready) go() else pending = go  // 1st tap right aft opening: engine still booting
    }

    private fun plain(text: String, persona: Persona) {
        tts.setPitch(persona.pitch)
        tts.setSpeechRate(persona.rate)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, PLAIN)
    }

    /** 1 TTS pass per layer ➡ layer + Hall ➡ cache/voice/<hash>.wav (same verse again = instant) */
    private suspend fun render(text: String, lang: Lang, persona: Persona, h: Holy): File? {
        val dir = File(app.cacheDir, "voice").apply { mkdirs() }
        val out = File(dir, "%08x.wav".format((persona.name + lang.code + text).hashCode()))
        if (out.length() > 44) return out
        val passes = h.layers.mapIndexed { i, l ->
            val id = "holy$i-${System.nanoTime()}"
            val f = File(dir, "pass-$id.wav")  // unique: 2 screens may render at once
            val d = CompletableDeferred<Boolean>().also { waits[id] = it }
            // pitch/rate r snapshotted per request ➡ set, queue, set, queue …
            tts.setPitch(l.pitch); tts.setSpeechRate(persona.rate)
            if (tts.synthesizeToFile(text, Bundle(), f, id) != TextToSpeech.SUCCESS) { waits.remove(id); return null }
            f to d
        }
        val ok = withTimeoutOrNull(60_000) { passes.all { it.second.await() } } ?: false
        if (!ok) return null
        val wav = holy(passes.map { Wav.read(it.first.readBytes()) }, h)
        out.writeBytes(Wav.write(wav))
        passes.forEach { it.first.delete() }
        // keep th cache 🤏: newest 30
        dir.listFiles { f -> f.name != out.name && !f.name.startsWith("pass") }
            ?.sortedByDescending { it.lastModified() }?.drop(29)?.forEach { it.delete() }
        return out
    }

    private fun play(f: File) {
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(f.path)
            setOnCompletionListener { speaking = false; it.release(); if (player === it) player = null }
            setOnErrorListener { mp, _, _ -> speaking = false; mp.release(); if (player === mp) player = null; true }
            prepare(); start()
        }
    }

    fun stop() {
        job?.cancel(); job = null
        player?.run { runCatching { stop() }; release() }; player = null
        tts.stop(); speaking = false
    }

    fun close() { stop(); scope.cancel(); tts.shutdown() }

    private companion object { const val PLAIN = "marx" }
}

/** 1 Speaker per screen, freed when th screen leaves (DisposableEffect = cleanup hook) */
@Composable
fun rememberSpeaker(): Speaker {
    val ctx = LocalContext.current
    val s = remember { Speaker(ctx) }
    DisposableEffect(Unit) { onDispose { s.close() } }
    return s
}
