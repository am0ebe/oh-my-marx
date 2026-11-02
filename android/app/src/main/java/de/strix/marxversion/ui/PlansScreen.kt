package de.strix.marxversion.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.f

@Composable
fun PlansScreen(vm: AppVm, openPlan: (String) -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text(tr("Lesepläne", "Reading plans"), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            Text(tr("Kleine Häppchen statt 800 Seiten. Das Spannende zuerst, die Wertform zuletzt (Althusser-approved).",
                "Small bites instead of 800 pages. The exciting bits first, the value-form last (Althusser-approved)."), fontSize = 13.sp)
            Gap()
        }
        items(vm.content.plans) { p ->
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { openPlan(p.id) }) {
                Column(Modifier.padding(14.dp)) {
                    Text("${p.emoji} " + planTitle(p) + if (s.activePlan == p.id) "  ✔️" else "",
                        fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text(tr("%d Tage", "%d days").f(p.days.size), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(planDesc(p), fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun PlanDetail(vm: AppVm, id: String, started: () -> Unit) {
    val p = vm.content.plan(id) ?: return
    val s by vm.settings.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        item {
            Text("${p.emoji} " + planTitle(p), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 12.dp))
            Text(planDesc(p))
            Gap()
            if (s.activePlan != p.id) Button(onClick = { vm.startPlan(p.id); started() }, modifier = Modifier.fillMaxWidth()) {
                Text(tr("Plan starten", "Start plan"))
            } else Text(tr("✔️ Aktiver Plan", "✔️ Active plan"), color = MaterialTheme.colorScheme.primary)
            Gap()
        }
        items(p.days.size) { i ->
            val d = p.days[i]
            val first = d.verseIds.firstNotNullOfOrNull { vm.content.verse(it) }
            Column(Modifier.padding(vertical = 6.dp)) {
                Text(tr("Tag %d", "Day %d").f(i + 1) + " · " + d.verseIds.joinToString(", "), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                first?.let { Text(quoted(verseQuote(it)), fontSize = 13.sp, maxLines = 2) }
            }
        }
    }
}
