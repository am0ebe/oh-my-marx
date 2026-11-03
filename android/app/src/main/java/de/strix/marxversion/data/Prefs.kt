package de.strix.marxversion.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
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
    }

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
        )
    }

    suspend fun setLang(l: Lang) = context.store.edit { it[K.lang] = l.name }
    suspend fun setTestMode(on: Boolean) = context.store.edit { it[K.testMode] = on }
    suspend fun setPersona(p: String) = context.store.edit { it[K.persona] = p }
    suspend fun popupSeen(date: String?) = context.store.edit { if (date == null) it.remove(K.popupSeen) else it[K.popupSeen] = date }
    suspend fun setNotif(on: Boolean, hour: Int, minute: Int) = context.store.edit {
        it[K.notifOn] = on; it[K.notifHour] = hour; it[K.notifMinute] = minute
    }
    suspend fun startPlan(id: String, date: String) = context.store.edit {
        it[K.activePlan] = id; it[K.planStart] = date
    }
}
