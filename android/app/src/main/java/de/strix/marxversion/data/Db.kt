package de.strix.marxversion.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// ── th usr's own stuff = "Mein Kapital" (Hooked: investment / stored value) ──
// Room = SQLite w compile-time checked queries · Flow = UI re-renders when a row changes

/** 1 row per Vers th usr touched: highlight colour, bookmark, own note */
@Entity
data class Mark(
    @PrimaryKey val verseId: String,
    val highlight: Int? = null,   // index into HIGHLIGHT_COLORS, null = none
    val bookmark: Boolean = false,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Vers was read (opened i Lesen / done i a plan) ➡ chapter progress */
@Entity
data class ReadLog(@PrimaryKey val verseId: String, val at: Long = System.currentTimeMillis())

/** "Tag geschafft! ☭" ➡ streak + plan progress · date = "2026-10-28" */
@Entity(primaryKeys = ["date", "planId"])
data class DayLog(val date: String, val planId: String, val planDay: Int)

@Dao
interface MarkDao {
    @Query("SELECT * FROM Mark WHERE highlight IS NOT NULL OR bookmark = 1 OR note IS NOT NULL ORDER BY updatedAt DESC")
    fun all(): Flow<List<Mark>>

    @Query("SELECT * FROM Mark WHERE verseId = :id")
    fun one(id: String): Flow<Mark?>

    @Query("SELECT * FROM Mark WHERE verseId = :id")
    suspend fun get(id: String): Mark?

    @Upsert
    suspend fun put(m: Mark)
}

@Dao
interface LogDao {
    @Upsert
    suspend fun read(r: ReadLog)

    @Query("SELECT verseId FROM ReadLog")
    fun readIds(): Flow<List<String>>

    @Upsert
    suspend fun day(d: DayLog)

    /** 🧪 test mode: undo "Tag geschafft" 4 today */
    @Query("DELETE FROM DayLog WHERE date = :date")
    suspend fun undoDay(date: String)

    @Query("SELECT * FROM DayLog ORDER BY date DESC")
    fun days(): Flow<List<DayLog>>
}

@Database(entities = [Mark::class, ReadLog::class, DayLog::class], version = 1)
abstract class MarxDb : RoomDatabase() {
    abstract fun marks(): MarkDao
    abstract fun logs(): LogDao

    companion object {
        fun create(context: Context) =
            Room.databaseBuilder(context, MarxDb::class.java, "marx.db").build()
    }
}
