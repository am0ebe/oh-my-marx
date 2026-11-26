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
import appkit.selfupdate.Update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import de.strix.marxversion.logic.Orden
import de.strix.marxversion.logic.Stats
import de.strix.marxversion.logic.bestStreak
import de.strix.marxversion.logic.streakInfo
import de.strix.marxversion.logic.UnlockStats
import de.strix.marxversion.logic.isOpen
import de.strix.marxversion.logic.eventOpens
import de.strix.marxversion.logic.plansDone
import de.strix.marxversion.logic.readingDays
import de.strix.marxversion.data.Plan
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    /** chapter ➡ token 4 th nxt page back (null = all loaded) · night F5: sheet's "older" btn */
    val commentsOlder = MutableStateFlow<Map<String, String?>>(emptyMap())
    private val paged = mutableSetOf<String>()   // chapters th user paged back i ➡ a refresh keeps th deepest token
    private val rooms = mutableMapOf<String, String>()

    /** newest page ➡ merged w older pages alr loaded · `gone` = just redacted */
    private suspend fun refreshComments(s: Matrix.Session, r: String, kap: String, gone: Set<String> = emptySet()) {
        val p = Matrix.commentPage(s, r)
        comments.value = comments.value + (kap to Matrix.mergeComments(comments.value[kap].orEmpty(), p.list, gone))
        if (kap !in paged) commentsOlder.value = commentsOlder.value + (kap to p.older)
    }

    /** 📜 1 page further back (300 msgs) · prepended, dupes dropped */
    fun loadOlderComments(kap: String) = viewModelScope.launch {
        val from = commentsOlder.value[kap] ?: return@launch
        val s = session() ?: return@launch
        val r = room(s, kap) ?: return@launch
        val p = Matrix.commentPage(s, r, from)
        val have = comments.value[kap].orEmpty()
        val ids = have.mapTo(HashSet()) { it.id }
        paged += kap
        comments.value = comments.value + (kap to p.list.filter { it.id !in ids } + have)
        commentsOlder.value = commentsOlder.value + (kap to p.older)
    }

    /** existing account · o (name given) make one now */
    private var mxChecked = false
    private suspend fun session(name: String? = null): Matrix.Session? {
        var dead = false
        marx.prefs.mxSession()?.let { s ->
            if (mxChecked) return s
            // 🔁 once per app run: does th server still K my token? 401 = homeserver replaced (@1113) / wiped ➡ drop + re-register (D-075)
            when (Matrix.tokenValid(s)) {
                true -> { mxChecked = true; return s }
                null -> return s   // offline ➡ ck again nxt call
                false -> { marx.prefs.dropMxSession(); rooms.clear(); dead = true }
            }
        }
        // stored name only aftr a dead token: an account nvr appears on a mere read
        val n = (name ?: if (dead) marx.prefs.mxName.first() else null)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val (s, pw) = Matrix.register(n, BuildConfig.OMM_REG_TOKEN) ?: return null
        marx.prefs.saveMx(s, pw, n)
        return s
    }
    private suspend fun room(s: Matrix.Session, kap: String) = rooms[kap] ?: Matrix.join(s, Matrix.alias(kap))?.also { rooms[kap] = it }

    fun loadComments(kap: String) = viewModelScope.launch {
        if (!on || !commentsAvailable) return@launch
        val s = session() ?: return@launch
        val r = room(s, kap) ?: return@launch
        refreshComments(s, r, kap)
    }

    /** post ➡ done(ok) · 1st post w a name ➡ makes th account */
    fun postComment(kap: String, page: Int?, text: String, name: String?, done: (Boolean) -> Unit) = viewModelScope.launch {
        val s = session(name)
        val r = s?.let { room(it, kap) }
        val ok = s != null && r != null && Matrix.send(s, r, text.trim(), page, mxName.value ?: name ?: "")
        if (ok) refreshComments(s!!, r!!, kap)
        done(ok)
    }

    fun deleteComment(kap: String, id: String) = viewModelScope.launch {
        val s = session() ?: return@launch
        val r = room(s, kap) ?: return@launch
        if (Matrix.redact(s, r, id)) refreshComments(s, r, kap, gone = setOf(id))
    }

    /** 🚩 D-088: report a stranger's comment ➡ server admins · done(ok) · fixed reason (no free txt = nothing new 2 moderate) */
    fun reportComment(kap: String, id: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val s = session()
        val r = s?.let { room(it, kap) }
        done(s != null && r != null && Matrix.report(s, r, id, "omm app: comment reported by a reader (chapter $kap)"))
    }

    /** 1 random comrade name per app run = th prefill evywhere a name is asked (⚙️ · comments · Kreise) */
    val suggestedName = comradeName()

    fun rename(name: String) = viewModelScope.launch {
        val n = name.trim().takeIf { it.isNotEmpty() } ?: return@launch
        marx.prefs.setMxName(n); session()?.let { Matrix.setName(it, n) }
    }
    suspend fun myUserId() = marx.prefs.mxSession()?.userId
    val mxUser: StateFlow<String?> = marx.prefs.mxUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** 🗑️ D-072: delete my Matrix account (comments + Kreise) ➡ done(ok) · local notes stay · no account = ok */
    fun deleteAccount(done: (Boolean) -> Unit) = viewModelScope.launch {
        val s = marx.prefs.mxSession() ?: return@launch done(true)
        val pw = marx.prefs.mxPw() ?: return@launch done(false)
        // D-076: continuwuity ignores `erase` ➡ redact evy own msg 1st · any fail ➡ stop (account kept, "try again" redacts th rest)
        // token alr dead (401 on joined_rooms) ➡ nothing left 2 redact w it ➡ deactivate says M_UNKNOWN_TOKEN = gone
        when (Matrix.tokenValid(s)) {
            true -> if (!Matrix.redactAllMine(s)) return@launch done(false)
            false -> {}
            null -> return@launch done(false)   // offline: nvr deactivate wo th redact pass
        }
        Matrix.setName(s, "")   // best effort: blank th profile name i evy room b4 leaving
        if (!Matrix.deactivate(s, pw)) return@launch done(false)
        marx.prefs.forgetMx()
        rooms.clear(); comments.value = emptyMap(); commentsOlder.value = emptyMap(); paged.clear(); kreisMembers.value = emptyMap(); kreisTimeline.value = emptyMap()
        done(true)
    }

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

    /** 🆕 newer version on th download page (data/Update.kt) · checked once per app start (+ bg ~daily: notify/UpdateWorker.kt) */
    val update = MutableStateFlow<Update.Latest?>(null)
    /** 🆕 UpdateDialog open (Today banner o update notification tap) */
    val showUpdate = MutableStateFlow(false)
    fun openUpdate() {
        showUpdate.value = true
        if (update.value == null) viewModelScope.launch { update.value = Update.check(getApplication()) }
    }

    /** 📖 whole book i th LLM langs, paired w its lang (MainActivity only passes it on if th lang matches) · Full null = DE/EN
     *  🐛 @1107 was: lang switch ➡ old book dropped ➡ EN shown while th new 2-5 MB parsed ➡ "switches 2 EN 1st"
     *  now setLang loads th new book 1st, then flips th lang ➡ 1 jump straight 2 th picked lang */
    private val _full = MutableStateFlow<Pair<Lang, Full?>?>(null)
    val full: StateFlow<Pair<Lang, Full?>?> = _full
    private suspend fun loadFull(l: Lang) {
        if (_full.value?.first != l) _full.value = l to withContext(Dispatchers.IO) {
            content.tr(l)   // ⚡ L6: this lang's UI table parsed here (IO), b4 th UI flips ➡ MainActivity's tr() = cache hit
            content.full(l)
        }
    }

    init {
        // parsing ~6 MB JSON on th main thread would freeze th UI ➡ IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            content.load()
            content.tr(marx.prefs.settings.first().lang)   // ⚡ L6: saved lang's table b4 `loaded` ➡ 1st frame nvr parses on main
            _loaded.value = true; refreshWidgets(app)
            planEvents(amenDay = false)                    // 🎁 feast gift plan (eg Christmas) on any app open
            content.warm()                                // footnotes, aftr th 1st screen is up
        }
        // 🔁 pipeline renamed verse ids (FÜFNZEHNTES fix @1113) ➡ marks + read log follow, once (data/IdMoves.kt)
        viewModelScope.launch(Dispatchers.IO) { de.strix.marxversion.data.IdMoves.migrate(app, marx.db, marx.prefs) }
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
        planEvents(amenDay = true)   // 🎲 lucky plan draw
    }

    // ── 🎖️ Orden (logic/Orden.kt): recomputed whenever reading/marks/circles/counters change ──
    val orden: StateFlow<Set<Orden>> = combine(combine(days, _loaded) { d, _ -> d }, readIds, marks, kreise, marx.prefs.orden) { d, read, m, k, o ->
        if (!content.loaded) return@combine emptySet()
        val dates = d.map { java.time.LocalDate.parse(it.date) }.toSet()
        val (st, rest) = streakInfo(d)
        val chapters = content.chapters.map { it.kap }.filter { kap -> content.chapter(kap).let { v -> v.isNotEmpty() && v.all { it.id in read } } }.toSet()
        val plansDone = plansDone(content.plans, planDays(d))
        Orden.earned(Stats(
            dates = dates, streak = st, best = maxOf(st, bestStreak(dates)),
            chaptersDone = chapters, bookDone = content.verses.all { it.id in read }, plansDone = plansDone,
            marks = m.count { it.highlight != null || it.note != null || it.bookmark }, shares = o.shares, kreise = k.size,
            flags = o.flags + if (rest.isNotEmpty()) setOf("sabbath") else emptySet(),
            readMinutes = o.readSecs / 60,
        ))
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    /** earned but not announced yet ➡ "Neuer Orden!" dialog (MainActivity) */
    val newOrden: StateFlow<List<Orden>> = combine(orden, marx.prefs.orden) { e, o -> e.filter { it.name !in o.shown.orEmpty() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun ordenSeen(o: List<Orden>) = viewModelScope.launch { marx.prefs.ordenShown(o.map { it.name }.toSet()) }

    // ── 🔒 plan unlocks (logic/PlanUnlock.kt · D-043) ──
    private fun planDays(d: List<DayLog>) = d.groupBy { it.planId }.mapValues { (_, l) -> l.map { it.planDay }.distinct().size }
    /** ids o all plans u can open now */
    val plansOpen: StateFlow<Set<String>> = combine(combine(days, _loaded) { d, _ -> d }, orden, settings) { d, o, s ->
        if (!content.loaded) return@combine emptySet()
        val dates = d.map { java.time.LocalDate.parse(it.date) }.toSet()
        val stats = UnlockStats(plansDone(content.plans, planDays(d)), o.size, maxOf(streakInfo(d).first, bestStreak(dates)),
            rank = de.strix.marxversion.logic.cardsDone(dates.size))   // 🎖️ rank 3 perk (D-050)
        content.plans.filter { isOpen(it, stats, s.plansOpen, IS_DEV && s.devUnlock) }.map { it.id }.toSet()
    }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())
    /** 🎁 locked plans th just opened, not announced yet (DEV switch = no fanfare, it opens all) */
    //   🐛 @1120 startup crash (DEV 1119.1944): fired b4 th lazy content load ➡ content.plans lateinit ➡ guard like plansOpen
    //   (plansOpen re-emits once _loaded flips ➡ this re-runs) · tst: UpgradeStartTest
    val newPlans: StateFlow<List<Plan>> = combine(plansOpen, marx.prefs.orden, settings) { open, o, s ->
        if (!content.loaded || (IS_DEV && s.devUnlock)) emptyList()
        else content.plans.filter { it.lock != null && it.id in open && "plan:${it.id}" !in o.shown.orEmpty() }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    fun plansSeen(ids: List<String>) = viewModelScope.launch { marx.prefs.ordenShown(ids.map { "plan:$it" }.toSet()) }
    /** 🎁 feast gift (any app open) · 🎲 lucky draw (Amen days only) ➡ persisted */
    fun planEvents(amenDay: Boolean) = viewModelScope.launch {
        if (!content.loaded) return@launch
        val s = marx.prefs.settings.first()
        val ids = eventOpens(content.plans, java.time.LocalDate.now(), readingDays(days.value) + if (amenDay) 1 else 0, s.plansOpen, amenDay)
        if (ids.isNotEmpty()) marx.prefs.openPlans(ids)
    }
    fun flag(f: String) = viewModelScope.launch { marx.prefs.flag(f) }
    /** ⏱️ D-062: + active reading secs (ChapterScreen ticks, only while touched ≤ 90 s ago) */
    fun addReadSecs(s: Long) { if (s > 0) viewModelScope.launch { marx.prefs.addReadSecs(s) } }
    private suspend fun nightCheck() { if (java.time.LocalTime.now().hour < 4) marx.prefs.flag("night") }

    /** "Amen ☭" on th Losung = th tiny habit: 1 verse read counts as a reading day (Fogg: ability > motivation) */
    fun amen(verseId: String) = viewModelScope.launch {
        nightCheck()
        marx.db.logs().day(DayLog(today(), "losung", 0))
        read(listOf(verseId))
        popupDone()
        widgets()
        planEvents(amenDay = true)   // 🎲 lucky plan draw
        if (on) { Community.amen(marx.prefs.commToken(today())); refreshCrowd() }  // 👥 "Heute N × Amen"
        amenToKreise()  // 👥 Kreise see it (explicit membership = consent, independent o Mitlesen)
    }

    fun startPlan(id: String) = viewModelScope.launch { marx.prefs.startPlan(id, today(), content.plan(id)?.ver ?: 1) }   // 🔢 pin th cut (D-042)
    fun setLang(l: Lang) = viewModelScope.launch { loadFull(l); marx.prefs.setLang(l); widgets() }
    fun setTestMode(on: Boolean) = viewModelScope.launch { marx.prefs.setTestMode(on) }
    fun setDevUnlock(on: Boolean) = viewModelScope.launch { marx.prefs.setDevUnlock(on) }
    fun setAutoUpdate(on: Boolean) = viewModelScope.launch { marx.prefs.setAutoUpdate(on) }
    fun setPersona(p: Persona) = viewModelScope.launch { marx.prefs.setPersona(p.name) }

    // ── 🔊 AI voice packs (D-085) · manifest 1×/week · no manifest (now) = nothing shown, system TTS as b4 ──
    data class VoiceUi(val lang: String = "", val offer: de.strix.marxversion.data.PackInfo? = null,
        val have: de.strix.marxversion.data.PackIndex? = null, val update: de.strix.marxversion.data.PackInfo? = null,
        val bytes: Long = 0, val progress: Float? = null, val failed: Boolean = false)
    val voice = MutableStateFlow(VoiceUi())
    /** th "load th AI voice?" dialog: 1× a week per lang (stamp i files/voice), then ⚙️ has it */
    fun voiceShouldAsk(lang: Lang) = marx.voice.shouldAsk(lang.code, System.currentTimeMillis())
    fun voiceAsked(lang: Lang) = viewModelScope.launch(Dispatchers.IO) { marx.voice.asked(lang.code, System.currentTimeMillis()) }
    private var voiceBase = de.strix.marxversion.data.VoicePacks.BASE
    private val voiceLoading = java.util.concurrent.atomic.AtomicBoolean(false)
    fun refreshVoice(lang: Lang, force: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        val p = marx.voice
        val m = p.manifest(System.currentTimeMillis(), force)
        m?.base?.takeIf { it.startsWith("https://") }?.let { voiceBase = it }
        // Ogg-Opus plays only on Android 10+ (MediaPlayer) ➡ older fons: no offer, system TTS as b4
        val offer = p.offer(m, lang.code).takeIf { android.os.Build.VERSION.SDK_INT >= 29 }
        val have = p.installed(lang.code); val upd = p.update(m, lang.code); val bytes = p.bytes(lang.code)
        voice.update { cur -> val same = cur.lang == lang.code   // a running download + its error survive a refresh
            VoiceUi(lang.code, offer, have, upd, bytes, progress = cur.progress.takeIf { same }, failed = same && cur.failed) }
    }
    fun loadVoice(pack: de.strix.marxversion.data.PackInfo) = viewModelScope.launch(Dispatchers.IO) {
        if (!voiceLoading.compareAndSet(false, true)) return@launch   // double tap = 1 download
        try {
            voice.update { it.copy(progress = 0f, failed = false) }
            val ok = marx.voice.install(pack, voiceBase) { f -> voice.update { it.copy(progress = f) } }
            voice.update { it.copy(progress = null, failed = !ok) }
        } finally { voiceLoading.set(false) }
        Lang.entries.firstOrNull { it.code == pack.lang }?.let { refreshVoice(it).join() }
    }
    fun deleteVoice(lang: Lang) = viewModelScope.launch(Dispatchers.IO) { marx.voice.delete(lang.code); refreshVoice(lang).join() }
    fun setReadScale(s: Float) = viewModelScope.launch { marx.prefs.setReadScale(s.coerceIn(0.7f, 2f)) }
    fun setReadScroll(on: Boolean) = viewModelScope.launch { marx.prefs.setReadScroll(on) }
    fun setReadPos(id: String) = viewModelScope.launch { marx.prefs.setReadPos(id) }
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
