package de.strix.marxversion.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore = async key-value store (replaces SharedPreferences) · 1 file, survives app restarts

private val Context.store by preferencesDataStore("settings")

data class Settings(
    val lang: Lang = Lang.system(),   // never picked ➡ fon lang if we hv it, else EN (@1106: EN 1st, DE 2nd)
    val notifOn: Boolean = true,
    val notifHour: Int = 8,
    val notifMinute: Int = 55,          // Mo 8:55 = 5 min b4 th Arbeitstag starts
    val testMode: Boolean = false,      // 🧪 Testmodus: shows th app's own Hooked tricks + test buttons
    val persona: String = "GOTTVATER",  // TTS voice preset (ui/Share.kt Persona)
    val popupSeen: String? = null,      // date th Losung popup was last shown ("2026-10-31")
    val activePlan: String? = null,
    val planStart: String? = null,      // "2026-10-28"
    val readScale: Float = 1f,          // 🤏 pinch i th reader ➡ font size × this
    val community: Boolean = true,      // 👥 Mitlesen: anonymous daily-token counts + comments · always on since QA r5 (D-020, no ⚙️ toggle)
)

/** 👥 a Lesekreis = 1 Matrix room, found by its code */
data class Kreis(val room: String, val code: String, val name: String)

class Prefs(private val context: Context) {
    private object K {
        val lang = stringPreferencesKey("lang")
        val notifOn = booleanPreferencesKey("notif_on")
        val notifHour = intPreferencesKey("notif_hour")
        val notifMinute = intPreferencesKey("notif_minute")
        val testMode = booleanPreferencesKey("test_mode")
        val persona = stringPreferencesKey("persona")
        val popupSeen = stringPreferencesKey("popup_seen")
        val activePlan = stringPreferencesKey("active_plan")
        val planStart = stringPreferencesKey("plan_start")
        val readScale = floatPreferencesKey("read_scale")
        val community = booleanPreferencesKey("community")
        val commDay = stringPreferencesKey("comm_day")      // th day th token belongs 2
        val commToken = stringPreferencesKey("comm_token")  // random, new evy day
    }

    /** 👥 today's random token (new day ➡ new token ➡ th server can't link days) */
    suspend fun commToken(day: String): String {
        var t = ""
        context.store.edit { p ->
            t = if (p[K.commDay] == day) p[K.commToken] ?: "" else ""
            if (t.isEmpty()) { t = java.util.UUID.randomUUID().toString().replace("-", ""); p[K.commDay] = day; p[K.commToken] = t }
        }
        return t
    }
    suspend fun setCommunity(on: Boolean) = context.store.edit { it[K.community] = on }

    // 💬 Matrix account (made on 1st comment, data/Matrix.kt) · lives only on this fon
    private object M {
        val user = stringPreferencesKey("mx_user"); val token = stringPreferencesKey("mx_token")
        val pw = stringPreferencesKey("mx_pw"); val name = stringPreferencesKey("mx_name")
    }
    val mxName: Flow<String?> = context.store.data.map { it[M.name] }
    suspend fun mxSession(): Matrix.Session? = context.store.data.first().let { p ->
        val u = p[M.user]; val t = p[M.token]
        if (u != null && t != null) Matrix.Session(u, t) else null
    }
    suspend fun saveMx(s: Matrix.Session, pw: String, name: String) = context.store.edit {
        it[M.user] = s.userId; it[M.token] = s.token; it[M.pw] = pw; it[M.name] = name
    }
    suspend fun setMxName(name: String) = context.store.edit { it[M.name] = name }

