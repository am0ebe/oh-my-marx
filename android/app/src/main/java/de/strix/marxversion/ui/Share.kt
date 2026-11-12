package de.strix.marxversion.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
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
import android.os.Build
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.strix.marxversion.R
import de.strix.marxversion.MarxApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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

const val APP_NAME = "Oh My Marx ☭"  // @1103 (was bibleApp = YouVersion's name) · applicationId stays de.strix.marxversion

// ── Spruchkarte: kitsch sunset + Marx quote ➡ png ➡ Android share sheet ──
// drawn w plain android.graphics.Canvas (simpler than capturing a composable) · same bitmap = Losung popup preview

/** 📉 th crisis shout i evy lang (card header + shake flash) · short on purpose: it's 120 px / 56 sp */
fun kriseWord(lang: Lang) = "📉 " + when (lang) {
    Lang.DE -> "KRISE!"; Lang.EN -> "CRISIS!"; Lang.IT -> "CRISI!"; Lang.ES -> "¡CRISIS!"
    Lang.PT -> "CRISE!"; Lang.ZH -> "危机！"; Lang.AR -> "!أزمة"
    Lang.FR -> "CRISE !"; Lang.RU -> "КРИЗИС!"; Lang.HI -> "संकट!"; Lang.BN -> "সংকট!"; Lang.UR -> "!بحران"; Lang.ID -> "KRISIS!"
}

/** 📉 crisis card palette: black ➡ blood ➡ alarm red */
private val KRISE_COLORS = intArrayOf(0xFF000000.toInt(), 0xFF3A0000.toInt(), 0xFF8E0000.toInt(), 0xFFE53935.toInt())

/**
 * feast ≠ null ➡ 🎉 Sondermotiv: its colours + its emoji next 2 th saint · krise ➡ 📉 black/red, crashing chart, no halo
 * evy normal card: 😇 Saint Marx (halo + beard = res/drawable/ic_launcher_fg) on top
 */
fun cardBitmap(ctx: Context, quote: String, ref: String, lang: Lang, feast: Feast? = null, krise: Boolean = false): Bitmap {
    val w = 1080; val h = 1350
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    // Kalenderspruch-Sonnenuntergang (or th feast's palette / crisis)
    val bg = Paint().apply {
        shader = LinearGradient(0f, 0f, 0f, h.toFloat(),
            if (krise) KRISE_COLORS else feast?.colors ?: intArrayOf(0xFF2B1B4F.toInt(), 0xFFB3261E.toInt(), 0xFFF29E4C.toInt(), 0xFFFFD98E.toInt()),
            floatArrayOf(0f, 0.45f, 0.8f, 1f), Shader.TileMode.CLAMP)
    }
    c.drawRect(0f, 0f, w.toFloat(), h.toFloat(), bg)
    if (krise) {
        // 📉 jagged crash line across th card + "KRISE!" header
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 14f; color = 0x66FFFFFF; strokeJoin = Paint.Join.ROUND }
        val pts = floatArrayOf(0f, 0.30f, 0.12f, 0.26f, 0.22f, 0.34f, 0.33f, 0.29f, 0.45f, 0.45f, 0.55f, 0.40f, 0.66f, 0.62f, 0.78f, 0.57f, 0.9f, 0.85f, 1f, 0.97f)
        val path = android.graphics.Path().apply {
            moveTo(pts[0] * w, pts[1] * h)
            for (i in 2 until pts.size step 2) lineTo(pts[i] * w, pts[i + 1] * h)
        }
        c.drawPath(path, line)
        val head = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt(); textSize = 120f; typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setShadowLayer(18f, 0f, 0f, 0xFFFF1744.toInt())
        }
        val t = kriseWord(lang)
        c.drawText(t, w / 2f - head.measureText(t) / 2f, 190f, head)
    } else {
        // sun + 😇 Saint Marx (vector: halo + hair + beard)
        c.drawCircle(w / 2f, h * 0.86f, 170f, Paint().apply { color = 0xCCFFE9A8.toInt() })
        ContextCompat.getDrawable(ctx, R.drawable.ic_launcher_fg)?.apply {
            setBounds(w / 2 - 140, 0, w / 2 + 140, 280)
            draw(c)
        }
        feast?.let { f ->
            Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 110f }.let { p ->  // colour emoji render fine i drawText
                c.drawText(f.emoji, w / 2f + 160f, 190f, p)
            }
        }
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
    } while (layout.height > h * 0.5 && size > 28f)  // room: below th saint (290) ➡ above th ref (0.76 h)
    if (layout.height > h * 0.5) {
        // still 2 long at th smallest size (a whole paragraph) ➡ as many lines as fit, then "…"
        val lines = (h * 0.5f / (layout.height.toFloat() / layout.lineCount)).toInt().coerceAtLeast(1)
        layout = StaticLayout.Builder.obtain(txt, 0, txt.length, tp, w - 160)
            .setAlignment(Layout.Alignment.ALIGN_CENTER).setLineSpacing(0f, 1.15f)
            .setMaxLines(lines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
    }
    c.save(); c.translate(80f, (h * 0.46f - layout.height / 2f).coerceAtLeast(290f)); layout.draw(c); c.restore()

    val small = TextPaint(tp).apply { textSize = 40f; typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL) }
    val by = "— Karl Marx, $ref"
    c.drawText(by, w / 2f - small.measureText(by) / 2f, h * 0.76f, small)
    small.textSize = 30f
    c.drawText(APP_NAME, w / 2f - small.measureText(APP_NAME) / 2f, h - 50f, small)
    return bmp
}

/** link on evy shared thing (viral loop: card ➡ "what app is that?" ➡ link) */
const val APP_LINK = "https://scagent.de/omm"

