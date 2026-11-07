package de.strix.marxversion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import de.strix.marxversion.MarxApp
import de.strix.marxversion.data.Community
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.data.Kreis
import de.strix.marxversion.BuildConfig
import de.strix.marxversion.data.DayLog
import de.strix.marxversion.data.Full
import de.strix.marxversion.data.Lang
import de.strix.marxversion.data.Mark
import de.strix.marxversion.data.ReadLog
import de.strix.marxversion.data.Settings
import de.strix.marxversion.logic.DayTime
import de.strix.marxversion.logic.Feast
import de.strix.marxversion.logic.dailyPool
import de.strix.marxversion.notify.refreshWidgets
import de.strix.marxversion.logic.today
import de.strix.marxversion.logic.verseOfDay
import de.strix.marxversion.notify.LosungWorker
import de.strix.marxversion.notify.schedule
import de.strix.marxversion.data.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import de.strix.marxversion.logic.Orden
import de.strix.marxversion.logic.Stats
import de.strix.marxversion.logic.bestStreak
import de.strix.marxversion.logic.streakInfo
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel = survives screen rotation · holds UI state as StateFlows.
 * Screens *observe* (collectAsStateWithLifecycle) and *call* th funs below — never touch db/prefs directly.
 */
class AppVm(app: Application) : AndroidViewModel(app) {
    private val marx = app as MarxApp
    val content = marx.content

    private val _loaded = MutableStateFlow(content.loaded)
    val loaded: StateFlow<Boolean> = _loaded