    // 👥 my Kreise: "roomId|code|name" (room ids never contain '|')
    private val kreiseKey = stringSetPreferencesKey("kreise")
    val kreise: Flow<List<Kreis>> = context.store.data.map { p ->
        p[kreiseKey].orEmpty().mapNotNull { e -> e.split("|", limit = 3).takeIf { it.size == 3 }?.let { Kreis(it[0], it[1], it[2]) } }.sortedBy { it.name }
    }
    suspend fun addKreis(k: Kreis) = context.store.edit { it[kreiseKey] = it[kreiseKey].orEmpty().filterNot { e -> e.startsWith(k.room + "|") }.toSet() + "${k.room}|${k.code}|${k.name}" }
    // 🔔 KreisWorker bookkeeping: newest msg ts seen per room · day o th last "du fehlst noch" nudge
    private fun seenKey(room: String) = longPreferencesKey("kseen_" + room.hashCode())
    private val nudgeKey = stringPreferencesKey("kreis_nudge_day")
    suspend fun kreisSeen(room: String): Long? = context.store.data.first()[seenKey(room)]
    suspend fun setKreisSeen(room: String, ts: Long) = context.store.edit { p -> if ((p[seenKey(room)] ?: 0) < ts) p[seenKey(room)] = ts }
    suspend fun nudgeDay(): String? = context.store.data.first()[nudgeKey]
    suspend fun setNudgeDay(day: String) = context.store.edit { it[nudgeKey] = day }

    suspend fun dropKreis(room: String) = context.store.edit { it[kreiseKey] = it[kreiseKey].orEmpty().filterNot { e -> e.startsWith("$room|") }.toSet() }

    val settings: Flow<Settings> = context.store.data.map { p: Preferences ->
        Settings(
            lang = p[K.lang]?.let { runCatching { Lang.valueOf(it) }.getOrNull() } ?: Lang.system(),
            notifOn = p[K.notifOn] ?: true,
            notifHour = p[K.notifHour] ?: 8,
            notifMinute = p[K.notifMinute] ?: 55,
            testMode = p[K.testMode] ?: false,
            persona = p[K.persona] ?: "GOTTVATER",
            popupSeen = p[K.popupSeen],
            activePlan = p[K.activePlan],
            planStart = p[K.planStart],
            readScale = p[K.readScale] ?: 1f,
            community = true,   // QA r5 (D-020): Mitlesen always on, toggle gone ➡ an old stored "off" no longer counts
        )
    }

    suspend fun setLang(l: Lang) = context.store.edit { it[K.lang] = l.name }
    suspend fun setTestMode(on: Boolean) = context.store.edit { it[K.testMode] = on }
    suspend fun setPersona(p: String) = context.store.edit { it[K.persona] = p }
    suspend fun setReadScale(s: Float) = context.store.edit { it[K.readScale] = s }
    suspend fun popupSeen(date: String?) = context.store.edit { if (date == null) it.remove(K.popupSeen) else it[K.popupSeen] = date }
    suspend fun setNotif(on: Boolean, hour: Int, minute: Int) = context.store.edit {
        it[K.notifOn] = on; it[K.notifHour] = hour; it[K.notifMinute] = minute
    }
    suspend fun startPlan(id: String, date: String) = context.store.edit {
        it[K.activePlan] = id; it[K.planStart] = date
    }

    // 🎖️ Orden (logic/Orden.kt): th few things th DB doesn't alr know + which medals were alr announced
    private object O {
        val flags = stringSetPreferencesKey("orden_flags")   // "night" · "founder" · "invite" · "krise" · "sabbath"
        val shares = intPreferencesKey("orden_shares")
        val shown = stringSetPreferencesKey("orden_shown")   // announced (so "new medal!" pops once)
    }
    data class OrdenState(val flags: Set<String>, val shares: Int, val shown: Set<String>?)
    val orden: Flow<OrdenState> = context.store.data.map { OrdenState(it[O.flags].orEmpty(), it[O.shares] ?: 0, it[O.shown]) }
    suspend fun flag(f: String) = context.store.edit { it[O.flags] = it[O.flags].orEmpty() + f }
    suspend fun shared() = context.store.edit { it[O.shares] = (it[O.shares] ?: 0) + 1 }
    suspend fun ordenShown(ids: Set<String>) = context.store.edit { it[O.shown] = it[O.shown].orEmpty() + ids }
}
