package de.strix.marxversion.ui

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.strix.marxversion.R
import de.strix.marxversion.data.T
import de.strix.marxversion.data.f
import de.strix.marxversion.logic.CARD
import de.strix.marxversion.logic.Cell
import de.strix.marxversion.logic.StampCard
import de.strix.marxversion.ui.ds.CloseStar
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.Shapes
import de.strix.marxversion.ui.ds.OmmType
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.StreakStar
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 🏆 C2 moments (design § 14.1 · D-048 Stempeluhr · D-049 ladder · D-050 Beförderung · D-046/047 Meilenstein)
 * 🔑 1 big screen per tap: queue keys `dayDone` ➡ `milestone` ➡ `rank` (TodayScreen RewardSlots, CelebrationQueue order)
 * reduced motion ➡ evy screen starts at its end frame · 📳 + txt stay
 */

/** 🎟️ ranks: 1 per full Stempelkarte — th path through Kapital's own history o capitalism (was TodayScreen) */
val RANKS = listOf(
    T("📗 Ursprüngliche Akkumulation", "📗 Primitive accumulation"),
    T("🔁 Einfache Reproduktion", "🔁 Simple reproduction"),
    T("📈 Erweiterte Reproduktion", "📈 Expanded reproduction"),
    T("🏭 Manufaktur", "🏭 Manufacture"),
    T("⚙️ Große Industrie", "⚙️ Large-scale industry"),
    T("🧔 Klassiker-Status", "🧔 Classic status"),
    T("☭ Expropriation der Expropriateure", "☭ Expropriating the expropriators"),
)
fun rank(i: Int) = RANKS[i.coerceIn(0, RANKS.lastIndex)]
private val RANK_ART = listOf(R.drawable.rank_1, R.drawable.rank_2, R.drawable.rank_3, R.drawable.rank_4,
    R.drawable.rank_5, R.drawable.rank_6, R.drawable.rank_7)
fun rankArt(i: Int) = RANK_ART[i.coerceIn(0, RANK_ART.lastIndex)]

/** perks (cosmetic, honest · D-050) · 1 line on th Beförderung screen */
private val PERKS = listOf(
    T("Neu: dein Held bekommt einen Goldrand", "New: your hero gets a gold border"),
    T("Neu: deine Stempelkarte wird blaues Leinen", "New: your stamp card turns blue cloth"),
    T("Neu: der Leseplan „Marx-Grundkurs“ ist frei 🔓", "New: the “Marx Basics” reading plan is unlocked 🔓"),
    T("Neu: in Kreisen steht dein Rang neben „du“", "New: in Circles your rank shows next to “you”"),
    T("Neu: dein Stern bekommt einen Zahnkranz", "New: your star gets a gear rim"),
    T("Neu: dein Name steht im Kolophon des Buchs", "New: your name is in the book's colophon"),
    T("Neu: der rote Stern wird für immer gold", "New: the red star turns gold for good"),
)

/** rank idx whose perk is built · @1120 #206: all 7 (rim · cloth · plan · Circles title · gear · colophon · gold ★) */
private val PERKS_LIVE = (0..6).toSet()

