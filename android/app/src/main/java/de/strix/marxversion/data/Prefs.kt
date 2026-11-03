package de.strix.marxversion.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore = async key-value store (replaces SharedPreferences) · 1 file, survives app restarts

private val Context.store by preferencesDataStore("settings")

data class Settings(
    val lang: Lang = Lang.DE,
    val notifOn: Boolean = true,
    val notifHour: Int = 8,
    val notifMinute: Int = 55,          // Mo 8:55 = 5 min b4 th Arbeitstag starts
    val testMode: Boolean = false,      // 🧪 Testmodus: shows th app's own Hooked tricks + test buttons
    val persona: String = "GOTTVATER",  // TTS voice preset (ui/Share.kt Persona)
    val popupSeen: String? = null,      // date th Losung popup was last shown ("2026-10-31")
    val activePlan: String? = null,
    val planStart: String? = null,      // "2026-10-28"
    val readScale: Float = 1f,          // 🤏 pinch i th reader ➡ font size × this
    val community: Boolean = true,      // 👥 Mitlesen (dflt ON @1103 strix · switch off i ⚙️): anonymous daily-token counts + comments
)

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

    val settings: Flow<Settings> = context.store.data.map { p: Preferences ->
        Settings(
            lang = p[K.lang]?.let { runCatching { Lang.valueOf(it) }.getOrNull() } ?: Lang.DE,
            notifOn = p[K.notifOn] ?: true,
            notifHour = p[K.notifHour] ?: 8,
            notifMinute = p[K.notifMinute] ?: 55,
            testMode = p[K.testMode] ?: false,
            persona = p[K.persona] ?: "GOTTVATER",
            popupSeen = p[K.popupSeen],
            activePlan = p[K.activePlan],
            planStart = p[K.planStart],
            readScale = p[K.readScale] ?: 1f,
            community = p[K.community] ?: true,
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
}
