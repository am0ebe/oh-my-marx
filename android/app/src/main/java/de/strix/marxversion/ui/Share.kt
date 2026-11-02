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
import java.io.File

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

// ── Vorlesen: Android's built-in TTS + "personas" = pitch/speed presets ──
// (real holy voices = pre-rendered audio, see notes.md § voice)

enum class Persona(val label: Pair<String, String>, val pitch: Float, val rate: Float) {
    PFARRER(T("⛪ Pfarrer", "⛪ Pastor"), 0.78f, 0.82f),
    PROPHET(T("🧔 Prophet Karl", "🧔 Prophet Karl"), 0.6f, 0.72f),
    NORMAL(T("🙂 Nüchtern", "🙂 Sober"), 1f, 1f),
    MASCHINE(T("🤖 Maschinerie (Kap. 13)", "🤖 Machinery (ch. 13)"), 1.5f, 1.2f);

    companion object { fun of(name: String) = entries.firstOrNull { it.name == name } ?: PFARRER }
}

class Speaker(ctx: Context) {
    private val app = ctx.applicationContext
    private var ready = false
    private var pending: (() -> Unit)? = null
    var speaking by mutableStateOf(false); private set

    // ⚠️ needs <queries> TTS_SERVICE i th manifest (Android 11+ package visibility) — else init fails silently
    private val tts = TextToSpeech(app) { status ->
        ready = status == TextToSpeech.SUCCESS
        if (ready) pending?.invoke()
        else Toast.makeText(app, "🔇 No text-to-speech engine — install “Speech Services by Google”", Toast.LENGTH_LONG).show()
        pending = null
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) { speaking = true }
            override fun onDone(id: String?) { speaking = false }
            @Deprecated("old API, still abstract") override fun onError(id: String?) { speaking = false }
        })
    }

    /** tap again while talking = stop */
    fun say(text: String, lang: Lang, persona: Persona) {
        if (speaking) { stop(); return }
        val go: () -> Unit = {
            val r = tts.setLanguage(lang.locale)
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(app, "🔇 ${lang.native}: voice missing — Android settings › Text-to-speech › install voice data", Toast.LENGTH_LONG).show()
            tts.setPitch(persona.pitch)
            tts.setSpeechRate(persona.rate)
            tts.speak(text.replace(Regex("\\[\\d+[a-z]?]"), ""), TextToSpeech.QUEUE_FLUSH, null, "marx")
        }
        if (ready) go() else pending = go  // 1st tap right aft opening: engine still booting
    }

    fun stop() { tts.stop(); speaking = false }
    fun close() = tts.shutdown()
}

/** 1 Speaker per screen, freed when th screen leaves (DisposableEffect = cleanup hook) */
@Composable
fun rememberSpeaker(): Speaker {
    val ctx = LocalContext.current
    val s = remember { Speaker(ctx) }
    DisposableEffect(Unit) { onDispose { s.close() } }
    return s
}
