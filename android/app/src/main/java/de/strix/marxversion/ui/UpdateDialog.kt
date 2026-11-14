package de.strix.marxversion.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.Update
import de.strix.marxversion.data.f
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Space
import kotlinx.coroutines.launch
import java.io.File

/**
 * 🆕 update: 1 tap ➡ download (progress) ➡ Android's install dialog · 1st time Android asks "allow installs fr this app?"
 * ➡ we send th usr there + carry on by ourselves when they come back (resume) · any failure ➡ fallback = th download page
 * look (design_system.md § 10, L5 @1114): reward-dialog pattern = 1 Primary-L (whatever th nxt step is) + Text "Later"
 * stacked · body-m lines + changelog list · 📳 Confirm on th Primary
 */
@Composable
fun UpdateDialog(vm: AppVm, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val l by vm.update.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val haptics = rememberHaptics()
    var progress by remember { mutableStateOf<Float?>(null) }   // null = not started · -1 = size unknown
    var apk by remember { mutableStateOf<File?>(null) }
    var failed by remember { mutableStateOf(false) }
    var waitAllow by remember { mutableStateOf(false) }
    val openPage = { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Update.page))) }; onDismiss() }

    // ❌ session failed (notify/InstallReceiver) ➡ web fallback
    val err by Update.installError.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { Update.installError.value = null }   // fresh dialog = no stale error
    // 🔄 Obtainium installed us ➡ let it do th update (D-023) · not there any more ➡ our own flow
    val viaObtainium = remember { Update.via(ctx) == Update.Via.OBTAINIUM }
    var obtainiumGone by remember { mutableStateOf(false) }

    fun install(f: File) {
        if (!Update.canInstall(ctx)) { waitAllow = true; runCatching { ctx.startActivity(Update.allowIntent(ctx)) }; return }
        waitAllow = false
        // session (not ACTION_VIEW) ➡ we become installer o record ➡ nxt updates silent (Android 12+)
        Update.installError.value = null
        if (runCatching { Update.installSession(ctx, f, bg = false) }.isFailure) failed = true
    }
    // back fr "allow installs" ➡ go on wo another tap
    LifecycleResumeEffect(waitAllow) {
        if (waitAllow && Update.canInstall(ctx)) apk?.let { install(it) }
        onPauseOrDispose {}
    }

    val u = l
    val p = progress
    val broken = failed || err != null
    /** Primary = 📳 Confirm (§ 8) */
    fun act(label: String, run: () -> Unit) = DialogAction(label) { haptics.confirm(); run() }
    // th 1 nxt step · same order + calls as b4 (look-only lane)
    val primary: DialogAction? = when {
        u == null -> null
        broken -> act(tr("Webseite öffnen", "Open web page"), openPage)
        viaObtainium && !obtainiumGone -> act(tr("In Obtainium aktualisieren", "Update in Obtainium")) {
            if (runCatching { ctx.startActivity(Update.obtainiumRefresh(ctx)) }.isSuccess) onDismiss() else obtainiumGone = true
        }
        waitAllow -> act(tr("Erlauben", "Allow")) { runCatching { ctx.startActivity(Update.allowIntent(ctx)) } }
        apk != null -> act(tr("Installieren", "Install")) { apk?.let { install(it) } }
        p != null -> null                                                  // downloading: th bar is th feedback
        u.apk == null -> act(tr("Zur Download-Seite", "To the download page"), openPage)
        else -> act(tr("Installieren", "Install")) {
            progress = 0f
            scope.launch {
                runCatching { Update.download(ctx, u) { progress = it } }
                    .onSuccess { apk = it; install(it) }
                    .onFailure { failed = true }
            }
        }
    }
    val defer = if (u != null && !broken && p == null) DialogAction(tr("Später", "Later"), onDismiss) else null
    val body = MaterialTheme.typography.bodyMedium

    MarxDialog(onDismiss, title = tr("Neue Version", "New version"), primary = primary, defer = defer) {
        Column(Modifier.fillMaxWidth()) {
            when {
                u == null -> Text(tr("Du hast die neueste Version (%s). ✔️", "You have the newest version (%s). ✔️").f(BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                broken -> {
                    Text(tr("Ging nicht — Internet? Lade sie auf der Webseite.", "Didn't work — internet? Get it on the web page."),
                        style = MaterialTheme.typography.bodyLarge)
                    err?.let { Text(it, style = body, color = Ds.redDeep, modifier = Modifier.padding(top = Space.xs)) }
                }
                else -> {
                    Text(tr("Oh My Marx %s ist da · du hast %s", "Oh My Marx %s is out · you have %s").f(u.version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodyLarge)
                    // 📜 what's new, if this build alr knows th release (DEV preview / same version notes)
                    RELEASES.firstOrNull { it.version == u.version.substringBefore("-") }?.items?.take(6)?.let { items ->
                        Column(Modifier.padding(top = Space.s)) { items.forEach { Text("• " + tr(it), style = body) } }
                    }
                    Text(tr("Installiert sich über die alte App — deine Daten bleiben.", "Installs over the old app — your data stays."),
                        style = body, color = Ds.inkMuted, modifier = Modifier.padding(top = Space.s))
                    if (waitAllow) Text(tr("Einmalig: erlaube „Apps aus dieser Quelle installieren“ für Oh My Marx, dann zurück.",
                        "One time: allow “install apps from this source” for Oh My Marx, then come back."),
                        style = body, modifier = Modifier.padding(top = Space.m))
                    if (p != null && apk == null && !(viaObtainium && !obtainiumGone) && !waitAllow) {
                        if (p < 0f) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = Space.l))
                        else LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().padding(top = Space.l))
                        Text(tr("lädt …", "downloading …"), style = MaterialTheme.typography.labelMedium, color = Ds.inkMuted,
                            modifier = Modifier.padding(top = Space.xs))
                    }
                }
            }
        }
    }
}
