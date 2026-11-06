package de.strix.marxversion.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/** 1 accent per plan (by list position) · Parteibuch-red · Insight-Timer dusk tones · night blue · factory green */
private val PLAN_COLORS = listOf(
    Color(0xFFB3261E), Color(0xFF6A3093), Color(0xFF1E88E5), Color(0xFF00897B), Color(0xFFE07A2E),
    Color(0xFFD6577A), Color(0xFF9C7A12), Color(0xFF1F2F4F), Color(0xFF2E7D32),
)

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
        // 🎨 @1106 "make colors nicer": evy plan its own accent (stripe + emoji disc + day chip) · active = gold rim
        itemsIndexed(vm.content.plans) { i, p ->
            val accent = PLAN_COLORS[i % PLAN_COLORS.size]
            val active = s.activePlan == p.id
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { openPlan(p.id) },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = if (active) BorderStroke(2.dp, Gold) else null,
                elevation = CardDefaults.cardElevation(2.dp)) {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(6.dp).fillMaxHeight().background(accent))
                    Box(Modifier.padding(start = 12.dp, top = 14.dp).size(46.dp).clip(CircleShape).background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center) { Text(p.emoji, fontSize = 22.sp) }
                    Column(Modifier.padding(12.dp).weight(1f)) {
                        Text(planTitle(p), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = accent)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(tr("%d Tage", "%d days").f(p.days.size), fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold,
                                modifier = Modifier.background(accent, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 1.dp))
                            if (active) Text("  " + tr("✔ läuft", "✔ active"), fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                        }
                        Text(planDesc(p), fontSize = 14.sp, color = Color(0xFF2B2233))
                    }
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
