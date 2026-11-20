package de.strix.marxversion.logic

import de.strix.marxversion.data.Plan
import java.time.LocalDate
import kotlin.random.Random

/**
 * 🔒 progressive disclosure 4 reading plans (qa_1117 Q12 · D-043): new plans r hidden behind a milestone, like a bonus level
 *  - plans / orden / streak = earned (best streak ever ➡ a broken chain nvr re-locks)
 *  - feast = 🎁 holiday gift (eg Christmas) · lucky = 🎲 random draw on an Amen day (variable reward, Hooked ch. 4)
 *    both = one-off events ➡ persisted (Prefs.plansOpen) so they stay open
 *  - DEV switch = all open (qa_1116 Q6 a)
 * plan.lock == null = open from day 1 (all plans b4 @1118)
 */
data class UnlockStats(val plansDone: Int = 0, val orden: Int = 0, val best: Int = 0, val rank: Int = 0)

/** 1-in-N per Amen day, once ≥ lock.lucky reading days */
const val LUCKY_ODDS = 5

fun isOpen(p: Plan, s: UnlockStats, opened: Set<String>, dev: Boolean): Boolean {
    val l = p.lock ?: return true
    if (dev || p.id in opened) return true
    return (l.plans > 0 && s.plansDone >= l.plans) || (l.orden > 0 && s.orden >= l.orden) || (l.streak > 0 && s.best >= l.streak) ||
        (l.rank > 0 && s.rank >= l.rank)
}

/**
 * one-off events today ➡ plan ids 2 open for good · feast = that feast's gift · lucky = only on an Amen day (`amenDay`),
 * seeded by th date ➡ 2 calls th same day = same result (no re-rolling by reopening th app) · max 1 lucky plan per draw
 */
fun eventOpens(plans: List<Plan>, date: LocalDate, readingDays: Int, opened: Set<String>, amenDay: Boolean): List<String> {
    val feast = Feast.on(date)?.name
    val gifts = plans.filter { p -> p.id !in opened && p.lock?.feast != null && p.lock.feast == feast }.map { it.id }
    val lucky = plans.filter { p -> p.id !in opened && (p.lock?.lucky ?: 0) > 0 && readingDays >= p.lock!!.lucky }
    val draw = amenDay && lucky.isNotEmpty() && Random(date.toEpochDay()).nextInt(LUCKY_ODDS) == 0
    return gifts + if (draw) listOf(lucky.first().id) else emptyList()
}

/** finished = evy day o ANY cut logged (a re-cut plan doesn't un-finish ppl) */
fun plansDone(plans: List<Plan>, done: Map<String, Int>): Int =
    plans.count { p -> (done[p.id] ?: 0) >= (listOf(p.days.size) + p.old.values.map { it.size }).min() }
