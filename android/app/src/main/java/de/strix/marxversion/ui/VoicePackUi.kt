package de.strix.marxversion.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.PackInfo
import de.strix.marxversion.data.f
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.TextBtn

/** MB 4 humans: 41 000 000 ➡ "41" · < 1 MB ➡ "1" (a dialog nvr says 0 MB) */
fun mb(bytes: Long): Int = ((bytes + 999_999) / 1_000_000).toInt().coerceAtLeast(1)

/** 🔊 D-085: 1st 🔊 tap in a lang th hs a pack ➡ ask (size · once) · Later / outside tap / Esc = system voice, ⚙️ can load it later */
@Composable
fun VoiceAskDialog(p: PackInfo, onLoad: () -> Unit, onLater: () -> Unit) =
    MarxDialog(onLater, title = tr("🔊 KI-Stimme laden?", "🔊 Load the AI voice?"),
        primary = DialogAction(tr("Laden · %d MB", "Load · %d MB").f(mb(p.bytes)), onLoad),
        defer = DialogAction(tr("Später", "Later"), onLater)) {
        Text(tr("Liest die Losungen mit einer KI-Stimme vor, einmal laden, dann offline. Bis dahin liest die Stimme deines Handys.",
            "Reads the daily quotes in an AI voice: load it once, then it works offline. Until then your phone's voice reads."),
            style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
        if (p.label.isNotBlank()) Text(p.label, style = MaterialTheme.typography.bodySmall, color = Ds.inkMuted, modifier = Modifier.padding(top = 6.dp))
    }

/** ⚙️ Mine row: loaded ➡ label · MB · Delete (+ new version) · offered ➡ Load · MB · busy ➡ bar · no pack 4 th lang ➡ nothing */
@Composable
fun VoicePackRow(vm: AppVm, lang: Lang, v: AppVm.VoiceUi) {
    if (v.lang != lang.code || (v.offer == null && v.have == null)) return
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(tr("🔊 KI-Stimme", "🔊 AI voice"))
        val have = v.have
        when {
            v.progress != null -> LinearProgressIndicator(progress = { v.progress.coerceIn(0f, 1f) }, color = Ds.gold, trackColor = Ds.paperDeep,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            have != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${have.label.ifBlank { have.persona }} · %d MB".f(mb(v.bytes)), style = MaterialTheme.typography.bodySmall,
                    color = Ds.inkMuted, modifier = Modifier.weight(1f))
                v.update?.let { u -> TextBtn(tr("Neue Version", "New version"), { vm.loadVoice(u) }) }
                TextBtn(tr("Löschen", "Delete"), { vm.deleteVoice(lang) }, danger = true)
            }
            v.offer != null -> Primary(tr("Laden · %d MB", "Load · %d MB").f(mb(v.offer.bytes)), { vm.loadVoice(v.offer) },
                Modifier.padding(top = 6.dp), large = false)
        }
        if (v.failed) Text(tr("⚠️ Laden ging nicht — Internet? Nochmal versuchen.", "⚠️ Loading didn't work — internet? Try again."),
            style = MaterialTheme.typography.bodySmall, color = Ds.redDeep, modifier = Modifier.padding(top = 4.dp))
        Text(tr("Ohne KI-Stimme liest die Stimme deines Handys.", "Without the AI voice, your phone's voice reads."),
            style = MaterialTheme.typography.bodySmall, color = Ds.inkMuted, modifier = Modifier.padding(top = 4.dp))
    }
}