    /** false til DataStore delivered th real settings (dflt Settings() would flash th daily popup) */
    val prefsReady = MutableStateFlow(false)
    val settings: StateFlow<Settings> = marx.prefs.settings.onEach { prefsReady.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    val marks: StateFlow<List<Mark>> = marx.db.marks().all().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val readIds: StateFlow<Set<String>> = marx.db.logs().readIds().map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    val days: StateFlow<List<DayLog>> = marx.db.logs().days().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** 🧪 test mode: "🎲 neuer Vers" bumps this ➡ verse-o-day re-rolls (in memory only, gone aft restart) */
    val reroll = MutableStateFlow(0)
    /** popup opened by tapping th Losung (or 🪝 test chip) · normal: once a day, see MainActivity */
    val forcePopup = MutableStateFlow(false)
    /** 🚀 shortcut asked 4 a card on Today: "oracle" | "krise" · TodayScreen shows it + clears */
    val popRequest = MutableStateFlow<String?>(null)
    /** 🃏 pulled / shaken-in cards, newest 1st (max 10) · sit i front o today's Losung i th Home carousel */
    val extraCards = MutableStateFlow<List<CardItem>>(emptyList())
    fun addCard(c: CardItem) { extraCards.value = (listOf(c) + extraCards.value).take(10) }
    /** ++ ➡ Home carousel jumps back 2 th 1st card (home btn, notif tap) */
    val homeJump = MutableStateFlow(0)
    // ── 👥 Mitlesen (opt-in, data/Community.kt) ──
    /** today's crowd: readers + Amen · null = off / offline */
    val crowd = MutableStateFlow<Community.Stats?>(null)
    /** MEW page ➡ "N× gelesen" (filled as pages r shown) */
    val pageReads = MutableStateFlow<Map<Int, Int>>(emptyMap())
    private val on get() = settings.value.community

    fun setCommunity(on: Boolean) = viewModelScope.launch {
        marx.prefs.setCommunity(on)
        if (on) refreshCrowd() else { crowd.value = null; pageReads.value = emptyMap() }
    }
    fun refreshCrowd() = viewModelScope.launch { if (on) Community.stats()?.let { crowd.value = it } }
    /** book page shown ➡ count me (once/day, server-side) ➡ fetch its total */
    fun pageSeen(page: Int?) = viewModelScope.launch {
        if (!on || page == null) return@launch
        val k = Community.pageKey(page)
        Community.read(marx.prefs.commToken(today()), listOf(k))
        Community.stats(listOf(k))?.let { s ->
            pageReads.value = pageReads.value + (page to (s.reads[k] ?: 0)); crowd.value = s
        }
    }

    // ── 💬 comments per book page (Matrix, data/Matrix.kt) · needs Mitlesen ON + a token i th build ──
    val commentsAvailable get() = BuildConfig.OMM_REG_TOKEN.isNotBlank()
    val mxName: StateFlow<String?> = marx.prefs.mxName.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    /** chapter ➡ its comments (oldest 1st) */
    val comments = MutableStateFlow<Map<String, List<Matrix.Comment>>>(emptyMap())
    private val rooms = mutableMapOf<String, String>()

    /** existing account · o (name given) make one now */
    private suspend fun session(name: String? = null): Matrix.Session? {
        marx.prefs.mxSession()?.let { return it }
        val n = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val (s, pw) = Matrix.register(n, BuildConfig.OMM_REG_TOKEN) ?: return null
        marx.prefs.saveMx(s, pw, n)
        return s
    }
    private suspend fun room(s: Matrix.Session, kap: String) = rooms[kap] ?: Matrix.join(s, Matrix.alias(kap))?.also { rooms[kap] = it }

    fun loadComments(kap: String) = viewModelScope.launch {
        if (!on || !commentsAvailable) return@launch
        val s = session() ?: return@launch
        val r = room(s, kap) ?: return@launch
        comments.value = comments.value + (kap to Matrix.comments(s, r))
    }

    /** post ➡ done(ok) · 1st post w a name ➡ makes th account */
    fun postComment(kap: String, page: Int?, text: String, name: String?, done: (Boolean) -> Unit) = viewModelScope.launch {
        val s = session(name)
        val r = s?.let { room(it, kap) }
        val ok = s != null && r != null && Matrix.send(s, r, text.trim(), page, mxName.value ?: name ?: "")
        if (ok) comments.value = comments.value + (kap to Matrix.comments(s!!, r!!))
        done(ok)
    }

    fun deleteComment(kap: String, id: String) = viewModelScope.launch {
        val s = session() ?: return@launch
        val r = room(s, kap) ?: return@launch
        if (Matrix.redact(s, r, id)) comments.value = comments.value + (kap to Matrix.comments(s, r))
    }

    fun rename(name: String) = viewModelScope.launch {
        val n = name.trim().takeIf { it.isNotEmpty() } ?: return@launch
        marx.prefs.setMxName(n); session()?.let { Matrix.setName(it, n) }
    }
    suspend fun myUserId() = marx.prefs.mxSession()?.userId

    // ── 👥 Kreise (Matrix rooms joined by code, data/Matrix.kt) ──
    /** invite link opened ➡ th code waiting 4 "Beitreten" */
    val pendingKreis = MutableStateFlow<String?>(null)
    /** ++ ➡ MainActivity switches 2 th Kreis tab */
    val openKreise = MutableStateFlow(0)
    /** opened a Kreis ➡ its newest msg counts as seen (KreisWorker won't notify it) */
    fun kreisSeen(room: String, ts: Long) = viewModelScope.launch { marx.prefs.setKreisSeen(room, ts) }
    val kreise: StateFlow<List<Kreis>> = marx.prefs.kreise.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    /** room ➡ members (user id ➡ name) */
    val kreisMembers = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    /** room ➡ chat + Amens, oldest 1st */
    val kreisTimeline = MutableStateFlow<Map<String, List<Matrix.Event>>>(emptyMap())

    /** found a Kreis ➡ done(code) · null = failed · new code if th random one is taken */
    fun createKreis(name: String, myName: String?, done: (String?) -> Unit) = viewModelScope.launch {
        val s = session(myName) ?: return@launch done(null)
        repeat(3) {
            val code = Matrix.newCode()
            Matrix.createKreis(s, name.trim(), code)?.let { room ->
                marx.prefs.addKreis(Kreis(room, code, name.trim())); marx.prefs.flag("founder"); loadKreis(room); return@launch done(code)
            }
        }
        done(null)
    }

    fun joinKreis(code: String, myName: String?, done: (Boolean) -> Unit) = viewModelScope.launch {
        val c = code.trim().uppercase().replace(" ", "")
        val s = session(myName) ?: return@launch done(false)
        val room = Matrix.join(s, Matrix.kreisAlias(c)) ?: return@launch done(false)
        marx.prefs.addKreis(Kreis(room, c, Matrix.roomName(s, room) ?: c))
        loadKreis(room); done(true)
    }

    fun leaveKreis(k: Kreis) = viewModelScope.launch {
        session()?.let { Matrix.leave(it, k.room) }
        marx.prefs.dropKreis(k.room)
    }

    fun loadKreis(room: String) = viewModelScope.launch {
        val s = session() ?: return@launch
        kreisMembers.value = kreisMembers.value + (room to Matrix.members(s, room))
        kreisTimeline.value = kreisTimeline.value + (room to Matrix.timeline(s, room))
    }

    fun kreisSay(room: String, text: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val s = session() ?: return@launch done(false)
        val ok = Matrix.sendEvent(s, room, "m.room.message", org.json.JSONObject().put("msgtype", "m.text").put("body", text.trim())
            .put("de.ohmymarx.name", mxName.value ?: ""))
        if (ok) loadKreis(room)
        done(ok)
    }

    /** my Amen ➡ evy Kreis gets a ☭ (th others see "Rosa ✔️ heute") */
    private suspend fun amenToKreise() {
        val s = marx.prefs.mxSession() ?: return
        kreise.value.forEach { k ->
            Matrix.sendEvent(s, k.room, Matrix.AMEN, org.json.JSONObject().put("day", today()).put("de.ohmymarx.name", mxName.value ?: ""))
        }
    }

    /** 🪝 preview a feast / time o day (null = real calendar + clock) */
    val feastPreview = MutableStateFlow<Feast?>(null)
    val timePreview = MutableStateFlow<DayTime?>(null)

    /** 🆕 newer version on th download page (data/Update.kt) · checked once per app start */
    val update = MutableStateFlow<Update.Latest?>(null)

    /** 📖 whole book i th LLM langs, paired w its lang (MainActivity only passes it on if th lang matches) · Full null = DE/EN
     *  🐛 @1107 was: lang switch ➡ old book dropped ➡ EN shown while th new 2-5 MB parsed ➡ "switches 2 EN 1st"
     *  now setLang loads th new book 1st, then flips th lang ➡ 1 jump straight 2 th picked lang */
    private val _full = MutableStateFlow<Pair<Lang, Full?>?>(null)
    val full: StateFlow<Pair<Lang, Full?>?> = _full
    private suspend fun loadFull(l: Lang) {
        if (_full.value?.first != l) _full.value = l to withContext(Dispatchers.IO) { content.full(l) }
    }

    init {
        // parsing ~6 MB JSON on th main thread would freeze th UI ➡ IO dispatcher
        viewModelScope.launch(Dispatchers.IO) { content.load(); _loaded.value = true; refreshWidgets(app) }
        // app start (+ any lang change not via setLang) ➡ load th book 4 th saved lang · no-op if setLang alr did
        viewModelScope.launch {
            settings.map { it.lang }.distinctUntilChanged().collectLatest { l -> loadFull(l) }
        }
        viewModelScope.launch { update.value = Update.check(app) }
    }

    private val pool by lazy { dailyPool(content.verses) }

    /** 🎉 feast verse wins (unless re-rolled), else th date-seeded ⭐ verse */
    fun verseOfDay(r: Int, feast: Feast?) =
        feast?.takeIf { r == 0 }?.let { content.verse(it.verse) } ?: verseOfDay(pool, today(), r)

    /** th Losung any day had (popup stories: tap left = go back in time) · same rule as th real day: feast verse 1st */
    fun losungOn(d: java.time.LocalDate) =
        Feast.on(d)?.let { content.verse(it.verse) } ?: verseOfDay(pool, d.toString(), 0)

    private fun widgets() = viewModelScope.launch(Dispatchers.IO) { refreshWidgets(getApplication()) }

    fun mark(id: String) = marx.db.marks().one(id)

    private fun edit(id: String, f: (Mark) -> Mark) = viewModelScope.launch {
        val dao = marx.db.marks()
        dao.put(f(dao.get(id) ?: Mark(id)).copy(updatedAt = System.currentTimeMillis()))
    }

    fun highlight(id: String, color: Int?) = edit(id) { it.copy(highlight = if (it.highlight == color) null else color) }
    fun bookmark(id: String) = edit(id) { it.copy(bookmark = !it.bookmark) }
    fun note(id: String, text: String) = edit(id) { it.copy(note = text.ifBlank { null }) }

    fun read(ids: List<String>) = viewModelScope.launch { ids.forEach { marx.db.logs().read(ReadLog(it)) } }

    /** "Tag geschafft! ☭" */
    fun dayDone(planId: String, planDay: Int, verseIds: List<String>) = viewModelScope.launch {
        nightCheck()
        marx.db.logs().day(DayLog(today(), planId, planDay))
        read(verseIds)
        widgets()
    }

    // ── 🎖️ Orden (logic/Orden.kt): recomputed whenever reading/marks/circles/counters change ──
    val orden: StateFlow<Set<Orden>> = combine(combine(days, _loaded) { d, _ -> d }, readIds, marks, kreise, marx.prefs.orden) { d, read, m, k, o ->
        if (!content.loaded) return@combine emptySet()
        val dates = d.map { java.time.LocalDate.parse(it.date) }.toSet()
        val (st, rest) = streakInfo(d)
        val chapters = content.chapters.map { it.kap }.filter { kap -> content.chapter(kap).let { v -> v.isNotEmpty() && v.all { it.id in read } } }.toSet()
        val plansDone = content.plans.count { p -> d.filter { it.planId == p.id }.map { it.planDay }.distinct().size >= p.days.size }
        Orden.earned(Stats(
            dates = dates, streak = st, best = maxOf(st, bestStreak(dates)),
            chaptersDone = chapters, bookDone = content.verses.all { it.id in read }, plansDone = plansDone,
            marks = m.count { it.highlight != null || it.note != null || it.bookmark }, shares = o.shares, kreise = k.size,
            flags = o.flags + if (rest.isNotEmpty()) setOf("sabbath") else emptySet(),
        ))
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /** earned but not announced yet ➡ "Neuer Orden!" dialog (MainActivity) */
    val newOrden: StateFlow<List<Orden>> = combine(orden, marx.prefs.orden) { e, o -> e.filter { it.name !in o.shown.orEmpty() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun ordenSeen(o: List<Orden>) = viewModelScope.launch { marx.prefs.ordenShown(o.map { it.name }.toSet()) }
    fun flag(f: String) = viewModelScope.launch { marx.prefs.flag(f) }
    private suspend fun nightCheck() { if (java.time.LocalTime.now().hour < 4) marx.prefs.flag("night") }

    /** "Amen ☭" on th Losung = th tiny habit: 1 verse read counts as a reading day (Fogg: ability > motivation) */
    fun amen(verseId: String) = viewModelScope.launch {
        nightCheck()
        marx.db.logs().day(DayLog(today(), "losung", 0))
        read(listOf(verseId))
        popupDone()
        widgets()
        if (on) { Community.amen(marx.prefs.commToken(today())); refreshCrowd() }  // 👥 "Heute N × Amen"
        amenToKreise()  // 👥 Kreise see it (explicit membership = consent, independent o Mitlesen)
    }

    fun startPlan(id: String) = viewModelScope.launch { marx.prefs.startPlan(id, today()) }
    fun setLang(l: Lang) = viewModelScope.launch { loadFull(l); marx.prefs.setLang(l); widgets() }
    fun setTestMode(on: Boolean) = viewModelScope.launch { marx.prefs.setTestMode(on) }
    fun setPersona(p: Persona) = viewModelScope.launch { marx.prefs.setPersona(p.name) }
    fun setReadScale(s: Float) = viewModelScope.launch { marx.prefs.setReadScale(s.coerceIn(0.7f, 2f)) }
    fun popupDone() = viewModelScope.launch { forcePopup.value = false; marx.prefs.popupSeen(today()) }
    fun setNotif(on: Boolean, h: Int, m: Int) = viewModelScope.launch {
        marx.prefs.setNotif(on, h, m)
        schedule(getApplication(), on, h, m)
    }

    // ── 🧪 test mode ──
    fun testNotifNow() = WorkManager.getInstance(getApplication()).enqueue(OneTimeWorkRequestBuilder<LosungWorker>().build())
    fun testUndoDay() = viewModelScope.launch { marx.db.logs().undoDay(today()); widgets() }
    /** cycle: real ➡ each feast ➡ real */
    fun testNextFeast() { feastPreview.value = Feast.entries.getOrNull((feastPreview.value?.ordinal ?: -1) + 1) }
    fun testNextTime() { timePreview.value = DayTime.entries.getOrNull((timePreview.value?.ordinal ?: -1) + 1) }
}