/** 🗣️ Stempeluhr line pool (~12, rotate by day = variable reward · nvr a Kapital quote here) */
private val SHIFT_LINES = listOf(
    T("Schicht erledigt. Der Mehrwert gehört heute dir.", "Shift done. Today the surplus value is yours."),
    T("Tagessoll erfüllt — ohne Aufseher.", "Quota met — no overseer."),
    T("Lesekapital +1. Ausbeutungsfrei.", "Reading capital +1. Exploitation-free."),
    T("Stempel drauf. Die Uhr gehört heute dir.", "Stamped. Today the clock is yours."),
    T("Nicht mal Engels war so pünktlich.", "Not even Engels was this punctual."),
    T("Ein Tag, den dir keiner abkaufen kann.", "A day nobody can buy off you."),
    T("Feierabend. Die Maschine läuft ohne dich weiter.", "Clocked out. The machine runs on without you."),
    T("Heute hast du nur für dich gearbeitet.", "Today you worked for yourself only."),
    T("Notwendige Arbeitszeit: erledigt. Der Rest ist frei.", "Necessary labour time: done. The rest is free."),
    T("Kein Akkord, kein Vorarbeiter — nur du und das Buch.", "No piecework, no foreman — just you and the book."),
    T("Gestempelt. Die Fabriksirene schweigt.", "Stamped. The factory siren is silent."),
    T("Wieder ein Tag Theorie im Kasten.", "Another day of theory in the bag."),
)
/** line 1 ("Schicht erledigt") = th hero's own copy ➡ skipped (design § 14.1.2 ⚠️) */
fun shiftLine(date: LocalDate = LocalDate.now()) = SHIFT_LINES[1 + Math.floorMod(date.toEpochDay(), (SHIFT_LINES.size - 1).toLong()).toInt()]

private fun shareText(ctx: Context, text: String) = runCatching {
    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), null)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Text btn on th dark fullscreen moments: TextBtn's blue vanishes on ink ➡ white 85 %, h 48 */