/** th receiver 4 th last share sheet · only 1 alive: a cancelled sheet never calls back ➡ drop it on th nxt share */
private var chosenReceiver: BroadcastReceiver? = null

/**
 * 📤 open th share sheet · 🎖️ count (Orden / 1st-share badge) ONLY when th user picks a target
 * Android tells us via createChooser(IntentSender) ➡ cancel = no callback = no count (QA @1112: badge fired w sheet still open)
 * ⚠️ "picked a target" ≠ "sent" (user can still back out inside th target app) — closest Android lets us K
 */
private fun launchChooser(ctx: Context, send: Intent, title: String) {
    val app = ctx.applicationContext
    chosenReceiver?.let { runCatching { app.unregisterReceiver(it) } }
    val action = app.packageName + ".SHARE_CHOSEN"
    val rec = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            runCatching { app.unregisterReceiver(this) }
            if (chosenReceiver === this) chosenReceiver = null
            (app as? MarxApp)?.let { a -> a.scope.launch { a.prefs.shared() } }
        }
    }
    chosenReceiver = rec
    ContextCompat.registerReceiver(app, rec, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0)
    val pi = PendingIntent.getBroadcast(app, 0, Intent(action).setPackage(app.packageName), flags)
    ctx.startActivity(Intent.createChooser(send, title, pi.intentSender))
}

/**
 * 🐢 QA @1112 "share btn lags": render (StaticLayout 1080×1350) + PNG-compress (~1 MB) ran on th MAIN thread at th tap ➡ frozen UI.
 * Now: both on Dispatchers.Default, only startActivity back on main.
 */
fun shareCard(ctx: Context, make: () -> Bitmap, text: String, chooserTitle: String) {
    val app = ctx.applicationContext as? MarxApp ?: return
    app.scope.launch(Dispatchers.Default) {
        val bmp = make()
        val dir = File(ctx.cacheDir, "share").apply { mkdirs() }
        val f = File(dir, "marx.png")
        f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".files", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, if ("scagent.de" in text) text else "$text · $APP_LINK")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        withContext(Dispatchers.Main) { launchChooser(ctx, send, chooserTitle) }
    }
}

/** same, bitmap alr there (popup) — still compresses off-main */
fun shareCard(ctx: Context, bmp: Bitmap, text: String, chooserTitle: String) = shareCard(ctx, { bmp }, text, chooserTitle)

/** 📤 "share th app" (text + link, no image) · counts like a card share once a target is picked */
fun shareApp(ctx: Context, text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "$text $APP_LINK") }
    launchChooser(ctx, send, chooserTitle)
}

fun copyText(ctx: Context, text: String) {
    ctx.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Marx", text))
}

/** 📋 lil copy btn (like on a chat app's code block) · Android 13+ shows its own "copied" bubble ➡ toast only below */
@Composable
fun CopyIcon(text: String, tint: Color = MaterialTheme.colorScheme.secondary) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val done = tr("📋 Kopiert", "📋 Copied")
    IconButton(onClick = {
        copyText(ctx, text)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (Build.VERSION.SDK_INT < 33) Toast.makeText(ctx, done, Toast.LENGTH_SHORT).show()
    }, modifier = Modifier.size(36.dp)) {
        Icon(painterResource(R.drawable.ic_copy), tr("Kopieren", "Copy"), Modifier.size(18.dp), tint = tint)
    }
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

    private fun clean(t: String) = t.replace(Regex("\\[\\d+[a-z]?]"), "").take(TextToSpeech.getMaxSpeechInputLength() - 1)

    /** renders running or done: same text+voice asked twice (prerender, then tap) ➡ 1 render */
    private val inflight = ConcurrentHashMap<String, Deferred<File?>>()

    private fun renderAsync(text: String, lang: Lang, persona: Persona, h: Holy): Deferred<File?> {
        val k = persona.name + lang.code + text
        inflight[k]?.let { return it }
        val d = scope.async(start = CoroutineStart.LAZY) {
            try { runCatching { render(text, lang, persona, h) }.getOrNull() } finally { inflight.remove(k) }
        }
        inflight[k] = d
        d.start()
        return d
    }

    /** render th holy wav i th background (eg th Losung while th popup is open) ➡ 🔊 tap plays at once */
    fun prerender(text: String, lang: Lang, persona: Persona) {
        val h = persona.holy ?: return
        val go: () -> Unit = { renderAsync(clean(text), lang, persona, h) }
        if (ready) go() else if (pending == null) pending = go
    }

    /** voice picker: always (re)start, even while sth else plays */
    fun sample(text: String, lang: Lang, persona: Persona) { stop(); say(text, lang, persona) }

    /** tap again while talking (or still rendering) = stop */
    fun say(text: String, lang: Lang, persona: Persona) {
        if (speaking) { stop(); return }
        val clean = clean(text)
        val go: () -> Unit = {
            val r = tts.setLanguage(lang.locale)
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED)
                Toast.makeText(app, "🔇 ${lang.native}: voice missing — Android settings › Text-to-speech › install voice data", Toast.LENGTH_LONG).show()
            if (persona.holy == null) plain(clean, persona)
            else {
                speaking = true  // = "⏹" right away, rendering takes ~1-3 s (0 if prerendered)
                val d = renderAsync(clean, lang, persona, persona.holy)
                job = scope.launch {
                    val f = d.await()
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
        tts.setLanguage(lang.locale)  // also snapshotted per request (prerender may run w a dif lang)
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

    /** hold th popup = pause (stories-style) · plain TTS can't pause ➡ stops */
    fun pause() { player?.takeIf { it.isPlaying }?.pause() ?: run { if (speaking && player == null) stop() } }
    fun resume() { player?.takeIf { !it.isPlaying && speaking }?.start() }

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
