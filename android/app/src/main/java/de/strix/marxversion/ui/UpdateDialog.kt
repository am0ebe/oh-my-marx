package de.strix.marxversion.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.Update
import de.strix.marxversion.data.f
import kotlinx.coroutines.launch
import java.io.File

/**
 * 🆕 update: 1 tap ➡ download (progress) ➡ Android's install dialog · 1st time Android asks "allow installs fr this app?"
 * ➡ we send th usr there + carry on by ourselves when they come back (resume) · any failure ➡ fallback = th download page
 */
@Composable
fun UpdateDialog(vm: AppVm, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val l by vm.update.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf<Float?>(null) }   // null = not started · -1 = size unknown
    var apk by remember { mutableStateOf<File?>(null) }
    var failed by remember { mutableStateOf(false) }
    var waitAllow by remember { mutableStateOf(false) }
    val openPage = { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Update.page))) }; onDismiss() }

    fun install(f: File) {
        if (!Update.canInstall(ctx)) { waitAllow = true; runCatching { ctx.startActivity(Update.allowIntent(ctx)) }; return }
        waitAllow = false
        if (runCatching { ctx.startActivity(Update.installIntent(ctx, f)) }.isFailure) failed = true
    }
    // back fr "allow installs" ➡ go on wo another tap
    LifecycleResumeEffect(waitAllow) {
        if (waitAllow && Update.canInstall(ctx)) apk?.let { install(it) }
        onPauseOrDispose {}
    }

    MarxDialog(onDismiss, title = tr("Neue Version", "New version")) {
        Column(Modifier.fillMaxWidth()) {
            val u = l
            when {
                u == null -> Text(tr("Du hast die neueste Version (%s). ✔️", "You have the newest version (%s). ✔️").f(BuildConfig.VERSION_NAME))
                failed -> {
                    Text(tr("Ging nicht — Internet? Lade sie auf der Webseite.", "Didn't work — internet? Get it on the web page."))
                    Button(onClick = openPage, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) { Text(tr("🌐 Webseite öffnen", "🌐 Open web page")) }
                }
                else -> {
                    Text(tr("Oh My Marx %s ist da · du hast %s", "Oh My Marx %s is out · you have %s").f(u.version, BuildConfig.VERSION_NAME))
                    Text(tr("Installiert sich über die alte App — deine Daten bleiben.", "Installs over the old app — your data stays."),
                        fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 4.dp))
                    val p = progress
                    when {
                        waitAllow -> {
                            Text(tr("Einmalig: erlaube „Apps aus dieser Quelle installieren“ für Oh My Marx, dann zurück.",
                                "One time: allow “install apps from this source” for Oh My Marx, then come back."),
                                fontSize = 13.sp, modifier = Modifier.padding(top = 10.dp))
                            Button(onClick = { runCatching { ctx.startActivity(Update.allowIntent(ctx)) } },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(tr("Erlauben ➡", "Allow ➡")) }
                        }
                        apk != null -> Button(onClick = { install(apk!!) }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Text(tr("📦 Installieren", "📦 Install"))
                        }
                        p != null -> {
                            if (p < 0f) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 14.dp))
                            else LinearProgressIndicator(progress = { p }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp))
                            Text(tr("lädt …", "downloading …"), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                        u.apk == null -> Button(onClick = openPage, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Text(tr("🌐 Zur Download-Seite", "🌐 To the download page"))
                        }
                        else -> Button(onClick = {
                            progress = 0f
                            scope.launch {
                                runCatching { Update.download(ctx, u) { progress = it } }
                                    .onSuccess { apk = it; install(it) }
                                    .onFailure { failed = true }
                            }
                        }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) { Text(tr("⬇️ Laden + installieren", "⬇️ Download + install")) }
                    }
                    if (!failed && p == null) TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 2.dp)) { Text(tr("Später", "Later")) }
                }
            }
        }
    }
}