@Composable
private fun ShareOnDark(onClick: () -> Unit) = Box(Modifier.height(48.dp).clip(Shapes.xl)
    .clickable(onClick = onClick).padding(horizontal = 20.dp), contentAlignment = Alignment.Center) {
    Text(tr("Teilen", "Share"), color = Ds.white.copy(alpha = .85f), style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun rememberStill(): Boolean { val ctx = LocalContext.current; return remember { reducedMotion(ctx) } }

// ── T2 · Stempeluhr ─────────────────────────────────────────────────────────────────────────────────────────

/**
 * ⏱️ Day complete = th time clock (D-048 a+b+c): card w today's ☭ thumping in · star counting N-1 ➡ N + glow ·
 * 1 line fr th pool · tomorrow = a teasing Q (C7 `qd`/`qe`) · Primary "Feierabend" o "Moment … ▸" wh more is due
 */
@Composable
fun StempeluhrPane(
    card: StampCard, cardNo: Int, streak: Int, tier: Int, line: String, teaser: String?, more: Boolean,
    hooked: Boolean, level: Int = 0, onClose: () -> Unit,
) {
    val still = rememberStill()
    val h = rememberHaptics()
    val thump = remember { Animatable(if (still) 1f else 1.6f) }
    val tilt = remember { Animatable(if (still) -3f else -8f) }
    val count = remember { Animatable(if (still) 1f else 0f) }
    val fade = remember { Animatable(if (still) 1f else 0f) }
    val glow = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (still) { h.confirm(); return@LaunchedEffect }
        delay(400)
        h.longPress()
        launch { thump.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
        launch { tilt.animateTo(-3f, tween(260)) }
        delay(300)
        launch { count.animateTo(1f, tween(260)) }
        launch { glow.animateTo(1f, tween(200)); glow.animateTo(0f, tween(500)) }
        delay(300)
        fade.animateTo(1f, tween(400))
    }
    Dialog(onClose) {
        Column(Modifier.fillMaxWidth().clip(Shapes.xl).background(Ds.paperHi).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.fillMaxWidth()) { CloseStar(onClose, Modifier.align(Alignment.TopEnd)) }
            Text(tr("STEMPELKARTE %d", "STAMP CARD %d").f(cardNo), style = OmmType.bookMeta, color = Ds.inkMuted,
                modifier = Modifier.fillMaxWidth())
            // the card: today's slot = card.filled - 1 (just stamped) · full card (filled 0 aftr wrap) ➡ all 10
            val stamped = if (card.filled == 0) CARD else card.filled
            // 🎖️ level-2 perk (D-050): th card turns blue cloth
            val cloth = level >= 2
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp).clip(Shapes.m).background(if (cloth) Ds.blueTint else Ds.paper)
                .border(if (cloth) 2.dp else 1.dp, if (cloth) Ds.blue else Ds.line, Shapes.m).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(CARD) { i ->
                    val today = i == stamped - 1
                    Box(Modifier.size(22.dp).then(if (today) Modifier.scale(thump.value).rotate(tilt.value) else Modifier)
                        .clip(CircleShape).then(if (i < stamped) Modifier.background(Ds.gold) else Modifier.border(1.5.dp, Ds.outline, CircleShape)),
                        contentAlignment = Alignment.Center) {
                        if (i < stamped) Text("☭", style = MaterialTheme.typography.labelMedium, color = Ds.red, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Box(contentAlignment = Alignment.Center) {
                Box(Modifier.size(84.dp).clip(CircleShape).background(Ds.gold.copy(alpha = .35f * glow.value)))
                StreakStar(if (count.value < .5f) (streak - 1).coerceAtLeast(0) else streak, tier, size = 72.dp, onGradient = false, level = level)
            }
            Text(if (streak == 1) tr("1 Tag am Stück", "1 day in a row") else tr("%d Tage am Stück", "%d days in a row").f(streak),
                style = MaterialTheme.typography.titleMedium, color = Ds.ink, modifier = Modifier.padding(top = 6.dp))
            Column(Modifier.alpha(fade.value), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(quoted(line), style = MaterialTheme.typography.bodyLarge, color = Ds.ink, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp))
                if (teaser != null) {
                    HorizontalDivider(color = Ds.line, modifier = Modifier.padding(vertical = 10.dp))
                    Text(tr("Morgen: %s", "Tomorrow: %s").f(teaser), style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic, color = Ds.inkMuted, textAlign = TextAlign.Center)
                }
            }
            HookNote(Hook.SHIFT_CLOCK, hooked)
            if (more) HookNote(Hook.OPEN_LOOP, hooked)
            HookNote(Hook.NEXT, hooked)
            Box(Modifier.padding(top = 14.dp)) {
                Primary(if (more) tr("Moment … ▸", "Wait … ▸") else tr("Feierabend", "Clock out"), onClose)
            }
        }
    }
}

// ── T3 · Meilenstein ────────────────────────────────────────────────────────────────────────────────────────

/** 🏅 streak milestone (D-046): big star grows its new ornament · recap chain fills · ✊ +1 Streiktag token */
@Composable
fun MilestoneScreen(n: Int, tier: Int, best: Boolean, chain: List<Cell>, strikeEarned: Boolean, hooked: Boolean, onClose: () -> Unit) {
    val still = rememberStill()
    val h = rememberHaptics()
    val ctx = LocalContext.current
    val fill = remember { Animatable(if (still) 1f else 0f) }
    val grow = remember { Animatable(if (still) 1f else 0f) }
    val head = remember { Animatable(if (still) 1f else 0f) }
    val token = remember { Animatable(if (still) 1f else 0f) }
    LaunchedEffect(Unit) {
        h.celebrate()
        if (still) return@LaunchedEffect
        delay(200); fill.animateTo(1f, tween(800))
        grow.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        head.animateTo(1f, tween(300))
        token.animateTo(1f, tween(400))
    }
    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Column(Modifier.fillMaxSize().background(Ds.ink.copy(alpha = .85f)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            StreakStar(n, tier, size = 160.dp, grow = grow.value)
            Text(tr("%d Tage ununterbrochen", "%d days unbroken").f(n), style = OmmType.display, color = Ds.white,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 16.dp).alpha(head.value))
            if (best) Text(tr("Rekord!", "Record!"), style = MaterialTheme.typography.labelMedium, color = Ds.gold, modifier = Modifier.alpha(head.value))
            // recap chain: last 14 · 🕊 + ✊ = own icon
            val shown = (chain.size * fill.value).toInt()
            Row(Modifier.padding(vertical = 18.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                chain.forEachIndexed { i, c ->
                    Box(Modifier.size(16.dp).clip(Shapes.tick).background(when {
                        i >= shown -> Ds.white.copy(alpha = .12f)
                        c == Cell.DONE -> Ds.gold
                        c == Cell.EMPTY -> Ds.white.copy(alpha = .12f)
                        else -> Ds.white.copy(alpha = .8f)
                    }), contentAlignment = Alignment.Center) {
                        if (i < shown && c == Cell.REST) Text("🕊", style = MaterialTheme.typography.labelSmall)
                        if (i < shown && c == Cell.STRIKE) Text("✊", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (strikeEarned) Row(Modifier.offset { IntOffset(0, ((1 - token.value) * 60).toInt()) }.alpha(token.value)
                .fillMaxWidth().clip(Shapes.m).background(Ds.white.copy(alpha = .1f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("✊", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(tr("+1 Streiktag erkämpft", "+1 strike day won"), color = Ds.white, style = MaterialTheme.typography.titleSmall)
                    Text(tr("rettet 1 verpassten Tag", "saves 1 missed day"), color = Ds.white.copy(alpha = .75f), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HookNote(Hook.MILESTONE, hooked)
            if (strikeEarned) HookNote(Hook.STRIKE, hooked)
            Gap(18)
            Primary(tr("Weiter", "Continue"), onClose)
            val msg = tr("%d Tage Kapital am Stück ☭ — Oh My Marx", "%d days of Capital in a row ☭ — Oh My Marx").f(n)
            ShareOnDark { shareText(ctx, msg) }
        }
    }
}

// ── T4 · Beförderung ────────────────────────────────────────────────────────────────────────────────────────

/** 🎖️ rank-up = lvl-up (D-050 a · D-060): rays · old badge out, new flies in · LEVEL N ▲ pops + XP bar fills · confetti */
@Composable
fun PromotionScreen(rankIdx: Int, totalDays: Int, milestone: Int?, hooked: Boolean, onClose: () -> Unit) {
    val still = rememberStill()
    val h = rememberHaptics()
    val ctx = LocalContext.current
    val rays = remember { Animatable(if (still) 0f else 1f) }
    val spin = remember { Animatable(0f) }
    val fly = remember { Animatable(if (still) 1f else 0f) }
    val seal = remember { Animatable(if (still) 1f else 0f) }
    val txt = remember { Animatable(if (still) 1f else 0f) }
    val confetti = remember { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (still) { h.celebrate(); return@LaunchedEffect }
        launch { spin.animateTo(14f, tween(2300)) }
        delay(400); fly.animateTo(1f, spring(dampingRatio = .55f))
        delay(600); seal.animateTo(1f, spring(dampingRatio = .4f, stiffness = Spring.StiffnessMedium)); h.celebrate()
        delay(300); confetti.value = true; txt.animateTo(1f, tween(400))
        rays.animateTo(0f, tween(600))   // rays stop (nvr loop 4ever, § 8)
    }
    Dialog(onClose, DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Ds.ink.copy(alpha = .9f))) {
            if (confetti.value) Confetti {}
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        repeat(12) { i -> rotate(spin.value + i * 30f) {
                            drawLine(Ds.gold.copy(alpha = .5f * rays.value), Offset(center.x, center.y - size.minDimension * .2f),
                                Offset(center.x, 0f), 6.dp.toPx())
                        } }
                    }
                    if (rankIdx > 0) Image(painterResource(rankArt(rankIdx - 1)), null, Modifier.size(56.dp)
                        .offset { IntOffset((-110 * fly.value).toInt(), 0) }.alpha(1 - fly.value))
                    Image(painterResource(rankArt(rankIdx)), tr(rank(rankIdx)), Modifier.size(120.dp)
                        .offset { IntOffset(0, ((1 - fly.value) * -400).toInt()) }.scale(.6f + .4f * fly.value))
                    // D-060 (qa_1120 "GENEHMIGT = bureaucratic ➡ lvl-up"): gold LEVEL UP pill pops (overshoot), no rubber stamp
                    Text(tr("LEVEL %d ▲", "LEVEL %d ▲").f(rankIdx + 1), color = Ds.ink, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.align(Alignment.BottomCenter).alpha(seal.value).scale(.4f + .6f * seal.value)
                            .background(Brush.verticalGradient(listOf(androidx.compose.ui.graphics.lerp(Ds.gold, Ds.white, .45f), Ds.gold)), Shapes.pill)
                            .border(2.dp, Ds.white, Shapes.pill).padding(horizontal = 16.dp, vertical = 4.dp))
                }
                // XP bar: old lvl ➡ new lvl fills (D-060 · same seal anim)
                Box(Modifier.padding(top = 14.dp).width(200.dp).height(8.dp).clip(Shapes.pill).background(Ds.white.copy(alpha = .15f))) {
                    Box(Modifier.fillMaxWidth(((rankIdx + seal.value) / RANKS.size).coerceIn(0f, 1f)).height(8.dp).clip(Shapes.pill).background(Ds.gold))
                }
                Column(Modifier.alpha(txt.value), horizontalAlignment = Alignment.CenterHorizontally) {
                    // (RANG N / 7 line rm'd: th LEVEL pill says it, D-060)
                    Text(tr(rank(rankIdx)).substringAfter(' '), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif,
                        color = Ds.white, textAlign = TextAlign.Center)
                    // perk line only once th perk is real (D-004 honest) · PERKS_LIVE grows as each perk ships (🔵 todo)
                    if (rankIdx in PERKS_LIVE) Text(tr(PERKS[rankIdx]), style = MaterialTheme.typography.bodyMedium, color = Ds.white,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
                    Text(tr("%d Lesetage · Karte %d voll", "%d reading days · card %d full").f(totalDays, rankIdx + 1),
                        style = MaterialTheme.typography.labelMedium, color = Ds.white.copy(alpha = .7f), modifier = Modifier.padding(top = 4.dp))
                    // T3 + T4 same day ➡ th milestone = a 2nd line here, not a 2nd screen (D-049)
                    milestone?.let { Text(tr("+ %d Tage am Stück 🏅", "+ %d days in a row 🏅").f(it), style = MaterialTheme.typography.labelMedium,
                        color = Ds.gold, modifier = Modifier.padding(top = 4.dp)) }
                }
                HookNote(Hook.PROMOTION, hooked)
                Gap(18)
                Primary(tr("Weiter", "Continue"), onClose)
                val msg = tr("Befördert: %s ☭ — Oh My Marx", "Promoted: %s ☭ — Oh My Marx").f(tr(rank(rankIdx)))
                ShareOnDark { shareText(ctx, msg) }
            }
        }
    }
}

/** hero bar: nxt milestone (4dp gold on white 20 % + "noch 3 bis 14") */
@Composable
fun MilestoneBar(streak: Int, next: Int, modifier: Modifier = Modifier) {
    val prev = de.strix.marxversion.logic.MILESTONES.lastOrNull { it <= streak && it < next } ?: 0
    val p = ((streak - prev).toFloat() / (next - prev).coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(modifier) {
        Box(Modifier.fillMaxWidth().height(4.dp).clip(Shapes.tick).background(Ds.white.copy(alpha = .2f))) {
            Box(Modifier.fillMaxWidth(p).height(4.dp).clip(Shapes.tick).background(Ds.gold))
        }
        Text(tr("noch %d bis %d", "%d to go to %d").f(next - streak, next), style = MaterialTheme.typography.labelMedium,
            color = Ds.white.copy(alpha = .85f), modifier = Modifier.padding(top = 3.dp))
    }
}
