package de.strix.marxversion.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.serialization.json.Json

/**
 * 🔁 verse ids renamed by th pipeline (bin/split.py ➡ data/id_moves.json ➡ assets content/id_moves.json, old ➡ new,
 * cumulative + chains composed) ➡ th usr's own stuff follows th passage: Mark (🔖 ✍️ highlight) + ReadLog (progress, badges)
 * · DayLog = plan day indices, no verse ids ➡ untouched · @1113: "FÜFNZEHNTES" typo fix moved 14,37-39 + all o Kap 15
 * runs once per new moves file (Prefs id_moves_done = moves.size) · 1 Room transaction · old ids that alr hv a row at th
 * new id (can't happen 4 @1113, kept safe) ➡ merged, nothing lost
 */
object IdMoves {
    fun load(ctx: Context): Map<String, String> = runCatching {
        ctx.assets.open("content/id_moves.json").bufferedReader().use { Json.decodeFromString<Map<String, String>>(it.readText()) }
    }.getOrDefault(emptyMap())

    /** pure: moved mark onto an existing one at th new id ➡ union (bookmark ‖ · highlight + note: existing wins, else moved) */
    fun merge(moved: Mark, there: Mark?): Mark = if (there == null) moved else there.copy(
        bookmark = there.bookmark || moved.bookmark,
        highlight = there.highlight ?: moved.highlight,
        note = listOfNotNull(there.note, moved.note).distinct().joinToString("\n\n").ifBlank { null },
        updatedAt = maxOf(there.updatedAt, moved.updatedAt),
    )

    /** rename evy row whose id is a key i `moves` ➡ count o rows moved */
    suspend fun apply(db: MarxDb, moves: Map<String, String>): Int {
        if (moves.isEmpty()) return 0
        val old = moves.keys.toList()
        return db.withTransaction {
            val marks = db.marks().byIds(old)
            val reads = db.logs().readByIds(old)
            db.marks().drop(old); db.logs().dropRead(old)
            marks.forEach { m ->
                val to = moves.getValue(m.verseId)
                db.marks().put(merge(m.copy(verseId = to), db.marks().get(to)))
            }
            reads.forEach { r -> db.logs().read(r.copy(verseId = moves.getValue(r.verseId))) }
            marks.size + reads.size
        }
    }

    /** app start: new moves since last time ➡ apply + remember */
    suspend fun migrate(ctx: Context, db: MarxDb, prefs: Prefs): Int {
        val moves = load(ctx)
        if (moves.size <= prefs.idMovesDone()) return 0
        return apply(db, moves).also { prefs.setIdMovesDone(moves.size) }
    }
}
