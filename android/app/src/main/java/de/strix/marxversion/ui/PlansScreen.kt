package de.strix.marxversion.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.Plan
import de.strix.marxversion.data.f
import de.strix.marxversion.ui.ds.AccentPanel
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Space
import de.strix.marxversion.ui.ds.Tag
import de.strix.marxversion.ui.ds.planAccent

// 🎨 L3 @1114 (design_system.md § 10 Plans): cards = AccentPanel (4dp stripe · emoji disc · title ink · hairline) · day + active = Tag

/** head o a plan card / plan detail: title ink + "N days" Tag + gold "✔ active" Tag */
@Composable
private fun PlanHead(p: Plan, accent: androidx.compose.ui.graphics.Color, active: Boolean) {
    Text(planTitle(p), style = MaterialTheme.typography.titleMedium, color = Ds.ink)
    Row(horizontalArrangement = Arrangement.spacedBy(Space.s), modifier = Modifier.padding(top = Space.xs)) {
        Tag(tr("%d Tage", "%d days").f(p.days.size), accent = accent)
        if (active) Tag(tr("✔ läuft", "✔ active"), gold = true)
    }
}

@Composable
fun PlansScreen(vm: AppVm, openPlan: (String) -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val open by vm.plansOpen.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxWidth().padding(horizontal = Space.gutter), verticalArrangement = Arrangement.spacedBy(Space.betwPanels)) {
        item {
            Column {
                Text(tr("Lesepläne", "Reading plans"), style = MaterialTheme.typography.titleLarge, color = Ds.ink,
                    modifier = Modifier.padding(top = Space.l, bottom = Space.s))
                Text(tr("Kleine Häppchen statt 800 Seiten. Das Spannende zuerst, die Wertform zuletzt (Althusser-approved).",
                    "Small bites instead of 800 pages. The exciting bits first, the value-form last (Althusser-approved)."),
                    style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted)
            }
        }
        // 🎨 @1106 "make colors nicer": evy plan its own accent (stripe + emoji disc + day tag) · active = gold ring
        itemsIndexed(vm.content.plans) { i, p ->
            val active = s.activePlan == p.id
            if (p.id !in open) {
                // 🔒 progressive disclosure (D-043): no title, no content ➡ only HOW it opens · a bonus level, not a paywall
                AccentPanel(Ds.inkMuted, "🔒", head = {
                    Text(tr("Geheimer Leseplan", "Secret reading plan"), style = MaterialTheme.typography.titleMedium, color = Ds.inkMuted)
                }) { Text(lockHint(p.lock!!), style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted) }
                return@itemsIndexed
            }
            AccentPanel(planAccent(i), p.emoji, active = active, onClick = { openPlan(p.id) },
                head = { PlanHead(p, planAccent(i), active) }) {
                Text(planDesc(p), style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
            }
        }
        item { Box(Modifier.padding(bottom = Space.l)) }
    }
}

@Composable
fun PlanDetail(vm: AppVm, id: String, started: () -> Unit) {
    val p = vm.content.plan(id) ?: return
    val s by vm.settings.collectAsStateWithLifecycle()
    val open by vm.plansOpen.collectAsStateWithLifecycle()
    if (p.id !in open) return   // 🔒 deep link into a locked plan = nothing 2 see yet
    val days by vm.days.collectAsStateWithLifecycle()
    val done = days.filter { it.planId == p.id }.map { it.planDay }.toSet()
    val active = s.activePlan == p.id
    val planDays = if (active) p.daysFor(s.planVer) else p.days   // 🔢 running plan = th cut u started on (D-042)
    val accent = planAccent(vm.content.plans.indexOfFirst { it.id == p.id })
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = Space.gutter)) {
            item {
                Box(Modifier.padding(top = Space.l, bottom = Space.xl)) {
                    AccentPanel(accent, p.emoji, active = active, head = { PlanHead(p, accent, active) }) {
                        Text(planDesc(p), style = MaterialTheme.typography.bodyMedium, color = Ds.ink)
                    }
                }
                Text(tr("Tage", "Days"), style = MaterialTheme.typography.titleSmall, color = Ds.ink, modifier = Modifier.padding(bottom = Space.s))
            }
            itemsIndexed(planDays) { i, d ->
                val ok = i in done
                val first = d.verseIds.firstNotNullOfOrNull { vm.content.verse(it) }
                val dayLabel = tr("Tag %d", "Day %d").f(i + 1)
                // row 56: ✔ / ○ · day + ref · 1-line snippet
                Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(vertical = Space.s)
                    .semantics(mergeDescendants = true) { contentDescription = dayLabel + if (ok) " ✔" else "" },
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(if (ok) "✔" else "○", style = MaterialTheme.typography.titleMedium, color = if (ok) Ds.ok else Ds.inkMuted,
                        textAlign = TextAlign.Center, modifier = Modifier.width(Space.xl))
                    Column(Modifier.padding(start = Space.m).weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dayLabel, style = MaterialTheme.typography.labelLarge, color = Ds.ink)
                            Text("  " + d.verseIds.joinToString(", "), style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        first?.let {
                            Text(quoted(verseQuote(it)), style = MaterialTheme.typography.bodyMedium, color = Ds.ink,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (i < planDays.lastIndex) HorizontalDivider(color = Ds.line)
            }
            item { Box(Modifier.padding(bottom = Space.l)) }
        }
        // 1 Primary-L pinned at th bottom · only th existing action ("start") — active plan = gold Tag up top, no btn
        if (!active) Box(Modifier.fillMaxWidth().padding(horizontal = Space.gutter, vertical = Space.m)) {
            Primary(tr("Plan starten", "Start plan"), { vm.startPlan(p.id); started() })
        }
    }
}

/** 🔒 HOW a locked plan opens (any 1 o th conditions) · tone = bonus level, not a paywall */
@Composable
fun lockHint(l: de.strix.marxversion.data.PlanLock): String = listOfNotNull(
    if (l.plans > 0) (if (l.plans == 1) tr("wenn du einen Leseplan beendest", "when you finish a reading plan")
        else tr("wenn du %d Lesepläne beendest", "when you finish %d reading plans").f(l.plans)) else null,
    if (l.orden > 0) tr("bei %d Abzeichen", "at %d badges").f(l.orden) else null,
    if (l.streak > 0) tr("bei einer Serie von %d Tagen", "at a %d-day streak").f(l.streak) else null,
    if (l.feast == "WEIHNACHTEN") tr("🎁 als Geschenk zu Weihnachten", "🎁 as a Christmas present") else null,
    if (l.lucky > 0) tr("🎲 irgendwann, als Überraschung an einem Lesetag", "🎲 some day, as a surprise on a reading day") else null,
).joinToString(tr(" — oder ", " — or "), prefix = tr("Öffnet sich ", "Opens ")) + "."

/** 🎁 "new reading plan unlocked" = own moment (queued like a badge, MainActivity R_PLAN) */
@Composable
fun PlanUnlockedDialog(plans: List<Plan>, onOpen: () -> Unit, onDismiss: () -> Unit) {
    Confetti {}
    MarxDialog(onDismiss, title = tr("🎁 Neuer Leseplan freigeschaltet!", "🎁 New reading plan unlocked!"),
        primary = DialogAction(tr("Ansehen", "Take a look"), onOpen)) {
        Column {
            plans.forEach { p ->
                Text(p.emoji + "  " + planTitle(p), style = MaterialTheme.typography.titleMedium, color = Ds.ink,
                    modifier = Modifier.padding(top = Space.s))
                Text(planDesc(p), style = MaterialTheme.typography.bodyMedium, color = Ds.inkMuted)
            }
        }
    }
}
